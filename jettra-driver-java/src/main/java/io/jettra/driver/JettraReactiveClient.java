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
    private String discoveredStoreAddress;
    private final HttpClient httpClient;
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public JettraReactiveClient(String pdAddress, String authToken) {
        this.pdAddress = pdAddress;
        this.authToken = authToken;
        this.httpClient = HttpClient.newHttpClient();
    }

    public JettraReactiveClient(String pdAddress) {
        this(pdAddress, null);
    }

    private Uni<String> getStoreAddress() {
        if (discoveredStoreAddress != null) {
            return Uni.createFrom().item(discoveredStoreAddress);
        }
        return listNodes().onItem().transform(nodes -> {
            discoveredStoreAddress = nodes.stream()
                    .filter(n -> "ONLINE".equals(n.status()) && "STORAGE".equals(n.role()))
                    .map(NodeInfo::address)
                    .findFirst()
                    .orElse(pdAddress); // Fallback to PD address if no store found (unlikely in real deployments)
            return discoveredStoreAddress;
        });
    }

    @Override
    public Uni<Void> save(String collection, Object document) {
        return save(collection, null, document);
    }

    @Override
    public Uni<Void> save(String collection, String jettraId, Object document) {
        return getStoreAddress().onItem().transformToUni(address -> Uni.createFrom().completionStage(() -> {
            try {
                String json = (document instanceof String) ? (String) document : mapper.writeValueAsString(document);
                String url = "http://" + address + "/api/v1/document/" + collection;
                if (jettraId != null) {
                    url += "?jettraID=" + java.net.URLEncoder.encode(jettraId, java.nio.charset.StandardCharsets.UTF_8);
                }
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Bearer " + authToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        })).onItem().transformToUni(response -> {
            if (response.statusCode() >= 200 && response.statusCode() < 300)
                return Uni.createFrom().voidItem();
            return Uni.createFrom().failure(new RuntimeException("Save failed: " + response.body()));
        });
    }

    @Override
    public Uni<Object> findById(String collection, String id) {
        return findById(collection, id, false);
    }

    @Override
    public Uni<Object> findById(String collection, String id, boolean resolveRefs) {
        return getStoreAddress().onItem().transformToUni(address -> Uni.createFrom().completionStage(() -> {
            String url = "http://" + address + "/api/v1/document/" + collection + "/"
                    + java.net.URLEncoder.encode(id, java.nio.charset.StandardCharsets.UTF_8);
            if (resolveRefs) {
                url += "?resolveRefs=true";
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        })).onItem().transform(response -> {
            if (response.statusCode() == 200)
                return response.body();
            if (response.statusCode() == 404)
                return null;
            throw new RuntimeException("Find failed: " + response.body());
        });
    }

    @Override
    public Uni<Void> delete(String collection, String id) {
        return getStoreAddress().onItem().transformToUni(address -> Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + address + "/api/v1/document/" + collection + "/"
                            + java.net.URLEncoder.encode(id, java.nio.charset.StandardCharsets.UTF_8)))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        })).onItem().transformToUni(response -> {
            if (response.statusCode() == 200 || response.statusCode() == 204)
                return Uni.createFrom().voidItem();
            throw new RuntimeException("Delete failed: " + response.body());
        });
    }

    @Override
    public Uni<Long> count(String collection) {
        // Simple mock count for now, implementation could be added in Store
        return Uni.createFrom().item(0L);
    }

    @Override
    public Uni<String> generateJettraId(String bucketId) {
        return Uni.createFrom().item(bucketId + "#" + java.util.UUID.randomUUID().toString().replace("-", ""));
    }

    @Override
    public Uni<java.util.List<String>> getDocumentVersions(String collection, String jettraId) {
        return getStoreAddress().onItem().transformToUni(address -> Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + address + "/api/v1/document/" + collection + "/"
                            + java.net.URLEncoder.encode(jettraId, java.nio.charset.StandardCharsets.UTF_8)
                            + "/versions"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        })).onItem().transform(response -> {
            if (response.statusCode() == 200) {
                try {
                    return mapper.readValue(response.body(),
                            new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                            });
                } catch (Exception e) {
                    return List.of();
                }
            }
            return List.of();
        });
    }

    @Override
    public Uni<Void> restoreVersion(String collection, String jettraId, String version) {
        return getStoreAddress().onItem().transformToUni(address -> Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + address + "/api/v1/document/" + collection + "/"
                            + java.net.URLEncoder.encode(jettraId, java.nio.charset.StandardCharsets.UTF_8)
                            + "/restore/"
                            + java.net.URLEncoder.encode(version, java.nio.charset.StandardCharsets.UTF_8)))
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        })).onItem().transformToUni(response -> {
            if (response.statusCode() == 200)
                return Uni.createFrom().voidItem();
            return Uni.createFrom().failure(new RuntimeException("Restore failed: " + response.body()));
        });
    }

    @Override
    public Uni<Object> resolveReference(String collection, String referenceJettraId) {
        return findById(collection, referenceJettraId);
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
    public Uni<String> getDatabaseInfo(String name) {
        LOG.log(Level.INFO, "Getting info for database {0}", name);
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/db/" + name))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transform(response -> {
            if (response.statusCode() == 200) {
                return response.body();
            }
            throw new RuntimeException("Failed to get database info. Status: " + response.statusCode());
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
                String body = response.body();
                List<String> names = new java.util.ArrayList<>();
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"name\":\"([^\"]+)\"").matcher(body);
                while (m.find()) {
                    names.add(m.group(1));
                }
                return names;
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
                String body = response.body();
                List<String> names = new java.util.ArrayList<>();
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"name\":\"([^\"]+)\"").matcher(body);
                while (m.find()) {
                    names.add(m.group(1));
                }
                return names;
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
            String url = "http://" + pdAddress + "/api/db/" + dbName + "/collections/" + colName;
            return Uni.createFrom()
                    .failure(new RuntimeException(
                            "Failed to add collection. Status: " + response.statusCode() + " URL: " + url));
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
        String authStatus = (authToken != null) ? "active" : "none";
        LOG.log(Level.INFO, "Listing cluster nodes from {0} [Auth: {1}]", new Object[] { pdAddress, authStatus });

        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/monitor/nodes"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transform(response -> {
            if (response.statusCode() == 200) {
                try {
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
                    .uri(URI.create("http://" + pdAddress + "/api/monitor/nodes/" + nodeId + "/stop"))
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
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200) {
                try {
                    com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(response.body());
                    if (node.has("token")) {
                        this.authToken = node.get("token").asText();
                        return Uni.createFrom().item(this.authToken);
                    }
                } catch (Exception e) {
                    return Uni.createFrom()
                            .failure(new RuntimeException("Failed to parse login response: " + e.getMessage()));
                }
            }
            return Uni.createFrom().failure(new RuntimeException(
                    "Login failed. Status: " + response.statusCode() + " Body: " + response.body()));
        });
    }

    @Override
    public Uni<Void> createUser(String username, String password, java.util.Set<String> roles) {
        return Uni.createFrom().completionStage(() -> {
            String json = String.format(
                    "{\"username\":\"%s\", \"password\":\"%s\", \"roles\":%s, \"forcePasswordChange\":false}",
                    username, password, formatSet(roles));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/auth/users"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200)
                return Uni.createFrom().voidItem();
            return Uni.createFrom().failure(new RuntimeException("Create user failed: " + response.body()));
        });
    }

    @Override
    public Uni<Void> updateUser(String username, String password, java.util.Set<String> roles) {
        return Uni.createFrom().completionStage(() -> {
            String json = String.format(
                    "{\"username\":\"%s\", \"password\":\"%s\", \"roles\":%s, \"forcePasswordChange\":false}",
                    username, password, formatSet(roles));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/auth/users/" + username))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200)
                return Uni.createFrom().voidItem();
            return Uni.createFrom().failure(new RuntimeException("Update user failed: " + response.body()));
        });
    }

    @Override
    public Uni<java.util.List<String>> listUsers() {
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/auth/users"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transform(response -> {
            if (response.statusCode() == 200) {
                // Simplified extraction of usernames from JSON array of objects
                String body = response.body();
                java.util.List<String> names = new java.util.ArrayList<>();
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"username\":\"([^\"]+)\"").matcher(body);
                while (m.find())
                    names.add(m.group(1));
                return names;
            }
            return java.util.List.of();
        });
    }

    @Override
    public Uni<Void> deleteUser(String username) {
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/auth/users/" + username))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200)
                return Uni.createFrom().voidItem();
            return Uni.createFrom().failure(new RuntimeException("Delete user failed: " + response.body()));
        });
    }

    @Override
    public Uni<Void> createRole(String name, String database, java.util.Set<String> privileges) {
        return Uni.createFrom().completionStage(() -> {
            String json = String.format("{\"name\":\"%s\", \"database\":\"%s\", \"privileges\":%s}",
                    name, database, formatSet(privileges));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/auth/roles"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200)
                return Uni.createFrom().voidItem();
            return Uni.createFrom().failure(new RuntimeException("Create role failed: " + response.body()));
        });
    }

    @Override
    public Uni<Void> updateRole(String name, String database, java.util.Set<String> privileges) {
        return Uni.createFrom().completionStage(() -> {
            String json = String.format("{\"name\":\"%s\", \"database\":\"%s\", \"privileges\":%s}",
                    name, database, formatSet(privileges));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/auth/roles/" + name))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200)
                return Uni.createFrom().voidItem();
            return Uni.createFrom().failure(new RuntimeException("Update role failed: " + response.body()));
        });
    }

    @Override
    public Uni<java.util.List<String>> listRoles() {
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/auth/roles"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transform(response -> {
            if (response.statusCode() == 200) {
                String body = response.body();
                java.util.List<String> names = new java.util.ArrayList<>();
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"name\":\"([^\"]+)\"").matcher(body);
                while (m.find())
                    names.add(m.group(1));
                return names;
            }
            return java.util.List.of();
        });
    }

    @Override
    public Uni<Void> deleteRole(String name) {
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/auth/roles/" + name))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200)
                return Uni.createFrom().voidItem();
            return Uni.createFrom().failure(new RuntimeException("Delete role failed: " + response.body()));
        });
    }

    private String formatSet(java.util.Set<String> set) {
        if (set == null || set.isEmpty())
            return "[]";
        return "[" + String.join(",", set.stream().map(s -> "\"" + s + "\"").toList()) + "]";
    }

    @Override
    public Uni<String> executeSql(String sql) {
        return executeSql(sql, false);
    }

    @Override
    public Uni<String> executeSql(String sql, boolean resolveRefs) {
        LOG.log(Level.INFO, "Executing SQL via PD (resolveRefs={0}): {1}", new Object[] { resolveRefs, sql });
        return Uni.createFrom().completionStage(() -> {
            try {
                java.util.Map<String, Object> body = java.util.Map.of("sql", sql, "resolveRefs", resolveRefs);
                String json = mapper.writeValueAsString(body);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + pdAddress + "/api/v1/sql"))
                        .header("Authorization", "Bearer " + authToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).onItem().transform(response -> {
            if (response.statusCode() == 200) {
                return response.body();
            }
            throw new RuntimeException(
                    "SQL execution failed. Status: " + response.statusCode() + " Body: " + response.body());
        });
    }

    @Override
    public Uni<Void> createSequence(String name, String database, long start, long increment) {
        LOG.log(Level.INFO, "Creating sequence: {0} in {1}", new Object[] { name, database });
        return Uni.createFrom().completionStage(() -> {
            try {
                String json = String.format(
                        "{\"name\": \"%s\", \"database\": \"%s\", \"startValue\": %d, \"increment\": %d}",
                        name, database, start, increment);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + pdAddress + "/api/v1/sequence"))
                        .header("Authorization", "Bearer " + authToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 201) {
                return Uni.createFrom().voidItem();
            }
            return Uni.createFrom().failure(new RuntimeException("Failed to create sequence: " + response.body()));
        });
    }

    @Override
    public Uni<Long> nextSequenceValue(String name) {
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/v1/sequence/" + name + "/next"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transform(response -> {
            if (response.statusCode() == 200) {
                String body = response.body();
                // Simple parsing of {"value": 123}
                int start = body.indexOf("\"value\":") + 8;
                int end = body.indexOf("}", start);
                return Long.parseLong(body.substring(start, end).trim());
            }
            throw new RuntimeException("Failed to get next sequence value: " + response.body());
        });
    }

    @Override
    public Uni<Long> currentSequenceValue(String name) {
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/v1/sequence/" + name + "/current"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transform(response -> {
            if (response.statusCode() == 200) {
                String body = response.body();
                int start = body.indexOf("\"value\":") + 8;
                int end = body.indexOf("}", start);
                return Long.parseLong(body.substring(start, end).trim());
            }
            throw new RuntimeException("Failed to get current sequence value: " + response.body());
        });
    }

    @Override
    public Uni<Void> resetSequence(String name, long value) {
        return Uni.createFrom().completionStage(() -> {
            try {
                String json = String.format("{\"newValue\": %d}", value);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + pdAddress + "/api/v1/sequence/" + name + "/reset"))
                        .header("Authorization", "Bearer " + authToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 200) {
                return Uni.createFrom().voidItem();
            }
            return Uni.createFrom().failure(new RuntimeException("Failed to reset sequence: " + response.body()));
        });
    }

    @Override
    public Uni<Void> deleteSequence(String name) {
        return Uni.createFrom().completionStage(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + pdAddress + "/api/v1/sequence/" + name))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transformToUni(response -> {
            if (response.statusCode() == 204 || response.statusCode() == 200) {
                return Uni.createFrom().voidItem();
            }
            return Uni.createFrom().failure(new RuntimeException("Failed to delete sequence: " + response.body()));
        });
    }

    @Override
    public Uni<java.util.List<String>> listSequences(String database) {
        return Uni.createFrom().completionStage(() -> {
            String url = "http://" + pdAddress + "/api/v1/sequence" + (database != null ? "?database=" + database : "");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }).onItem().transform(response -> {
            if (response.statusCode() == 200) {
                String body = response.body();
                // Simple parsing for JSON list of objects to list of names
                java.util.List<String> names = new java.util.ArrayList<>();
                java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"name\":\"(.*?)\"");
                java.util.regex.Matcher m = p.matcher(body);
                while (m.find()) {
                    names.add(m.group(1));
                }
                return names;
            }
            throw new RuntimeException("Failed to list sequences: " + response.body());
        });
    }

    @Override
    public String connectionInfo() {
        return String.format("Connected to %s [Token: %s]", pdAddress,
                (authToken != null && !authToken.isEmpty()) ? "Present" : "None");
    }
}
