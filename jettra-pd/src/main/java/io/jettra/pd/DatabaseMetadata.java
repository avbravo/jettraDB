package io.jettra.pd;

public record DatabaseMetadata(String name, String storage, String engine,
        java.util.List<CollectionMetadata> collections) {
    public DatabaseMetadata {
        if (engine == null) {
            engine = "Multi-Model";
        }
        if (collections == null) {
            collections = new java.util.ArrayList<>();
        } else if (!(collections instanceof java.util.ArrayList)) {
            collections = new java.util.ArrayList<>(collections);
        }
    }

    public DatabaseMetadata(String name, String storage, String engine) {
        this(name, storage, engine, new java.util.ArrayList<>());
    }
}
