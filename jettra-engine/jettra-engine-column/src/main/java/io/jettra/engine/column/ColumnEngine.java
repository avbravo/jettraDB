package io.jettra.engine.column;

import io.jettra.engine.core.AbstractEngine;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Optimized Columnar Engine for Analytics.
 * Data is stored in columns to maximize CPU cache efficiency and compression.
 */
@ApplicationScoped
public class ColumnEngine extends AbstractEngine {

    private final Map<String, List<Object>> storageMap = new ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicInteger rowCounter = new java.util.concurrent.atomic.AtomicInteger(0);

    public Uni<Void> insert(Map<String, Object> row) {
        return Uni.createFrom().item(() -> {
            int currentRow = rowCounter.getAndIncrement();
            synchronized (this) {
                row.forEach((col, val) -> {
                    storageMap.computeIfAbsent(col, k -> new ArrayList<>(Collections.nCopies(currentRow, null)))
                              .add(val);
                });
                // Ensure all columns reach the same length
                storageMap.values().forEach(list -> {
                    while (list.size() <= currentRow) list.add(null);
                });
            }
            return null;
        }).chain(() -> writeData(0, "col:row:" + rowCounter.get(), row.toString()));
    }

    /**
     * Optimized Sum: Scans only the target column.
     */
    public Uni<Double> sum(String columnName) {
        return Uni.createFrom().item(() -> {
            List<Object> data = storageMap.get(columnName);
            if (data == null) return 0.0;
            return data.stream()
                       .filter(v -> v instanceof Number)
                       .mapToDouble(v -> ((Number) v).doubleValue())
                       .sum();
        });
    }

    /**
     * Columnar Projection: Fetch only needed data.
     */
    public Uni<List<Map<String, Object>>> project(List<String> requiredColumns) {
        return Uni.createFrom().item(() -> {
            List<Map<String, Object>> result = new ArrayList<>();
            int total = rowCounter.get();
            for (int i = 0; i < total; i++) {
                Map<String, Object> row = new HashMap<>();
                for (String col : requiredColumns) {
                    List<Object> data = storageMap.get(col);
                    row.put(col, (data != null && i < data.size()) ? data.get(i) : null);
                }
                result.add(row);
            }
            return result;
        });
    }
}
