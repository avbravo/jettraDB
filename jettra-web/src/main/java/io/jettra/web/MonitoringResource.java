package io.jettra.web;

import io.jettra.pd.NodeMetadata;
import io.jettra.pd.RaftGroupMetadata;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/api/monitor")
public class MonitoringResource {

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "jettra.pd.url")
    String pdUrl;

    @jakarta.ws.rs.core.Context
    jakarta.ws.rs.core.HttpHeaders headers;

    @GET
    @Path("/nodes")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<NodeMetadata> getNodes() {
        try {
            String authHeader = headers.getHeaderString("Authorization");
            try (jakarta.ws.rs.client.Client client = jakarta.ws.rs.client.ClientBuilder.newClient()) {
                jakarta.ws.rs.core.Response response = client.target(pdUrl + "/api/internal/pd/nodes")
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeader)
                        .get();

                if (response.getStatus() == 401 || response.getStatus() == 403) {
                    throw new jakarta.ws.rs.WebApplicationException(response);
                }
                return response.readEntity(new jakarta.ws.rs.core.GenericType<java.util.List<NodeMetadata>>() {
                });
            }
        } catch (jakarta.ws.rs.WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    @GET
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<RaftGroupMetadata> getGroups() {
        try {
            String authHeader = headers.getHeaderString("Authorization");
            try (jakarta.ws.rs.client.Client client = jakarta.ws.rs.client.ClientBuilder.newClient()) {
                jakarta.ws.rs.core.Response response = client.target(pdUrl + "/api/internal/pd/groups")
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeader)
                        .get();

                if (response.getStatus() == 401 || response.getStatus() == 403) {
                    throw new jakarta.ws.rs.WebApplicationException(response);
                }
                return response.readEntity(new jakarta.ws.rs.core.GenericType<java.util.List<RaftGroupMetadata>>() {
                });
            }
        } catch (jakarta.ws.rs.WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    @GET
    @Path("/alerts")
    @Produces(MediaType.APPLICATION_JSON)
    public java.util.List<Alert> getAlerts() {
        return java.util.List.of(
                new Alert("STORAGE", "Critical: Node 2 is at 85% capacity. Migration recommended.", "HIGH"),
                new Alert("LATENCY", "Warning: Raft Group 5 latency increased to 150ms.", "MEDIUM"),
                new Alert("CPU", "Predictive: Node 1 CPU load spike expected in 10 mins.", "LOW"));
    }

    public record Alert(String type, String message, String severity) {
    }
}
