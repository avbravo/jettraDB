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

    // Index to find which collection owns a jettraID (per node)
    private final Map<String, String> idToCollection = new ConcurrentHashMap<>();

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

        idToCollection.put(jettraId, collection); // Index it

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
                    try {
                        int currentVersion = (meta == null || meta.isEmpty()) ? 1 : Integer.parseInt(meta);
                        String versionKey = "ver:" + collection + ":" + jettraId + ":" + currentVersion;
                        long groupId = Math.abs(versionKey.hashCode()) % 1024;

                        LOG.infof("Archiving version %d for %s/%s", currentVersion, collection, jettraId);
                        return writeData(groupId, versionKey, json)
                                .onItem()
                                .transformToUni(v -> writeData(groupId, metaKey, String.valueOf(currentVersion + 1)))
                                .onItem().transform(v -> currentVersion);
                    } catch (Exception e) {
                        LOG.error("Error in archiveVersion", e);
                        return Uni.createFrom().failure(e);
                    }
                });
    }

    /**
     * Restores a specific version of a document.
     * Use the string version (e.g. "ver:collection:id:1") or just the number.
     * Here we accept just the version number as string or int.
     */
    public Uni<String> restoreVersion(String collection, String jettraId, String versionStr) {
        LOG.infof("Restore version request for %s/%s, version: %s", collection, jettraId, versionStr);
        String versionKey = "ver:" + collection + ":" + jettraId + ":" + versionStr;

        return readData(versionKey)
                .onItem().transformToUni(oldJson -> {
                    if (oldJson == null || oldJson.isEmpty()) {
                        LOG.warnf("Version key %s not found for restore", versionKey);
                        return Uni.createFrom()
                                .failure(new RuntimeException("Version " + versionStr + " not found in archives"));
                    }

                    // Ensure the document JSON being restored has the current jettraID
                    String restoredJson = oldJson;
                    try {
                        JsonNode node = objectMapper.readTree(oldJson);
                        if (node.isObject()) {
                            ObjectNode obj = (ObjectNode) node;
                            obj.put("jettraID", jettraId);
                            restoredJson = objectMapper.writeValueAsString(obj);
                        }
                    } catch (Exception e) {
                        LOG.warn("Could not re-verify jettraID in restored JSON", e);
                    }

                    return save(collection, jettraId, restoredJson);
                })
                .onFailure().invoke(e -> LOG.error("Failed to restore document version", e));
    }

    /**
     * Retrieves the latest version of a document.
     */
    public Uni<String> findById(String collection, String jettraId) {
        return findById(collection, jettraId, false);
    }

    public Uni<String> findById(String collection, String jettraId, boolean resolveRefs) {
        String key = "doc:" + collection + ":" + jettraId;
        if (documentCache.containsKey(key)) {
            String json = documentCache.get(key);
            return resolveRefs ? resolveDocumentRefs(json) : Uni.createFrom().item(json);
        }
        return readData(key)
                .onItem().invoke(val -> {
                    if (val != null && !val.isEmpty())
                        documentCache.put(key, val);
                })
                .onItem().transformToUni(json -> {
                    if (json == null || json.isEmpty())
                        return Uni.createFrom().item("");
                    return resolveRefs ? resolveDocumentRefs(json) : Uni.createFrom().item(json);
                });
    }

    private Uni<String> resolveDocumentRefs(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return resolveRefsRecursively(node).onItem().transform(n -> n.toString());
        } catch (Exception e) {
            LOG.error("Failed to parse JSON for resolution", e);
            return Uni.createFrom().item(json);
        }
    }

    private Uni<JsonNode> resolveRefsRecursively(JsonNode node) {
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            if (obj.has("jettraID") && obj.size() == 1) {
                // Potential reference! (Object with only jettraID)
                String rid = obj.get("jettraID").asText();
                String col = idToCollection.get(rid);
                if (col != null) {
                    return findById(col, rid, false) // Fetch without further recursion for now
                            .onItem().transform(refJson -> {
                                try {
                                    return objectMapper.readTree(refJson);
                                } catch (Exception e) {
                                    return obj;
                                }
                            });
                }
            }

            // Normal object, recurse through fields
            List<Uni<Void>> unis = new ArrayList<>();
            obj.fields().forEachRemaining(entry -> {
                unis.add(resolveRefsRecursively(entry.getValue())
                        .onItem().invoke(resolved -> obj.set(entry.getKey(), resolved))
                        .onItem().ignore().andContinueWithNull());
            });

            if (unis.isEmpty())
                return Uni.createFrom().item(obj);
            return Uni.combine().all().unis(unis).discardItems().onItem().transform(v -> obj);

        } else if (node.isArray()) {
            ArrayNode arr = (ArrayNode) node;
            List<Uni<Void>> unis = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                final int index = i;
                unis.add(resolveRefsRecursively(arr.get(i))
                        .onItem().invoke(resolved -> arr.set(index, resolved))
                        .onItem().ignore().andContinueWithNull());
            }
            if (unis.isEmpty())
                return Uni.createFrom().item(arr);
            return Uni.combine().all().unis(unis).discardItems().onItem().transform(v -> arr);
        }
        return Uni.createFrom().item(node);
    }

    /**
     * Resolves a reference using jettraID.
     * This handles the requirement that references are made using the physical
     * address (bucket) in jettraID.
     */
    public Uni<String> resolveReference(String collection, String referenceJettraId) {
        LOG.debugf("Resolving reference: %s", referenceJettraId);
        // jettraID format bucket#uuid allows future direct bucket-level routing
        return findById(collection, referenceJettraId);
    }

    /**
     * Lists all versions of a document.
     * Note: In a real implementation this would use a prefix scan on
     * 'ver:{collection}:{jettraId}:'
     */
    public Multi<String> getDocumentVersions(String collection, String jettraId) {
        String metaKey = "meta:" + collection + ":" + jettraId + ":vcount";
        return readData(metaKey).onItem().transformToMulti(meta -> {
            int count = (meta == null || meta.isEmpty()) ? 1 : Integer.parseInt(meta);
            List<String> keys = new ArrayList<>();
            // count is the NEXT version to be archived, so archive versions are 1 to
            // count-1
            for (int i = 1; i < count; i++) {
                keys.add("ver:" + collection + ":" + jettraId + ":" + i);
            }
            if (keys.isEmpty()) {
                return Multi.createFrom().empty();
            }
            return Multi.createFrom().iterable(keys)
                    .onItem().transformToUniAndMerge(this::readData)
                    .filter(json -> json != null && !json.isEmpty());
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
                                if (t.asText().equalsIgnoreCase(tag))
                                    return true;
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
     * Lists documents in a collection with pagination and optional search.
     */
    public Multi<String> findAll(String collection, int page, int size, String search) {
        return findAll(collection, page, size, search, false);
    }

    public Multi<String> findAll(String collection, int page, int size, String search, boolean resolveRefs) {
        int skip = (page - 1) * size;
        return Multi.createFrom().iterable(documentCache.entrySet())
                .filter(e -> {
                    String key = e.getKey();
                    if (!key.startsWith("doc:" + collection + ":"))
                        return false;
                    if (search != null && !search.isEmpty()) {
                        return e.getValue().toLowerCase().contains(search.toLowerCase());
                    }
                    return true;
                })
                .skip().first(skip)
                .select().first(size)
                .onItem().transformToUniAndConcatenate(e -> {
                    if (resolveRefs) {
                        return resolveDocumentRefs(e.getValue());
                    }
                    return Uni.createFrom().item(e.getValue());
                });
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
