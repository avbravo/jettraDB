package io.jettra.store;

import io.jettra.engine.core.ObjectStorage;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class LsmObjectStore implements ObjectStorage {
    private static final Logger LOG = Logger.getLogger(LsmObjectStore.class);
    
    // Optimized MemTable with size tracking for automated flushing
    private final Map<String, byte[]> memTable = new ConcurrentHashMap<>();
    private final AtomicLong currentSize = new AtomicLong(0);
    private static final long MAX_MEMTABLE_SIZE = 64 * 1024 * 1024; // 64MB

    @Inject
    ObjectMapper objectMapper;

    private static final String DATA_DIR = "data/store";
    private static final String AOF_FILE = DATA_DIR + "/aof.log";

    @PostConstruct
    void init() {
        loadAof();
    }

    private void loadAof() {
        try {
            File file = new File(AOF_FILE);
            if (file.exists()) {
                java.util.List<String> lines = Files.readAllLines(file.toPath());
                for (String line : lines) {
                    try {
                        AofEntry entry = objectMapper.readValue(line, AofEntry.class);
                        if ("PUT".equals(entry.operation)) {
                            memTable.put(entry.key, java.util.Base64.getDecoder().decode(entry.value));
                        } else if ("DELETE".equals(entry.operation)) {
                            memTable.remove(entry.key);
                        }
                    } catch (Exception parseEx) {
                        LOG.warn("Skipping malformed AOF line: " + line);
                    }
                }
                LOG.infof("Restored %d keys from AOF.", memTable.size());
            }
        } catch (IOException e) {
            LOG.error("Failed to load AOF from disk", e);
        }
    }

    private void appendToAof(String operation, String key, byte[] value) {
        String collection = extractCollection(key);
        if (collection != null && "STORE".equalsIgnoreCase(PDConnector.collectionStorageTypes.get(collection))) {
            try {
                Path dirPath = Paths.get(DATA_DIR);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                
                AofEntry entry = new AofEntry();
                entry.operation = operation;
                entry.key = key;
                entry.value = value != null ? java.util.Base64.getEncoder().encodeToString(value) : null;
                
                String json = objectMapper.writeValueAsString(entry) + System.lineSeparator();
                Files.write(Paths.get(AOF_FILE), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                LOG.error("Failed to write to AOF", e);
            }
        }
    }

    private String extractCollection(String key) {
        // e.g., doc:my_collection:id OR meta:my_collection:id
        String[] parts = key.split(":");
        if (parts.length >= 3) {
            return parts[1];
        }
        return null;
    }

    public static class AofEntry {
        public String operation;
        public String key;
        public String value;
    }

    @Override
    public Uni<Void> put(String key, byte[] data) {
        return Uni.createFrom().item(() -> {
            byte[] old = memTable.put(key, data);
            appendToAof("PUT", key, data);
            long delta = data.length - (old != null ? old.length : 0);
            
            if (currentSize.addAndGet(delta) > MAX_MEMTABLE_SIZE) {
                flushToSst();
            }
            return null;
        });
    }

    private synchronized void flushToSst() {
        LOG.info("MemTable limit reached. Flushing to SSTable (Simulation)...");
        // In a real implementation:
        // 1. Move memTable to immutable skip-list
        // 2. Write to disk as sorted file (SST)
        // 3. Clear current memTable
        memTable.clear();
        currentSize.set(0);
    }

    @Override
    public Uni<Optional<byte[]>> get(String key) {
        return Uni.createFrom().item(() -> Optional.ofNullable(memTable.get(key)));
    }

    @Override
    public Uni<Void> delete(String key) {
        return Uni.createFrom().item(() -> {
            memTable.remove(key);
            appendToAof("DELETE", key, null);
            return null;
        });
    }
}
