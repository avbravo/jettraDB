package io.jettra.pd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PlacementDriverService {
    private static final Logger LOG = Logger.getLogger(PlacementDriverService.class);

    private final Map<String, NodeMetadata> nodes = new ConcurrentHashMap<>();
    private final Map<Long, RaftGroupMetadata> groups = new ConcurrentHashMap<>();
    private final Map<String, DatabaseMetadata> databases = new ConcurrentHashMap<>();

    @jakarta.inject.Inject
    io.jettra.pd.auth.AuthService authService;

    @io.quarkus.scheduler.Scheduled(every = "2s")
    void checkNodeHealth() {
        long now = System.currentTimeMillis();
        nodes.forEach((id, node) -> {
            if (now - node.lastSeen() > 10000 && "ONLINE".equals(node.status())) { // 10 seconds threshold
                LOG.warnf("Node %s is unresponsive. Marking as OFFLINE.", id);
                NodeMetadata offlineNode = new NodeMetadata(
                        node.id(), node.address(), node.role(), "OFFLINE",
                        node.raftRole(),
                        node.lastSeen(), node.cpuUsage(), node.memoryUsage(), node.memoryMax());
                nodes.put(id, offlineNode);

                // If this node was a leader of any group, trigger election
                reassignLeadersFromOfflineNode(id);
            }
        });
    }

    private void reassignLeadersFromOfflineNode(String nodeId) {
        groups.forEach((groupId, group) -> {
            if (nodeId.equals(group.leaderId())) {
                LOG.infof("Leader %s for group %d is offline. Reassigning leader...", nodeId, groupId);
                // Find another online peer
                java.util.Optional<String> newLeader = group.peers().stream()
                        .filter(peerId -> !peerId.equals(nodeId))
                        .filter(peerId -> {
                            NodeMetadata peer = nodes.get(peerId);
                            return peer != null && "ONLINE".equals(peer.status());
                        })
                        .findFirst();

                if (newLeader.isPresent()) {
                    LOG.infof("New leader for group %d: %s", groupId, newLeader.get());
                    RaftGroupMetadata updatedGroup = new RaftGroupMetadata(
                            group.groupId(), newLeader.get(), group.peers());
                    groups.put(groupId, updatedGroup);
                } else {
                    LOG.warnf("No online peers available for group %d to take over!", groupId);
                }
            }
        });
    }

    public void createDatabase(DatabaseMetadata db, String creator) {
        LOG.infof("Creating database: %s (Storage: %s, Engine: %s) by %s", db.name(), db.storage(), db.engine(),
                creator);
        databases.put(db.name(), db);
        if (authService != null) {
            authService.setupDefaultDatabaseRoles(db.name(), creator);
        }
    }

    public void updateDatabase(String oldName, DatabaseMetadata db) {
        LOG.infof("Updating database: %s -> %s", oldName, db.name());
        if (!oldName.equals(db.name())) {
            databases.remove(oldName);
        }
        databases.put(db.name(), db);
    }

    public void deleteDatabase(String name) {
        LOG.infof("Deleting database: %s", name);
        databases.remove(name);
    }

    public java.util.Collection<DatabaseMetadata> listDatabases(String username) {
        if ("admin".equals(username) || "system-pd".equals(username) || "system".equals(username)) {
            return databases.values();
        }

        if (authService == null) {
            return java.util.Collections.emptyList();
        }

        io.jettra.pd.auth.User user = authService.getUser(username);
        if (user == null) {
            return java.util.Collections.emptyList();
        }

        java.util.List<io.jettra.pd.auth.Role> userRoles = authService.getRolesForUser(user);
        return databases.values().stream()
                .filter(db -> userRoles.stream()
                        .anyMatch(r -> "_all".equals(r.database()) || db.name().equals(r.database())))
                .toList();
    }

    public DatabaseMetadata getDatabaseInfo(String name) {
        return databases.get(name);
    }

    public boolean addCollection(String dbName, String collectionName, String engine) {
        DatabaseMetadata db = databases.get(dbName);
        LOG.infof("addCollection called for DB: %s, Collection: %s. DB Found: %s", dbName, collectionName,
                (db != null));
        if (db != null) {
            boolean exists = db.collections().stream().anyMatch(c -> c.name().equals(collectionName));
            if (!exists) {
                db.collections().add(new CollectionMetadata(collectionName, engine));
            }
            return true;
        }
        return false;
    }

    public void removeCollection(String dbName, String collectionName) {
        DatabaseMetadata db = databases.get(dbName);
        if (db != null) {
            db.collections().removeIf(c -> c.name().equals(collectionName));
        }
    }

    public void renameCollection(String dbName, String oldName, String newName) {
        DatabaseMetadata db = databases.get(dbName);
        if (db != null) {
            for (int i = 0; i < db.collections().size(); i++) {
                CollectionMetadata col = db.collections().get(i);
                if (col.name().equals(oldName)) {
                    db.collections().set(i, new CollectionMetadata(newName, col.engine()));
                    break;
                }
            }
        }
    }

    public void registerNode(NodeMetadata node) {
        LOG.debugf("Registering node: %s at %s", node.id(), node.address());
        NodeMetadata updatedNode = new NodeMetadata(
                node.id(), node.address(), node.role(), node.status(),
                node.raftRole(),
                System.currentTimeMillis(), // Use local PD time for lastSeen
                node.cpuUsage(), node.memoryUsage(), node.memoryMax());
        nodes.put(node.id(), updatedNode);
    }

    public RaftGroupMetadata getGroup(long groupId) {
        return groups.get(groupId);
    }

    public void updateGroup(RaftGroupMetadata group) {
        groups.put(group.groupId(), group);
    }

    public Map<String, NodeMetadata> getNodes() {
        return nodes;
    }

    public Map<Long, RaftGroupMetadata> getGroups() {
        return groups;
    }

    public void stopNode(String nodeId) {
        NodeMetadata node = nodes.get(nodeId);
        if (node == null) {
            LOG.warnf("Node %s not found for stop command", nodeId);
            return;
        }

        LOG.infof("Requesting stop for node %s at %s", nodeId, node.address());

        // Use a background thread or asynchronous call to notify the node to stop
        // We'll use a simple JAX-RS client call here
        try {
            String targetUrl = String.format("http://%s/stop", node.address());
            LOG.infof("Sending internal stop request to: %s", targetUrl);

            String token = io.jettra.pd.auth.TokenUtils.generateToken("system-pd", java.util.Set.of("admin", "system"));

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(targetUrl))
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString("{}"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            LOG.infof("Node %s responded with status: %d", nodeId, response.statusCode());
        } catch (Exception e) {
            LOG.errorf("Error notifying node %s to stop at %s: %s", nodeId, node.address(), e.getMessage());
        }

        // Mark as OFFLINE immediately in PD metadata
        NodeMetadata offlineNode = new NodeMetadata(
                node.id(), node.address(), node.role(), "OFFLINE",
                node.raftRole(),
                node.lastSeen(), node.cpuUsage(), node.memoryUsage(), node.memoryMax());
        nodes.put(nodeId, offlineNode);
        reassignLeadersFromOfflineNode(nodeId);
    }
}
