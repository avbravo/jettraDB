package io.jettra.engine.document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.jettra.engine.core.AbstractEngine;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Enhanced Document Engine for JettraDB.
 * Supports:
 * - jettraID (bucket-aware unique identifiers)
 * - Version management
 * - Embedded and referenced documents
 * - JSON with tag support
 */
@ApplicationScoped
public class DocumentEngine extends AbstractEngine {
    private static final Logger LOG = Logger.getLogger(DocumentEngine.class);

    @Inject
    ObjectMapper objectMapper;

    // Cache for documents (latest version)
    private final Map<String, String> documentCache = new ConcurrentHashMap<>();

    /**
     * Generates a unique jettraID.
     * Format: {bucketId}#{uuid}
     * Identifies exactly where the document is physically located.
     */
    public String generateJettraId(String bucketId) {
        return String.format("%s#%s", bucketId, UUID.randomUUID().toString().replace("-", ""));
    }

    /**
     * Saves a document with automatic versioning.
     */
    public Uni<String> save(String collection, String jettraId, String json) {
        LOG.infof("Save request for %s/%s", collection, jettraId);
        
        return findById(collection, jettraId)
                .onItem().transformToUni(existingJson -> {
                    if (existingJson != null && !existingJson.isEmpty()) {
                        return archiveVersion(collection, jettraId, existingJson)
                                .onItem().transformToUni(v -> persistDocument(collection, jettraId, json, v + 1));
                    }
                    return persistDocument(collection, jettraId, json, 1);
                });
    }

    private Uni<String> persistDocument(String collection, String jettraId, String json, int version) {
        String key = "doc:" + collection + ":" + jettraId;
        long groupId = Math.abs(key.hashCode()) % 1024;

        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.isObject()) {
                ObjectNode obj = (ObjectNode) node;
                obj.put("jettraID", jettraId);
                obj.put("_version", version);
                obj.put("_lastModified", Instant.now().toString());
                
                // Ensure tags field exists for "enriched JSON" requirement
                if (!obj.has("_tags")) {
                    obj.putArray("_tags");
                }
                
                json = objectMapper.writeValueAsString(obj);
            }
        } catch (Exception e) {
            LOG.error("Failed to enrich document JSON", e);
        }

        String finalJson = json;
        return writeData(groupId, key, finalJson)
                .onItem().transform(v -> {
                    documentCache.put(key, finalJson);
                    return jettraId;
                });
    }

    private Uni<Integer> archiveVersion(String collection, String jettraId, String json) {
        String metaKey = "meta:" + collection + ":" + jettraId + ":vcount";
        return readData(metaKey)
                .onItem().transformToUni(meta -> {
                    int currentVersion = meta.isEmpty() ? 1 : Integer.parseInt(meta);
                    String versionKey = "ver:" + collection + ":" + jettraId + ":" + currentVersion;
                    long groupId = Math.abs(versionKey.hashCode()) % 1024;
                    
                    return writeData(groupId, versionKey, json)
                            .onItem().transformToUni(v -> writeData(groupId, metaKey, String.valueOf(currentVersion + 1)))
                            .onItem().transform(v -> currentVersion);
                });
    }

    /**
     * Retrieves the latest version of a document.
     */
    public Uni<String> findById(String collection, String jettraId) {
        String key = "doc:" + collection + ":" + jettraId;
        if (documentCache.containsKey(key)) {
            return Uni.createFrom().item(documentCache.get(key));
        }
        return readData(key).onItem().invoke(val -> {
            if (val != null && !val.isEmpty()) documentCache.put(key, val);
        });
    }

    /**
     * Resolves a reference using jettraID.
     * This handles the requirement that references are made using the physical address (bucket) in jettraID.
     */
    public Uni<String> resolveReference(String collection, String referenceJettraId) {
        LOG.debugf("Resolving reference: %s", referenceJettraId);
        // jettraID format bucket#uuid allows future direct bucket-level routing
        return findById(collection, referenceJettraId);
    }

    /**
     * Lists all versions of a document.
     * Note: In a real implementation this would use a prefix scan on 'ver:{collection}:{jettraId}:'
     */
    public Multi<String> getDocumentVersions(String collection, String jettraId) {
        String metaKey = "meta:" + collection + ":" + jettraId + ":vcount";
        return readData(metaKey).onItem().transformToMulti(meta -> {
            int count = meta.isEmpty() ? 0 : Integer.parseInt(meta);
            List<String> keys = new ArrayList<>();
            for (int i = 1; i < count; i++) {
                keys.add("ver:" + collection + ":" + jettraId + ":" + i);
            }
            // Add current
            return Multi.createFrom().iterable(keys)
                    .onItem().transformToUniAndMerge(this::readData);
        });
    }

    /**
     * Search by tags in the enriched JSON.
     */
    public Multi<String> findByTag(String collection, String tag) {
        return Multi.createFrom().iterable(documentCache.entrySet())
                .filter(e -> e.getKey().startsWith("doc:" + collection + ":"))
                .filter(e -> {
                    try {
                        JsonNode node = objectMapper.readTree(e.getValue());
                        if (node.has("_tags") && node.get("_tags").isArray()) {
                            ArrayNode tags = (ArrayNode) node.get("_tags");
                            for (JsonNode t : tags) {
                                if (t.asText().equalsIgnoreCase(tag)) return true;
                            }
                        }
                    } catch (Exception ex) {
                        return false;
                    }
                    return false;
                })
                .onItem().transform(Map.Entry::getValue);
    }

    /**
     * Lists all documents in a collection.
     */
    public Multi<String> findAll(String collection) {
        return Multi.createFrom().iterable(documentCache.entrySet())
                .filter(e -> e.getKey().startsWith("doc:" + collection + ":"))
                .onItem().transform(Map.Entry::getValue);
    }

    public Uni<Void> delete(String collection, String jettraId) {
        String key = "doc:" + collection + ":" + jettraId;
        documentCache.remove(key);
        return storage.delete(key);
    }

    /**
     * Support for embedded documents is implicit in JSON storage.
     * This method demonstrates how to extract an embedded part if needed.
     */
    public Uni<JsonNode> getEmbedded(String collection, String jettraId, String path) {
        return findById(collection, jettraId)
                .onItem().transform(json -> {
                    try {
                        JsonNode node = objectMapper.readTree(json);
                        return node.at(path);
                    } catch (Exception e) {
                        return null;
                    }
                });
    }
}
