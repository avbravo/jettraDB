package io.jettra.engine.graph;

import io.jettra.engine.core.AbstractEngine;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-Performance Graph Engine.
 * Implements native graph storage and optimized BFS traversals.
 */
@ApplicationScoped
public class GraphEngine extends AbstractEngine {

    public record Vertex(String id, String label, Map<String, Object> properties) {}
    public record Edge(String fromId, String toId, String label) {}

    private final Map<String, Vertex> vertexStore = new ConcurrentHashMap<>();
    private final Map<String, List<Edge>> outEdges = new ConcurrentHashMap<>();

    public Uni<Void> addVertex(Vertex v) {
        return writeData(1, "graph:v:" + v.id(), v.label())
                .onItem().invoke(() -> {
                    vertexStore.put(v.id(), v);
                    outEdges.putIfAbsent(v.id(), new java.util.concurrent.CopyOnWriteArrayList<>());
                });
    }

    public Uni<Void> addEdge(Edge e) {
        return writeData(1, "graph:e:" + e.fromId() + ":" + e.toId(), e.label())
                .onItem().invoke(() -> {
                    outEdges.computeIfAbsent(e.fromId(), k -> new java.util.concurrent.CopyOnWriteArrayList<>())
                            .add(e);
                });
    }

    /**
     * Optimized K-Step Traversal (BFS).
     */
    public Multi<Vertex> traverse(String startId, int maxDepth) {
        return Multi.createFrom().emitter(emitter -> {
            Set<String> visited = new HashSet<>();
            Queue<String> queue = new LinkedList<>();
            
            queue.add(startId);
            visited.add(startId);
            
            int depth = 0;
            while (!queue.isEmpty() && depth < maxDepth) {
                int levelSize = queue.size();
                for (int i = 0; i < levelSize; i++) {
                    String currentId = queue.poll();
                    Vertex v = vertexStore.get(currentId);
                    if (v != null) emitter.emit(v);

                    List<Edge> edges = outEdges.getOrDefault(currentId, Collections.emptyList());
                    for (Edge edge : edges) {
                        if (!visited.contains(edge.toId())) {
                            visited.add(edge.toId());
                            queue.add(edge.toId());
                        }
                    }
                }
                depth++;
            }
            emitter.complete();
        });
    }
}
