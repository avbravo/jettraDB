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
    private String authToken;
    private final HttpClient httpClient;

    public JettraReactiveClient(String pdAddress, String authToken) {
        this.pdAddress = pdAddress;
        this.authToken = authToken;
        this.httpClient = HttpClient.newHttpClient();
    }

    public JettraReactiveClient(String pdAddress) {
        this(pdAddress, null);
    }

    @Override
    public Uni<Void> save(String collection, Object document) {
        LOG.log(Level.INFO, "Saving document to {0}", collection);
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Object> findById(String collection, String id) {
        LOG.log(Level.INFO, "Finding document {0} in {1}", new Object[] { id, collection });
        return Uni.createFrom().item(null);
    }

    @Override
    public Uni<Void> delete(String collection, String id) {
        LOG.log(Level.INFO, "Deleting document {0} from {1}", new Object[] { id, collection });
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Long> count(String collection) {
        LOG.log(Level.INFO, "Counting documents in {0}", collection);
        return Uni.createFrom().item(0L);
    }

    @Override
    public Uni<Void> createDatabase(String name, String storage) {
        LOG.log(Level.INFO, "Creating database {0} [Storage: {1}]", new Object[] { name, storage });
        return Uni.createFrom().completionStage(() -> {
            String json = String.format("{\"name\": \"%s\", \"storage\": \"%s\"}", name, storage);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/db"))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200 || response.statusCode() == 201)
                return Uni.createFrom().voidItem();
            return Uni.createFrom()
                    .failure(new RuntimeException("Failed to create database. Status: " + response.statusCode()));
        });
    }

    @Override
    public Uni<Void> deleteDatabase(String name) {
        LOG.log(Level.INFO, "Deleting database {0}", name);
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/db/" + name))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200)
                return Uni.createFrom().voidItem();
            return Uni.createFrom()
                    .failure(new RuntimeException("Failed to delete database. Status: " + response.statusCode()));
        });
    }

    @Override
    public Uni<Void> renameDatabase(String oldName, String newName) {
        LOG.log(Level.INFO, "Renaming database {0} to {1}", new Object[] { oldName, newName });
        return Uni.createFrom().completionStage(() -> {
            String json = String.format("{\"name\": \"%s\"}", newName);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/db/" + oldName))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200)
                return Uni.createFrom().voidItem();
            return Uni.createFrom()
                    .failure(new RuntimeException("Failed to rename database. Status: " + response.statusCode()));
        });
    }

    @Override
    public Uni<List<String>> listDatabases() {
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/db"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transform(response -> {
            if (response.statusCode() == 200) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    return mapper.readValue(response.body(),
                            new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                            });
                } catch (java.io.IOException e) {
                    return List.of();
                }
            }
            return List.of();
        });
    }

    @Override
    public Uni<List<String>> listCollections(String dbName) {
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/db/" + dbName + "/collections"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transform(response -> {
            if (response.statusCode() == 200) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    return mapper.readValue(response.body(),
                            new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                            });
                } catch (java.io.IOException e) {
                    return List.of();
                }
            }
            return List.of();
        });
    }

    @Override
    public Uni<Void> addCollection(String dbName, String colName) {
        return addCollection(dbName, colName, "Document");
    }

    @Override
    public Uni<Void> addCollection(String dbName, String colName, String engine) {
        LOG.log(Level.INFO, "Adding collection {0} to database {1} [Engine: {2}]",
                new Object[] { colName, dbName, engine });
        return Uni.createFrom().completionStage(() -> {
            String json = String.format("{\"engine\": \"%s\"}", engine);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/db/" + dbName + "/collections/" + colName))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200)
                return Uni.createFrom().voidItem();
            return Uni.createFrom()
                    .failure(new RuntimeException("Failed to add collection. Status: " + response.statusCode()));
        });
    }

    @Override
    public Uni<Void> removeCollection(String dbName, String colName) {
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/db/" + dbName + "/collections/" + colName))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200)
                return Uni.createFrom().voidItem();
            return Uni.createFrom()
                    .failure(new RuntimeException("Failed to remove collection. Status: " + response.statusCode()));
        });
    }

    @Override
    public Uni<Void> renameCollection(String dbName, String oldName, String newName) {
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "http://" + pdAddress + "/api/db/" + dbName + "/collections/" + oldName + "/" + newName))
                    .header("Authorization", "Bearer " + authToken)
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200)
                return Uni.createFrom().voidItem();
            return Uni.createFrom()
                    .failure(new RuntimeException("Failed to rename collection. Status: " + response.statusCode()));
        });
    }

    @Override
    public Uni<List<NodeInfo>> listNodes() {
        LOG.log(Level.INFO, "Listing cluster nodes from {0} [Auth: {1}]", new Object[] { pdAddress, authToken });

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
                    return mapper.readValue(response.body(),
                            new com.fasterxml.jackson.core.type.TypeReference<List<NodeInfo>>() {
                            });
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

    @Override
    public Uni<Void> stopNode(String nodeId) {
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/internal/pd/nodes/" + nodeId + "/stop"))
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200) {
                return Uni.createFrom().voidItem();
            } else {
                return Uni.createFrom()
                        .failure(new RuntimeException("Failed to stop node. Status: " + response.statusCode()));
            }
        });
    }

    /**
     * Stops a node directly using its network address.
     * 
     * @param address The address of the node (host:port)
     * @return Uni<Void>
     */
    public Uni<Void> stopNodeDirect(String address) {
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + address + "/stop"))
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200) {
                return Uni.createFrom().voidItem();
            } else {
                return Uni.createFrom()
                        .failure(new RuntimeException(
                                "Failed to stop node at " + address + ". Status: " + response.statusCode()));
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
    public Uni<String> login(String username, String password) {
        return Uni.createFrom().completionStage(() -> {
            String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/web-auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transform(response -> {
            if (response.statusCode() == 200) {
                String body = response.body();
                if (body.contains("\"token\":\"")) {
                    int start = body.indexOf("\"token\":\"") + 9;
                    int end = body.indexOf("\"", start);
                    String token = body.substring(start, end);
                    this.authToken = token;
                    return token;
                }
            }
            throw new RuntimeException("Login failed. Status: " + response.statusCode() + " Body: " + response.body());
        });
    }

    @Override
    public String connectionInfo() {
        return String.format("Connected to %s [Token: %s]", pdAddress,
                (authToken != null && !authToken.isEmpty()) ? "Present" : "None");
    }
}
