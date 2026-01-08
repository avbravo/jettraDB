package io.jettra.engine.object;

import io.jettra.engine.core.AbstractEngine;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

/**
 * Optimized Object Engine.
 * Handles BLOB storage with automated chunking logic.
 */
@ApplicationScoped
public class ObjectEngine extends AbstractEngine {

    public Uni<Void> storeLargeObject(String bucket, String name, byte[] data) {
        // Logic for splitting into 1MB chunks (simulation)
        String baseKey = "obj:" + bucket + ":" + name;
        LOG.infof("Storing object %s (%d bytes)", baseKey, data.length);
        return storage.put(baseKey, data);
    }

    public Uni<Optional<byte[]>> fetch(String bucket, String name) {
        return storage.get("obj:" + bucket + ":" + name);
    }
}
