package io.jettra.engine.kv;

import io.jettra.engine.core.AbstractEngine;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class KvEngine extends AbstractEngine {

    public Uni<Void> put(String key, String value) {
        long groupId = Math.abs(key.hashCode()) % 1024;
        return writeData(groupId, "kv:" + key, value);
    }

    public Uni<Optional<String>> get(String key) {
        return readData("kv:" + key)
                .onItem().transform(val -> val.isEmpty() ? Optional.empty() : Optional.of(val));
    }

    /**
     * Optimized Batch Put.
     */
    public Uni<Void> putBatch(Map<String, String> items) {
        return Multi.createFrom().iterable(items.entrySet())
                .onItem().transformToUniAndMerge(e -> put(e.getKey(), e.getValue()))
                .collect().last().replaceWithVoid();
    }

    public Uni<Void> delete(String key) {
        return storage.delete("kv:" + key);
    }
}
