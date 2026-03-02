package io.jettra.pd.auth;

import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class AuthServiceTest {

    @Test
    public void testDefaultCredentials() {
        AuthService authService = new AuthService();

        // Test super-user with new password
        User superUser = authService.authenticate("super-user", "superuser");
        assertNotNull(superUser, "super-user should authenticate with password 'superuser'");
        assertEquals("super-user", superUser.username());
        assertEquals("super-user", superUser.profile());

        // Test super-user with old password (should fail)
        User failedSuperUser = authService.authenticate("super-user", "superuser-jettra");
        assertNull(failedSuperUser, "super-user should NOT authenticate with old password 'superuser-jettra'");

        // Test admin with password 'superuser'
        User adminUser = authService.authenticate("admin", "superuser");
        assertNotNull(adminUser, "admin should authenticate with password 'superuser'");
        assertEquals("admin", adminUser.username());
        assertEquals("super-user", adminUser.profile());
    }
}
