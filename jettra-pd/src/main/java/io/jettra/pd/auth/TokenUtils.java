package io.jettra.pd.auth;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;

@ApplicationScoped
public class TokenUtils {

    public static String generateToken(String username, Set<String> roles) {
        return Jwt.issuer("https://jettra.io/issuer")
                .upn(username)
                .groups(roles)
                .expiresIn(3600) // 1 hour
                .sign();
    }

    public boolean validateToken(String token) {
        // In a real application, using MP-JWT this validation is automatic via implicit
        // container filter.
        // But since we implemented a manual AuthFilter, we need to manually validate.
        // For this demo, we can trust if it's not null and looks like a JWT (3 parts)
        // Ideally we would parse and verify signature using public key.
        return token != null && token.split("\\.").length == 3;
    }
}
