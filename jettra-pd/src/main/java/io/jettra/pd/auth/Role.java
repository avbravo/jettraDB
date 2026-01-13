package io.jettra.pd.auth;

import java.util.Set;

public record Role(
        String name,
        String database,
        Set<String> privileges // e.g., "READ", "WRITE", "ADMIN"
) {
}
