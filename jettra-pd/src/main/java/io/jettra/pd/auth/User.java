package io.jettra.pd.auth;

import java.util.Set;

public record User(
        String username,
        String password, // Storing plain/hash (in logic)
        Set<String> roles,
        boolean forcePasswordChange) {
}
