package io.jettra.example.ui.service;

import io.jettra.example.ui.client.AuthClient;
import io.jettra.example.ui.model.Role;
import io.jettra.example.ui.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class SecurityService {

    private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(SecurityService.class);

    @Inject
    @RestClient
    AuthClient authClient;

    public List<User> getUsers(String token) {
        try {
            String bearer = token != null ? "Bearer " + token : null;
            List<User> users = authClient.getUsers(bearer);
            LOG.infof("Fetched %d users from PD", users != null ? users.size() : 0);
            return users;
        } catch (Exception e) {
            LOG.error("Failed to fetch users from PD", e);
            return Collections.emptyList();
        }
    }

    public List<Role> getRoles(String token) {
        try {
            String bearer = token != null ? "Bearer " + token : null;
            List<Role> roles = authClient.getRoles(bearer);
            LOG.infof("Fetched %d roles from PD", roles != null ? roles.size() : 0);
            return roles;
        } catch (Exception e) {
            LOG.error("Failed to fetch roles from PD", e);
            return Collections.emptyList();
        }
    }

    public User getUser(String username, String token) {
        try {
            String bearer = token != null ? "Bearer " + token : null;
            return authClient.getUser(username, bearer);
        } catch (Exception e) {
            LOG.error("Failed to fetch user " + username, e);
            return null;
        }
    }
}
