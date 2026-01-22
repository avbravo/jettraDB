package io.jettra.pd;

public record SequenceMetadata(String name, String database, long currentValue, long increment) {
}
