package io.jettra.store;

import io.smallrye.mutiny.Uni;
import java.util.Optional;

public interface ObjectStorage {
    Uni<Void> put(String key, byte[] data);
    Uni<Optional<byte[]>> get(String key);
    Uni<Void> delete(String key);
}
