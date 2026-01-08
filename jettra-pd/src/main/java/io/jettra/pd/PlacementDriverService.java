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

    public void registerNode(NodeMetadata node) {
        LOG.infof("Registering node: %s at %s", node.id(), node.address());
        nodes.put(node.id(), node);
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
}
