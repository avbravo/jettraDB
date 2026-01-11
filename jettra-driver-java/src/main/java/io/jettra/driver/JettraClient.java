package io.jettra.driver;

import io.smallrye.mutiny.Uni;

public interface JettraClient {
    Uni<Void> save(String collection, Object document);

    Uni<Object> findById(String collection, String id);

    Uni<Void> delete(String collection, String id);

    Uni<Long> count(String collection);

    Uni<Void> createDatabase(String name, String storage);

    Uni<Void> createDatabase(String name, String storage, String engine);

    Uni<Void> deleteDatabase(String name);

    Uni<java.util.List<String>> listDatabases();
}
