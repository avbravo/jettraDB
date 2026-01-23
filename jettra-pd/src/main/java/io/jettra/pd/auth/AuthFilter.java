package io.jettra.pd.auth;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(AuthFilter.class);

    @Inject
    TokenUtils tokenUtils;

    @Inject
    AuthService authService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        // Robustly strip leading slashes
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        System.out.println("DEBUG: AuthFilter path=" + path);
        LOG.infof("AuthFilter entering for path: %s", path);

        // 1. Truly public endpoints
        if (path.equals("api/auth/login") || path.equals("api/web-auth/login") ||
                path.equals("api/internal/pd/health") ||
                path.equals("api/internal/pd/register") ||
                path.equals("api/internal/pd/groups") ||
                path.equals("api/internal/pd/nodes") ||
                path.equals("health") || path.equals("health/box")) {
            return;
        }

        // 2. Token Validation for everything else
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        String token = authHeader.substring(7);
        try {
            if (!tokenUtils.validateToken(token)) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                return;
            }

            String username = tokenUtils.getUsername(token);
            if (username == null) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                return;
            }

            // Store username for resource access
            requestContext.setProperty("auth.username", username);

            // 3. Authorization (Role-based access)
            User user = authService.getUser(username);

            // isGlobalAdmin: Users who have broad administrative powers (User management,
            // Database management)
            // matching logic in AuthResource.login
            boolean isGlobalAdmin = "super-user".equals(username) || "admin".equals(username)
                    || "system-pd".equals(username) ||
                    (user != null && ("super-user".equals(user.profile()) || "management".equals(user.profile())
                            || "admin".equals(user.profile())));

            // canManageUsers: Specifically for User/Role management API
            boolean canManageUsers = isGlobalAdmin;

            // A. Admin-only endpoints: User/Role management and Node stop
            // Exception: Allow GET (listing) for authenticated users to support UI
            // rendering
            if (path.startsWith("api/auth/users") || path.startsWith("api/auth/roles") ||
                    path.startsWith("api/web-auth/users") || path.startsWith("api/web-auth/roles") ||
                    (path.equals("stop") || path.contains("/stop"))) {

                String method = requestContext.getMethod();
                boolean isListing = method.equals("GET") && !(path.equals("stop") || path.contains("/stop"));
                boolean isStopNode = path.equals("stop") || path.contains("/stop");

                if (!canManageUsers && !isListing) {
                    requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                            .entity("{\"error\":\"Action restricted to administrative users.\"}")
                            .build());
                    return;
                }

            // Extra restriction: Only super-user profile can stop nodes
            // system-pd is allowed for internal operations
            if (isStopNode) {
                if (!"system-pd".equals(username) && (user == null || !"super-user".equals(user.profile()))) {
                    requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                            .entity("{\"error\":\"Only super-user can stop nodes.\"}")
                            .build());
                    return;
                }
            }
            }

            // B. Database access filtering
            String dbName = null;
            if (path.contains("/databases/") || path.contains("api/db/")) {
                String sub = "";
                if (path.contains("api/internal/pd/databases/")) {
                    sub = path.substring(path.indexOf("api/internal/pd/databases/") + 26);
                } else if (path.contains("api/web-auth/databases/")) {
                    sub = path.substring(path.indexOf("api/web-auth/databases/") + 23);
                } else if (path.contains("api/auth/databases/")) {
                    sub = path.substring(path.indexOf("api/auth/databases/") + 19);
                } else if (path.startsWith("api/db/")) {
                    sub = path.substring(7);
                } else {
                    int idx = path.indexOf("/databases/");
                    if (idx != -1) {
                        sub = path.substring(idx + 11);
                    }
                }

                if (!sub.isEmpty()) {
                    dbName = sub.split("/")[0];
                }
            }

            if (dbName != null && !dbName.isEmpty()) {
                String method = requestContext.getMethod();
                // Map structural changes to ADMIN
                if (path.contains("sync-roles") ||
                        (path.contains("collections") && !method.equals("GET")) ||
                        (path.matches(".*/databases/[^/]+$") && !method.equals("GET")) ||
                        (path.matches(".*/db/[^/]+$") && !method.equals("GET"))) {
                    method = "ADMIN";
                }

                // Check access for non-global admins or for database-level security enforcement
                // Although management isGlobalAdmin, we still check hasAccess if they are not
                // the super-user
                // to respect the user's wish for strict database-level role assignment.
                boolean isSuperUser = "super-user".equals(username) ||
                        (user != null && ("super-user".equals(user.profile()) || "management".equals(user.profile())
                                || "admin".equals(user.profile())));

                if (!isSuperUser && !hasAccess(user, username, dbName, method)) {
                    LOG.warnf("Access denied for user %s to database %s (Method: %s). isSuperUser: %s", username,
                            dbName,
                            method, isSuperUser);
                    requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                            .entity("{\"error\":\"Access denied to database: " + dbName + "\"}")
                            .build());
                }
            } else if (path.endsWith("databases") && requestContext.getMethod().equals("POST")) {
                // Explicit check for database creation when no dbName is in path
                if (!isGlobalAdmin) {
                    LOG.warnf("Database creation denied for user %s (not global admin)", username);
                    requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                            .entity("{\"error\":\"Database creation restricted to administrative users.\"}")
                            .build());
                }
            }
        } catch (Exception e) {
            LOG.error("Authentication/Authorization error", e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    private boolean hasAccess(User user, String username, String dbName, String method) {
        if (user == null) {
            user = authService.getUser(username);
        }
        if (user == null) {
            LOG.warnf("User %s not found in AuthService", username);
            return false;
        }

        // super-user profiles have full access to everything (except
        // node stopping, handled in filter)
        if ("super-user".equals(user.profile())) {
            LOG.infof("Access granted (Profile: %s) for %s to %s", user.profile(), username, dbName);
            return true;
        }

        java.util.List<Role> userRoles = authService.getRolesForUser(user);
        LOG.infof("Checking access for %s to %s (%s). Current user roles: %s. DB Roles count: %d",
                username, dbName, method, user.roles(), authService.listRoles().size());

        String requiredPrivilege;
        if ("ADMIN".equals(method))
            requiredPrivilege = "ADMIN";
        else
            requiredPrivilege = method.equals("GET") ? "READ" : "WRITE";

        for (Role role : userRoles) {
            String roleDb = role.database();
            LOG.debugf("Checking role %s (DB: %s) for user %s. Required: %s (Target DB: %s)",
                    role.name(), roleDb, username, requiredPrivilege, dbName);

            if ("_all".equals(roleDb) || dbName.equalsIgnoreCase(roleDb)) {
                java.util.Set<String> privs = role.privileges();
                String roleName = role.name();

                // Direct privilege check (Legacy/Internal)
                if (privs.contains("ADMIN") || privs.contains("SUPER")) {
                    LOG.debugf("Access granted (ADMIN/SUPER privilege) for %s to %s", username, dbName);
                    return true;
                }

                // Predefined role types mapping - check both name and database-prefixed name
                if (roleName.equalsIgnoreCase("admin") ||
                        roleName.equalsIgnoreCase("admin_" + dbName) ||
                        roleName.equalsIgnoreCase("super-user_" + dbName) ||
                        roleName.startsWith("admin_") && roleName.substring(6).equalsIgnoreCase(dbName)) {
                    LOG.debugf("Access granted (Admin Role: %s) for %s to %s", roleName, username, dbName);
                    return true;
                }

                if (!"ADMIN".equals(requiredPrivilege) && privs.contains(requiredPrivilege)) {
                    LOG.debugf("Access granted (Privilege %s) for %s to %s", requiredPrivilege, username, dbName);
                    return true;
                }

                // If asking for ADMIN, only exact admin matches above would have returned true.
                if ("ADMIN".equals(requiredPrivilege))
                    continue;

                String lowerRoleName = roleName.toLowerCase();
                if (method.equals("GET")) {
                    if (lowerRoleName.startsWith("reader") || lowerRoleName.startsWith("writer-reader")
                            || lowerRoleName.startsWith("guest")) {
                        return true;
                    }
                } else {
                    if (lowerRoleName.startsWith("writer-reader") || lowerRoleName.startsWith("writer")) {
                        return true;
                    }
                }
            }
        }
        LOG.infof("No matching role found for user %s with database %s and privilege %s. Checked %d roles.",
                username, dbName, requiredPrivilege, userRoles.size());
        return false;
    }
}
