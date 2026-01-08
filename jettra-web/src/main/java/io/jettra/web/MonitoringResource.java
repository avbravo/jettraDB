package io.jettra.web;

import io.jettra.pd.NodeMetadata;
import io.jettra.pd.PlacementDriverService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/api/monitor")
public class MonitoringResource {

    @Inject
    PlacementDriverService pdService;

    @GET
    @Path("/nodes")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<NodeMetadata> getNodes() {
        return pdService.getNodes().values();
    }

    @GET
    @Path("/alerts")
    @Produces(MediaType.APPLICATION_JSON)
    public java.util.List<Alert> getAlerts() {
        return java.util.List.of(
            new Alert("STORAGE", "Critical: Node 2 is at 85% capacity. Migration recommended.", "HIGH"),
            new Alert("LATENCY", "Warning: Raft Group 5 latency increased to 150ms.", "MEDIUM"),
            new Alert("CPU", "Predictive: Node 1 CPU load spike expected in 10 mins.", "LOW")
        );
    }

    public record Alert(String type, String message, String severity) {}
}
