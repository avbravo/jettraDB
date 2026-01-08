package io.jettra.engine.vector;

import io.jettra.engine.core.AbstractEngine;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
public class VectorEngine extends AbstractEngine {

    public record VectorEntry(String id, float[] embedding, String metadata) {}
    public record SearchMatch(String id, double distance, String metadata) {}

    private final List<VectorEntry> vectorBuffer = new CopyOnWriteArrayList<>();

    public Uni<Void> insert(VectorEntry entry) {
        return writeData(0, "vec:" + entry.id(), entry.metadata())
                .onItem().invoke(() -> vectorBuffer.add(entry));
    }

    /**
     * Optimized KNN Search using Cosine Similarity.
     */
    public Uni<List<SearchMatch>> search(float[] query, int k) {
        return Uni.createFrom().item(() -> {
            return vectorBuffer.stream()
                .map(entry -> new SearchMatch(entry.id(), cosineSimilarity(query, entry.embedding()), entry.metadata()))
                .sorted((a, b) -> Double.compare(b.distance(), a.distance())) // Higher similarity first
                .limit(k)
                .toList();
        });
    }

    private double cosineSimilarity(float[] v1, float[] v2) {
        double dot = 0.0, n1 = 0.0, n2 = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            n1 += v1[i] * v1[i];
            n2 += v2[i] * v2[i];
        }
        return dot / (Math.sqrt(n1) * Math.sqrt(n2));
    }
}
