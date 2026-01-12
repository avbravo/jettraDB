package io.jettra.driver;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.smallrye.mutiny.Uni;

public class JettraReactiveClient implements JettraClient {
    private static final Logger LOG = Logger.getLogger(JettraReactiveClient.class.getName());
    private final String pdAddress;
    private final String authToken;
    private final HttpClient httpClient;

    public JettraReactiveClient(String pdAddress, String authToken) {
        this.pdAddress = pdAddress;
        this.authToken = authToken;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public Uni<Void> save(String collection, Object document) {
        LOG.log(Level.INFO, "Saving document to {0}", collection);
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Object> findById(String collection, String id) {
        LOG.log(Level.INFO, "Finding document {0} in {1}", new Object[]{id, collection});
        return Uni.createFrom().item(null);
    }

    @Override
    public Uni<Void> delete(String collection, String id) {
        LOG.log(Level.INFO, "Deleting document {0} from {1}", new Object[]{id, collection});
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Long> count(String collection) {
        LOG.log(Level.INFO, "Counting documents in {0}", collection);
        return Uni.createFrom().item(0L);
    }

    @Override
    public Uni<Void> createDatabase(String name, String storage) {
        return createDatabase(name, storage, "Multi-Model");
    }

    @Override
    public Uni<Void> createDatabase(String name, String storage, String engine) {
        LOG.log(Level.INFO, "Creating database {0} at {1} [Engine: {2}, Storage: {3}] [Auth: {4}]", new Object[]{name, pdAddress, engine, storage, authToken});
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Void> deleteDatabase(String name) {
        LOG.log(Level.INFO, "Deleting database {0} from {1} [Auth: {2}]", new Object[]{name, pdAddress, authToken});
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<List<String>> listDatabases() {
        LOG.log(Level.INFO, "Listing databases from {0} [Auth: {1}]", new Object[]{pdAddress, authToken});
        return Uni.createFrom().item(java.util.Collections.emptyList());
    }

    @Override
    public Uni<List<NodeInfo>> listNodes() {
        LOG.log(Level.INFO, "Listing cluster nodes from {0} [Auth: {1}]", new Object[]{pdAddress, authToken});
        
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/internal/pd/nodes"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transform(response -> {
            if (response.statusCode() == 200) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    return mapper.readValue(response.body(), new com.fasterxml.jackson.core.type.TypeReference<List<NodeInfo>>() {});
                } catch (java.io.IOException e) {
                    LOG.log(Level.SEVERE, "Failed to parse nodes response: {0}", e.getMessage());
                    return List.of();
                }
            } else {
                LOG.log(Level.WARNING, "Failed to list nodes. Status: {0}", response.statusCode());
                return List.of();
            }
        });
    }

    // Specific Vector Engine Method
    public Uni<List<String>> searchVector(float[] query, int k) {
        LOG.info("Performing vector similarity search...");
        return Uni.createFrom().item(List.of("result-1", "result-2"));
    }

    // Specific Graph Engine Method
    public Uni<List<String>> traverseGraph(String startId, int depth) {
        LOG.log(Level.INFO, "Traversing graph from {0}", startId);
        return Uni.createFrom().item(List.of("vertex-a", "vertex-b"));
    }

    @Override
    public String connectionInfo() {
        return String.format("Connected to %s [Token: %s]", pdAddress, (authToken != null && !authToken.isEmpty()) ? "Present" : "None");
    }
}
