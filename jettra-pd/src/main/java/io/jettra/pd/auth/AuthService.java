package io.jettra.pd.auth;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuthService {
    private static final Logger LOG = Logger.getLogger(AuthService.class);
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Role> roles = new ConcurrentHashMap<>();

    public AuthService() {
        // Initialize default roles (Application level profiles are just strings in
        // profile field)
        // Global roles
        roles.put("super-user", new Role("super-user", "_all", Set.of("SUPER", "ADMIN", "READ", "WRITE")));
        roles.put("management", new Role("management", "_all", Set.of("ADMIN", "READ", "WRITE")));
        roles.put("admin", new Role("admin", "_all", Set.of("ADMIN", "READ", "WRITE")));
        roles.put("read", new Role("read", "_all", Set.of("READ")));
        roles.put("read-write", new Role("read-write", "_all", Set.of("READ", "WRITE")));

        // Initialize default super-user
        users.put("super-user", new User("super-user", "adminadmin", null,
                new java.util.HashSet<>(Set.of("super-user")), "super-user", false));
    }

    public User authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.password().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = users.get(username);
        if (user != null && user.password().equals(oldPassword)) {
            users.put(username, new User(username, newPassword, user.email(), user.roles(), user.profile(), false));
            LOG.infof("Password changed for user: %s", username);
            return true;
        }
        return false;
    }

    // User Management
    public User getUser(String username) {
        return users.get(username);
    }

    public void createUser(User user) {
        // Prevent altering super-user privileges if somehow attempted through API
        if ("super-user".equals(user.username())) {
            LOG.warn("Attempt to create/overwrite super-user via API blocked.");
            return;
        }
        if ("super-user".equals(user.profile()) && !"super-user".equals(user.username())) {
            LOG.warnf("Attempt to assign super-user profile to non-super-user user '%s' blocked.", user.username());
            return;
        }
        users.put(user.username(), user);
        LOG.infof("User created: %s with profile: %s", user.username(), user.profile());
    }

    public void deleteUser(String username) {
        if ("super-user".equals(username)) {
            LOG.warn("Attempt to delete super-user blocked.");
            return;
        }
        users.remove(username);
        LOG.infof("User deleted: %s", username);
    }

    public java.util.Collection<User> listUsers() {
        return users.values();
    }

    public void updateUser(User user) {
        if ("super-user".equals(user.username())) {
            // Protect super-user from profile change through general update, but allow role
            // updates
            User existing = users.get("super-user");
            if (existing != null) {
                String password = (user.password() == null || user.password().isEmpty()) ? existing.password()
                        : user.password();
                users.put("super-user", new User("super-user", password, user.email(), user.roles(), "super-user",
                        user.forcePasswordChange()));
                LOG.info("Super-user updated (profile protected, roles updated)");
                return;
            }
        }

        if (users.containsKey(user.username())) {
            User existing = users.get(user.username());
            String password = (user.password() == null || user.password().isEmpty()) ? existing.password()
                    : user.password();
            String profile = user.profile();
            if ("super-user".equals(profile) && !"super-user".equals(user.username())) {
                LOG.warnf("Attempt to update user '%s' with super-user profile blocked.", user.username());
                profile = existing.profile(); // Keep existing profile if they tried to illegally upgrade
            }

            users.put(user.username(),
                    new User(user.username(), password, user.email(), user.roles(), profile,
                            user.forcePasswordChange()));
            LOG.infof("User updated: %s", user.username());
        }
    }

    // Role Management
    public void createRole(Role role) {
        roles.put(role.name(), role);
        LOG.infof("Role created: %s for database: %s", role.name(), role.database());
    }

    public void updateRole(Role role) {
        if (roles.containsKey(role.name())) {
            roles.put(role.name(), role);
            LOG.infof("Role updated: %s", role.name());
        }
    }

    public void deleteRole(String name) {
        roles.remove(name);
        LOG.infof("Role deleted: %s", name);
    }

    public java.util.Collection<Role> listRoles() {
        return roles.values();
    }

    public Role getRole(String name) {
        return roles.get(name);
    }

    public java.util.List<Role> getRolesForUser(User user) {
        return user.roles().stream()
                .map(roles::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * Set up default permissions for a newly created database.
     * 1. Assigns 'super-user_<dbName>' role to 'super-user' user.
     * 2. Assigns 'admin_<dbName>' role to the creator.
     */
    public void setupDefaultDatabaseRoles(String dbName, String creator) {
        // 1. Create and Assign super-user role for this DB
        String suRoleName = "super-user_" + dbName;
        createRole(new Role(suRoleName, dbName, Set.of("SUPER", "ADMIN", "READ", "WRITE")));

        // Ensure the primary 'super-user' account always gets it
        assignRoleToUser("super-user", suRoleName);

        // 2. Assign roles to creator if they are not the super-user
        if (creator != null && !"super-user".equals(creator)) {
            String adminRoleName = "admin_" + dbName;
            createRole(new Role(adminRoleName, dbName, Set.of("ADMIN", "READ", "WRITE")));
            assignRoleToUser(creator, adminRoleName);
        }
    }

    private void assignRoleToUser(String username, String roleName) {
        User user = users.get(username);
        if (user != null) {
            Set<String> roles = user.roles();
            Set<String> updatedRoles = new java.util.HashSet<>(roles != null ? roles : Set.of());
            updatedRoles.add(roleName);
            updateUser(new User(user.username(), user.password(), user.email(), updatedRoles, user.profile(),
                    user.forcePasswordChange()));
            LOG.infof("Role %s assigned to user %s", roleName, username);
        }
    }

    public void renameDatabaseRoles(String oldName, String newName) {
        if (oldName == null || newName == null || oldName.equals(newName))
            return;

        LOG.infof("Renaming database roles from %s to %s", oldName, newName);
        java.util.List<Role> dbRoles = roles.values().stream()
                .filter(r -> oldName.equals(r.database()))
                .toList();

        for (Role oldRole : dbRoles) {
            String rolePrefix = oldRole.name();
            if (rolePrefix.endsWith("_" + oldName)) {
                rolePrefix = rolePrefix.substring(0, rolePrefix.length() - (oldName.length() + 1));
            }
            String newRoleName = rolePrefix + "_" + newName;

            createRole(new Role(newRoleName, newName, oldRole.privileges()));

            // Update users
            for (User user : users.values()) {
                if (user.roles() != null && user.roles().contains(oldRole.name())) {
                    java.util.Set<String> updatedRoles = new java.util.HashSet<>(user.roles());
                    updatedRoles.remove(oldRole.name());
                    updatedRoles.add(newRoleName);
                    updateUser(new User(user.username(), user.password(), user.email(), updatedRoles, user.profile(),
                            user.forcePasswordChange()));
                }
            }
            deleteRole(oldRole.name());
        }
    }

    public void syncDatabaseRoles(String dbName, java.util.Map<String, String> userRoleMapping) {
        LOG.infof("Syncing database roles for %s: %s", dbName, userRoleMapping);

        // 1. Identify all roles belonging to this database
        java.util.Set<String> dbRoleNames = roles.values().stream()
                .filter(r -> dbName.equals(r.database()))
                .map(Role::name)
                .collect(java.util.stream.Collectors.toSet());

        // 2. Clear these roles from ALL users first (except 'super-user' who is
        // protected)
        users.values().forEach(user -> {
            if (user.username().equals("super-user"))
                return;

            if (user.roles() == null)
                return;

            java.util.Set<String> updatedRoles = new java.util.HashSet<>(user.roles());
            if (updatedRoles.removeIf(dbRoleNames::contains)) {
                updateUser(new User(user.username(), user.password(), user.email(), updatedRoles, user.profile(),
                        user.forcePasswordChange()));
            }
        });

        // 3. Apply the new mapping
        userRoleMapping.forEach((username, roleType) -> {
            if ("none".equals(roleType) || "super-user".equals(roleType) || "denied".equals(roleType))
                return; // super-user is handled separately/protected. denied means no role.

            String roleName = roleType + "_" + dbName;

            // Ensure role exists
            if (!roles.containsKey(roleName)) {
                java.util.Set<String> privileges = switch (roleType) {
                    case "admin" -> java.util.Set.of("ADMIN", "READ", "WRITE");
                    case "read-write" -> java.util.Set.of("READ", "WRITE");
                    case "read" -> java.util.Set.of("READ");
                    default -> java.util.Set.of("READ");
                };
                createRole(new Role(roleName, dbName, privileges));
            }

            if (!"super-user".equals(username)) {
                assignRoleToUser(username, roleName);
            }
        });

        // 4. Ensure super-user ALWAYS has super-user role for this database
        String suRoleName = "super-user_" + dbName;
        if (!roles.containsKey(suRoleName)) {
            createRole(new Role(suRoleName, dbName, java.util.Set.of("SUPER", "ADMIN", "READ", "WRITE")));
        }
        assignRoleToUser("super-user", suRoleName);
        LOG.infof("Sync database roles completed for %s", dbName);
    }

}
