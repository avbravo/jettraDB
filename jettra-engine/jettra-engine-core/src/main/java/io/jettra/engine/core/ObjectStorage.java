package io.jettra.engine.core;

import java.util.Optional;

import io.smallrye.mutiny.Uni;

public interface ObjectStorage {
    Uni<Void> put(String key, byte[] data);
    Uni<Optional<byte[]>> get(String key);
    Uni<Void> delete(String key);
}
