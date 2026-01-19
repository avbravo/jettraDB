package io.jettra.pd.auth;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;

@ApplicationScoped
public class TokenUtils {

    public static String generateToken(String username, Set<String> roles) {
        var builder = Jwt.issuer("https://jettra.io/issuer")
                .upn(username);
        if (roles != null && !roles.isEmpty()) {
            builder.groups(roles);
        }
        return builder
                .expiresIn(3600) // 1 hour
                .sign();
    }

    public boolean validateToken(String token) {
        return token != null && token.split("\\.").length == 3;
    }

    public String getUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2)
                return null;
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            // Simple JSON parsing to find "upn":"username"
            if (payload.contains("\"upn\":\"")) {
                int start = payload.indexOf("\"upn\":\"") + 7;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
