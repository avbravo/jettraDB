package io.jettra.driver;

import io.smallrye.mutiny.Uni;

public interface JettraClient {
    Uni<Void> save(String collection, Object document);
    Uni<Object> findById(String collection, String id);
    Uni<Void> delete(String collection, String id);
    Uni<Long> count(String collection);
}
