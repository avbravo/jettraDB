package io.jettra.store;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.jettra.consensus.MultiRaftManager;
import io.jettra.consensus.RaftState;
import io.jettra.pd.NodeMetadata;
import io.jettra.pd.RaftGroupMetadata;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
public class PDConnector {

    private static final Logger LOG = Logger.getLogger(PDConnector.class);

    @Inject
    MultiRaftManager raftManager;

    @ConfigProperty(name = "jettra.pd.addr")
    String pdAddress;

    @ConfigProperty(name = "jettra.node.id")
    String nodeId;

    @ConfigProperty(name = "quarkus.http.port", defaultValue = "8080")
    int port;

    void onStart(@Observes StartupEvent ev) {
        LOG.infof("Registering node %s with PD at %s", nodeId, pdAddress);
        sendRegistration();
    }

    private volatile boolean stopped = false;

    public void stop() {
        LOG.warn("Simulating node stop: Stopping PD heartbeats and reports.");
        this.stopped = true;
    }

    @Scheduled(every = "5s")
    void reportStatus() {
        if (stopped)
            return;
        sendRegistration();
    }

    private void sendRegistration() {
        String host = pdAddress.split(":")[0];
        String pdUrl = String.format("http://%s:8080/api/internal/pd/register", host);
        String selfAddress = nodeId + ":" + port;

        Runtime runtime = Runtime.getRuntime();
        long memoryUsage = runtime.totalMemory() - runtime.freeMemory();
        long memoryMax = runtime.maxMemory();

        double cpuUsageVal = 0.0;
        try {
            java.lang.management.OperatingSystemMXBean osBean = java.lang.management.ManagementFactory
                    .getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                cpuUsageVal = sunBean.getProcessCpuLoad() * 100.0;
                if (cpuUsageVal < 0)
                    cpuUsageVal = 0.0;
            } else {
                cpuUsageVal = osBean.getSystemLoadAverage();
                if (cpuUsageVal < 0)
                    cpuUsageVal = 0.0;
            }
        } catch (Exception e) {
            cpuUsageVal = 0.0;
        }

        long diskUsage = 0;
        long diskMax = 0;
        try {
            java.io.File file = new java.io.File("/");
            diskMax = file.getTotalSpace();
            diskUsage = diskMax - file.getFreeSpace();
        } catch (Exception e) {
            // Ignored
        }

        String raftRole = getLocalRaftRole();
        NodeMetadata me = new NodeMetadata(nodeId, selfAddress, "STORAGE", "ONLINE", raftRole,
                System.currentTimeMillis(), cpuUsageVal, memoryUsage, memoryMax, diskUsage, diskMax);

        try (jakarta.ws.rs.client.Client client = ClientBuilder.newClient()) {
            client.target(pdUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(me, MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            // Silent to avoid log spamming if PD is down
        }
    }

    @Scheduled(every = "10s")
    void reportGroups() {
        if (stopped)
            return;

        LOG.debugf("Node %s reporting Raft groups status to PD...", nodeId);
        String host = pdAddress.split(":")[0];
        String pdUrl = String.format("http://%s:8080/api/internal/pd/groups", host);

        if ("jettra-store-1".equals(nodeId)) {
            io.jettra.consensus.RaftGroup group = raftManager.getOrCreateGroup(1L);
            group.setState(RaftState.LEADER);
            RaftGroupMetadata metadata = new RaftGroupMetadata(1L, "jettra-store-1",
                    List.of("jettra-store-1", "jettra-store-2", "jettra-store-3"));
            sendGroupReport(pdUrl, metadata);
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

    private String getLocalRaftRole() {
        if (raftManager == null || raftManager.getGroups().isEmpty()) {
            return "FOLLOWER";
        }
        // If leader of any group, report as LEADER for simplicity in node list
        boolean isLeader = raftManager.getGroups().values().stream()
                .anyMatch(g -> g.getState() == RaftState.LEADER);
        return isLeader ? "LEADER" : "FOLLOWER";
    }
}
