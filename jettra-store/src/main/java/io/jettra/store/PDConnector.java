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

        double cpuUsage = 0.0;
        try {
            java.lang.management.OperatingSystemMXBean osBean = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                cpuUsage = sunBean.getProcessCpuLoad() * 100.0;
            } else {
                cpuUsage = osBean.getSystemLoadAverage(); // Fallback
            }
        } catch (Exception e) {
            cpuUsage = Math.random() * 10.0; // Minimal fallback
        }

        NodeMetadata me = new NodeMetadata(nodeId, selfAddress, "STORAGE", "ONLINE", System.currentTimeMillis(), cpuUsage, memoryUsage, memoryMax);

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
