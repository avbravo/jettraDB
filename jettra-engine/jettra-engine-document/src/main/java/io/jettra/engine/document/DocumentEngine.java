package io.jettra.engine.document;

import io.jettra.engine.core.AbstractEngine;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Optimized Document Engine for JettraDB.
 * Supports indexing, fast local caching, and reactive multi-model persistence.
 */
@ApplicationScoped
public class DocumentEngine extends AbstractEngine {
    private static final Logger LOG = Logger.getLogger(DocumentEngine.class);

    // High-performance MemTable for documents
    private final Map<String, String> documentMap = new ConcurrentHashMap<>();
    
    // In-memory index simulated: Map<Collection, Map<Field, Map<Value, List<ID>>>>
    private final Map<String, Map<String, Map<String, List<String>>>> indexes = new ConcurrentHashMap<>();

    public Uni<Void> save(String collection, String id, String json) {
        LOG.infof("Inserting document into %s: %s", collection, id);
        String key = collection + ":" + id;
        long groupId = Math.abs(key.hashCode()) % 1024;
        
        return writeData(groupId, key, json)
                .onItem().invoke(() -> {
                    documentMap.put(key, json);
                    updateIndexes(collection, id, json);
                });
    }

    public Uni<String> findById(String collection, String id) {
        String key = collection + ":" + id;
        if (documentMap.containsKey(key)) {
            return Uni.createFrom().item(documentMap.get(key));
        }
        return readData(key).onItem().invoke(val -> {
            if (!val.isEmpty()) documentMap.put(key, val);
        });
    }

    public Multi<String> findAll(String collection) {
        return Multi.createFrom().iterable(documentMap.entrySet())
                .filter(e -> e.getKey().startsWith(collection + ":"))
                .onItem().transform(Map.Entry::getValue);
    }

    /**
     * Optimized Query by Field using simulated indexes.
     */
    public Multi<String> findByField(String collection, String field, String value) {
        LOG.infof("Querying %s by %s = %s", collection, field, value);
        
        Map<String, List<String>> fieldIndex = indexes.getOrDefault(collection, Map.of()).getOrDefault(field, Map.of());
        List<String> ids = fieldIndex.getOrDefault(value, List.of());
        
        return Multi.createFrom().iterable(ids)
                .onItem().transformToUniAndMerge(id -> findById(collection, id));
    }

    private void updateIndexes(String collection, String id, String json) {
        // Simplified index logic: find fields in JSON (simulated)
        // In a real impl, use a JSON parser to extract fields
        if (json.contains("\"status\"")) {
            String status = json.contains("\"OK\"") ? "OK" : "ERROR";
            indexes.computeIfAbsent(collection, k -> new ConcurrentHashMap<>())
                   .computeIfAbsent("status", k -> new ConcurrentHashMap<>())
                   .computeIfAbsent(status, k -> new java.util.ArrayList<>())
                   .add(id);
        }
    }
}
