package io.jettra.example.ui.model;

public record Sequence(String name, String database, long currentValue, long increment) {
}
