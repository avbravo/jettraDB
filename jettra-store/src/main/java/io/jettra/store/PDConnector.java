package io.jettra.store;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.jettra.pd.NodeMetadata;
import io.jettra.pd.RaftGroupMetadata;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

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

        String pdUrl = "http://" + pdAddress.split(":")[0] + ":8080/api/internal/pd/register";
        String selfAddress = nodeId + ":" + port;

        NodeMetadata me = new NodeMetadata(nodeId, selfAddress, "STORAGE", "ONLINE", System.currentTimeMillis(), 0.0, 0, 0);

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

    @Scheduled(every = "5s")
    void reportStatus() {
        String pdUrl = "http://" + pdAddress.split(":")[0] + ":8080/api/internal/pd/register";
        String selfAddress = nodeId + ":" + port;

        Runtime runtime = Runtime.getRuntime();
        long memoryUsage = runtime.totalMemory() - runtime.freeMemory();
        long memoryMax = runtime.maxMemory();

        double cpuUsageVal = 0.0;
        try {
            java.lang.management.OperatingSystemMXBean osBean = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                cpuUsageVal = sunBean.getProcessCpuLoad() * 100.0;
            } else {
                cpuUsageVal = osBean.getSystemLoadAverage(); // Fallback
            }
        } catch (Exception e) {
            cpuUsageVal = Math.random() * 10.0; // Minimal fallback
        }

        NodeMetadata me = new NodeMetadata(nodeId, selfAddress, "STORAGE", "ONLINE", System.currentTimeMillis(), cpuUsageVal, memoryUsage, memoryMax);

        try {
            ClientBuilder.newClient()
                    .target(pdUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(me, MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            // Silent to avoid log spamming if PD is down
        }
    }

    @Scheduled(every = "10s")
    void reportGroups() {
        LOG.debugf("Node %s reporting Raft groups status to PD...", nodeId);
        String pdUrl = "http://" + pdAddress.split(":")[0] + ":8080/api/internal/pd/groups";

        // In this simulation, we'll assume group 1 exists.
        // If store-1 is the leader, it should report it. 
        // If we want to simulate a real failover, the "other" nodes should only report if they think they are leaders.
        // For simplicity in this demo, all nodes report, but they only report themselves as leader if they are store-1 
        // OR if they want to 'claim' it.
        
        // Let's make it smarter: only report if this node IS the leader in its own Raft state (if implemented)
        // Since jettra-consensus is also in this project, we could check RaftGroup.
        
        // Simulating Group #1. 
        // We'll keep jettra-store-1 as the default leader, but if it's gone, PD will reassign.
        // If this node is NOT jettra-store-1, and it sees store-1 is down (not implemented here), it could report itself.
        
        // For now, let's just let store-1 be the one that reports the 'initial' state, 
        // and let PD handle the 'offline' reassignment.
        // BUT if store-1 is stopped, NO ONE reports the group state anymore, but the group object still exists in PD.
        // PD's checkNodeHealth will see store-1 is offline and reassign the leader in the PD's memory.
        
        if ("jettra-store-1".equals(nodeId)) {
             RaftGroupMetadata group1 = new RaftGroupMetadata(1L, "jettra-store-1",
                List.of("jettra-store-1", "jettra-store-2", "jettra-store-3"));
             sendGroupReport(pdUrl, group1);
        }
    }

    private void sendGroupReport(String pdUrl, RaftGroupMetadata group) {
        try (jakarta.ws.rs.client.Client client = ClientBuilder.newClient()) {
            client.target(pdUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(group, MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            // LOG.error("Failed to report group", e);
        }
    }
}
