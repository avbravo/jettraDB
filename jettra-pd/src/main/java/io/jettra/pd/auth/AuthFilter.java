package io.jettra.pd.auth;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Inject
    TokenUtils tokenUtils;

    @Inject
    AuthService authService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        if (path.startsWith("api/auth/") || path.startsWith("/api/auth/") ||
                path.startsWith("api/web-auth/") || path.startsWith("/api/web-auth/") ||
                path.startsWith("api/internal/pd/") || path.startsWith("/api/internal/pd/") ||
                path.equals("health") || path.equals("/health")) {
            return; // Allow public and internal endpoints
        }

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

            // Simple authorization check for database paths
            if (path.startsWith("api/db/") || path.startsWith("/api/db/")) {
                String subPath = path.startsWith("/") ? path.substring(8) : path.substring(7);
                if (!subPath.isEmpty()) {
                    String[] parts = subPath.split("/");
                    String dbName = parts[0];

                    String method = requestContext.getMethod();
                    if (!hasAccess(username, dbName, method)) {
                        requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                                .entity("{\"error\":\"Access denied to database: " + dbName + "\"}")
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    private boolean hasAccess(String username, String dbName, String method) {
        // Admin user (system-pd) is always allowed
        if ("admin".equals(username) || "system-pd".equals(username)) {
            return true;
        }

        User user = authService.getUser(username);
        if (user == null) {
            return false;
        }

        java.util.List<Role> userRoles = authService.getRolesForUser(user);
        String requiredPrivilege = method.equals("GET") ? "READ" : "WRITE";

        for (Role role : userRoles) {
            if ("_all".equals(role.database()) || dbName.equals(role.database())) {
                java.util.Set<String> privs = role.privileges();

                // Direct privilege check (Legacy/Internal)
                if (privs.contains("ADMIN") || privs.contains(requiredPrivilege)) {
                    return true;
                }

                // Predefined role types mapping
                String roleName = role.name();
                if (roleName.startsWith("admin")) {
                    return true;
                }

                if (method.equals("GET")) {
                    if (roleName.startsWith("reader") || roleName.startsWith("writer-reader")
                            || roleName.startsWith("guest")) {
                        return true;
                    }
                } else {
                    if (roleName.startsWith("writer-reader")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
