package io.jettra.pd.auth;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AuthService {
    private static final Logger LOG = Logger.getLogger(AuthService.class);
    // In-memory user store. In production, use a database or encoded repo.
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public AuthService() {
        // Initialize default admin
        users.put("admin", new User("admin", "adminadmin", Set.of("admin", "user"), true));
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
}
