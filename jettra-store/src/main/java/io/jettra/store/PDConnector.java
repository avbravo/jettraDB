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

        String host = pdAddress.split(":")[0];
        // PD REST API is always on 8080 in our current architecture
        String pdUrl = String.format("http://%s:8080/api/internal/pd/register", host);
        String selfAddress = nodeId + ":" + port;

        LOG.infof("Target PD Registration URL: %s", pdUrl);

        NodeMetadata me = new NodeMetadata(nodeId, selfAddress, "STORAGE", "ONLINE", System.currentTimeMillis(), 0.0, 0, 0);

        try (jakarta.ws.rs.client.Client client = ClientBuilder.newClient()) {
            jakarta.ws.rs.core.Response response = client.target(pdUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(me, MediaType.APPLICATION_JSON));
            
            if (response.getStatus() == 200) {
                LOG.info("Successfully registered with PD");
            } else {
                LOG.warnf("Failed to register with PD. Status: %d", response.getStatus());
            }
        } catch (Exception e) {
            LOG.error("Initial registration attempt failed. Will retry in background: " + e.getMessage());
        }
    }

    @Scheduled(every = "5s")
    void reportStatus() {
        String host = pdAddress.split(":")[0];
        String pdUrl = String.format("http://%s:8080/api/internal/pd/register", host);
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
        String host = pdAddress.split(":")[0];
        String pdUrl = String.format("http://%s:8080/api/internal/pd/groups", host);

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
