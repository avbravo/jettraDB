package io.jettra.pd;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PlacementDriverService {
    private static final Logger LOG = Logger.getLogger(PlacementDriverService.class);

    private final Map<String, NodeMetadata> nodes = new ConcurrentHashMap<>();
    private final Map<Long, RaftGroupMetadata> groups = new ConcurrentHashMap<>();
    private final Map<String, DatabaseMetadata> databases = new ConcurrentHashMap<>();

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

    public void createDatabase(DatabaseMetadata db) {
        LOG.infof("Creating database: %s (Storage: %s, Engine: %s)", db.name(), db.storage(), db.engine());
        databases.put(db.name(), db);
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

    public java.util.Collection<DatabaseMetadata> listDatabases() {
        return databases.values();
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
        try (jakarta.ws.rs.client.Client client = jakarta.ws.rs.client.ClientBuilder.newClient()) {
            String targetUrl = String.format("http://%s/api/internal/node/stop", node.address());
            LOG.infof("Sending internal stop request to: %s", targetUrl);
            jakarta.ws.rs.core.Response response = client.target(targetUrl)
                    .request()
                    .post(jakarta.ws.rs.client.Entity.json("{}"));
            LOG.infof("Node %s responded with status: %d", nodeId, response.getStatus());
            response.close();
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
