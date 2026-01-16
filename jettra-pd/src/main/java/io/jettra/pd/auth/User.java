package io.jettra.pd.auth;

import java.util.Set;

public record User(
                String username,
                String password, // Storing plain/hash (in logic)
                String email,
                Set<String> roles,
                boolean forcePasswordChange) {
}
