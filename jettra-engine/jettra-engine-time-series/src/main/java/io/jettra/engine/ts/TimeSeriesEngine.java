package io.jettra.engine.ts;

import io.jettra.engine.core.AbstractEngine;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

@ApplicationScoped
public class TimeSeriesEngine extends AbstractEngine {

    public record SeriesPoint(Instant ts, double val) {}

    private final Map<String, ConcurrentSkipListMap<Instant, Double>> metrics = new java.util.concurrent.ConcurrentHashMap<>();

    public Uni<Void> add(String metric, double value) {
        Instant now = Instant.now();
        return writeData(0, "ts:" + metric + ":" + now.toEpochMilli(), String.valueOf(value))
                .onItem().invoke(() -> metrics.computeIfAbsent(metric, k -> new ConcurrentSkipListMap<>())
                                              .put(now, value));
    }

    /**
     * Optimized Range Query with simple downsampling.
     */
    public Uni<List<SeriesPoint>> getRange(String metric, Instant start, Instant end) {
        return Uni.createFrom().item(() -> {
            ConcurrentSkipListMap<Instant, Double> tsMap = metrics.get(metric);
            if (tsMap == null) return Collections.emptyList();
            
            return tsMap.subMap(start, true, end, true)
                        .entrySet().stream()
                        .map(e -> new SeriesPoint(e.getKey(), e.getValue()))
                        .toList();
        });
    }
}
