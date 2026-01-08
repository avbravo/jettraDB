package io.jettra.pd;

public record NodeMetadata(
    String id,
    String address,
    boolean healthy
) {}
