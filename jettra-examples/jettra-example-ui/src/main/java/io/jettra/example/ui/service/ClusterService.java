package io.jettra.example.ui.service;

import io.jettra.example.ui.client.PlacementDriverClient;
import io.jettra.example.ui.model.Node;
import io.jettra.example.ui.model.RaftGroup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class ClusterService {

    private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(ClusterService.class);

    @Inject
    @RestClient
    PlacementDriverClient pdClient;

    public List<Node> getNodes(String token) {
        try {
            String bearer = token != null ? "Bearer " + token : null;
            List<Node> nodes = pdClient.getNodes(bearer);
            LOG.infof("Fetched %d nodes from PD", nodes != null ? nodes.size() : 0);
            return nodes;
        } catch (Exception e) {
            LOG.error("Failed to fetch nodes from PD", e);
            return Collections.emptyList();
        }
    }

    public List<RaftGroup> getGroups(String token) {
        try {
            String bearer = token != null ? "Bearer " + token : null;
            List<RaftGroup> groups = pdClient.getGroups(bearer);
            LOG.infof("Fetched %d groups from PD", groups != null ? groups.size() : 0);
            return groups;
        } catch (Exception e) {
            LOG.error("Failed to fetch groups from PD", e);
            return Collections.emptyList();
        }
    }

    public boolean stopNode(String nodeId, String token) {
        try {
            String bearer = token != null ? "Bearer " + token : null;
            var response = pdClient.stopNode(nodeId, bearer);
            return response.getStatus() == 200;
        } catch (Exception e) {
            LOG.error("Failed to stop node " + nodeId, e);
            return false;
        }
    }
}
