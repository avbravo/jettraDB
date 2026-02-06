package io.jettra.pd;

public record RuleMetadata(String name, String condition, String action, boolean active) {
}
