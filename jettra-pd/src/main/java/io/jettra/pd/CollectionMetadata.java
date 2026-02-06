package io.jettra.pd;

public record CollectionMetadata(String name, String engine, java.util.List<IndexMetadata> indexes,
        java.util.List<RuleMetadata> rules) {
    public CollectionMetadata(String name, String engine) {
        this(name, engine, new java.util.ArrayList<>(), new java.util.ArrayList<>());
    }
}
