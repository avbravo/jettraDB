package io.jettra.store;

import io.jettra.engine.core.ObjectStorage;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LsmObjectStore implements ObjectStorage {
    private static final Logger LOG = Logger.getLogger(LsmObjectStore.class);
    
    // Optimized MemTable with size tracking for automated flushing
    private final Map<String, byte[]> memTable = new ConcurrentHashMap<>();
    private final AtomicLong currentSize = new AtomicLong(0);
    private static final long MAX_MEMTABLE_SIZE = 64 * 1024 * 1024; // 64MB

    @Override
    public Uni<Void> put(String key, byte[] data) {
        return Uni.createFrom().item(() -> {
            byte[] old = memTable.put(key, data);
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
            return null;
        });
    }
}
