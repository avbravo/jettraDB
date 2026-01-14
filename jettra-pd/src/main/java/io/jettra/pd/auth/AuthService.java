package io.jettra.pd.auth;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AuthService {
    private static final Logger LOG = Logger.getLogger(AuthService.class);
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Role> roles = new ConcurrentHashMap<>();

    public AuthService() {
        // Initialize default roles
        roles.put("admin", new Role("admin", "_all", Set.of("ADMIN", "READ", "WRITE")));
        roles.put("guest", new Role("guest", "_all", Set.of("READ")));
        roles.put("reader", new Role("reader", "_all", Set.of("READ")));
        roles.put("writer-reader", new Role("writer-reader", "_all", Set.of("READ", "WRITE")));

        // Initialize default admin
        users.put("admin", new User("admin", "adminadmin", Set.of("admin"), true));
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
            users.put(username, new User(username, newPassword, user.roles(), false));
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
        users.put(user.username(), user);
        LOG.infof("User created: %s", user.username());
    }

    public void deleteUser(String username) {
        users.remove(username);
        LOG.infof("User deleted: %s", username);
    }

    public java.util.Collection<User> listUsers() {
        return users.values();
    }

    public void updateUser(User user) {
        if (users.containsKey(user.username())) {
            User existing = users.get(user.username());
            // If password is null or empty in the update, keep the existing one
            String password = (user.password() == null || user.password().isEmpty()) ? existing.password()
                    : user.password();
            users.put(user.username(), new User(user.username(), password, user.roles(), user.forcePasswordChange()));
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
     * Assigns 'admin_<dbName>' role to the global 'admin' and the creator.
     */
    public void setupDefaultDatabaseRoles(String dbName, String creator) {
        String roleName = "admin_" + dbName;
        Role dbAdminRole = new Role(roleName, dbName, Set.of("ADMIN", "READ", "WRITE"));
        createRole(dbAdminRole);

        // 1. Assign to global admin
        assignRoleToUser("admin", roleName);

        // 2. Assign to creator (if different from admin)
        if (creator != null && !creator.equals("admin") && !creator.equals("system-pd")) {
            assignRoleToUser(creator, roleName);
        }
    }

    private void assignRoleToUser(String username, String roleName) {
        User user = users.get(username);
        if (user != null) {
            Set<String> updatedRoles = new java.util.HashSet<>(user.roles());
            updatedRoles.add(roleName);
            updateUser(new User(user.username(), user.password(), updatedRoles, user.forcePasswordChange()));
            LOG.infof("Role %s assigned to user %s", roleName, username);
        }
    }
}
