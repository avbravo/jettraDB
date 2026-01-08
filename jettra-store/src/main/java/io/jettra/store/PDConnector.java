package io.jettra.store;

import io.jettra.pd.NodeMetadata;
import io.jettra.pd.RaftGroupMetadata;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PDConnector {

    private static final Logger LOG = Logger.getLogger(PDConnector.class);

    @ConfigProperty(name = "jettra.pd.addr")
    String pdAddress;

    @ConfigProperty(name = "jettra.node.id")
    String nodeId;

    @ConfigProperty(name = "quarkus.http.port", defaultValue = "8080")
    int port;

    void onStart(@Observes StartupEvent ev) {
        LOG.infof("Registering node %s with PD at %s", nodeId, pdAddress);

        // Construct the PD URL. pdAddress is 'jettra-pd:9000' (grpc) but we need http
        // port 8080 for REST
        // In docker-compose we set JETTRA_PD_ADDR=jettra-pd:9000, but the REST API is
        // on 8080.
        // We can assume http://jettra-pd:8080 for this internal communication if we
        // trust the service name.
        // Or we can parse it. For now let's construct it.

        String pdUrl = "http://" + pdAddress.split(":")[0] + ":8080/api/internal/pd/register";

        // For self address, we need the address that OTHER nodes (and PD) can reach us
        // at.
        // In docker-compose, the hostname is the service name (e.g. jettra-store-1) or
        // container IP.
        // We will trust the node ID to be the hostname/service name as per convention
        // in our compose file
        // (JETTRA_NODE_ID=store-1, but container_name=jettra-store-1, let's check
        // compose again to be sure).

        String selfAddress = nodeId + ":" + port;

        NodeMetadata me = new NodeMetadata(nodeId, selfAddress, "STORAGE", "ONLINE", System.currentTimeMillis());

        try {
            ClientBuilder.newClient()
                    .target(pdUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(me, MediaType.APPLICATION_JSON));
            LOG.info("Successfully registered with PD");
        } catch (Exception e) {
            LOG.error("Failed to register with PD", e);
        }
    }

    @Scheduled(every = "10s")
    void reportGroups() {
        // Only store-1 reports for this simulation to avoid conflicts/spam
        if (!"jettra-store-1".equals(nodeId)) {
            return;
        }

        LOG.info("Reporting Raft groups status to PD...");
        String pdUrl = "http://" + pdAddress.split(":")[0] + ":8080/api/internal/pd/groups";

        // Simulating Group #1 with all nodes as peers
        RaftGroupMetadata group1 = new RaftGroupMetadata(1L, "jettra-store-1",
                List.of("jettra-store-1", "jettra-store-2", "jettra-store-3"));

        try {
            ClientBuilder.newClient()
                    .target(pdUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(group1, MediaType.APPLICATION_JSON));
            LOG.info("Successfully reported groups to PD");
        } catch (Exception e) {
            LOG.error("Failed to report groups to PD", e);
        }
    }
}
