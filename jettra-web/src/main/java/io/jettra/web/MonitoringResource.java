package io.jettra.web;

import io.jettra.pd.NodeMetadata;
import io.jettra.pd.RaftGroupMetadata;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;

@Path("/api/monitor")
public class MonitoringResource {

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "jettra.pd.url")
    String pdUrl;

    @jakarta.ws.rs.core.Context
    jakarta.ws.rs.core.HttpHeaders headers;

    @jakarta.inject.Inject
    @io.quarkus.qute.Location("monitoring/nodes.html")
    io.quarkus.qute.Template nodes;

    @jakarta.inject.Inject
    @io.quarkus.qute.Location("monitoring/groups.html")
    io.quarkus.qute.Template groups;

    @jakarta.inject.Inject
    org.eclipse.microprofile.jwt.JsonWebToken jwt;

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
                Collection<NodeMetadata> nodeList = response
                        .readEntity(new GenericType<java.util.List<NodeMetadata>>() {
                        });
                return nodeList;
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
                Response response = client.target(pdUrl + "/api/internal/pd/groups")
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeader)
                        .get();

                if (response.getStatus() == 401 || response.getStatus() == 403) {
                    throw new jakarta.ws.rs.WebApplicationException(response);
                }
                return response.readEntity(new GenericType<java.util.List<RaftGroupMetadata>>() {
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

    @jakarta.ws.rs.POST
    @Path("/nodes/{id}/stop")
    public jakarta.ws.rs.core.Response stopNode(@jakarta.ws.rs.PathParam("id") String id) {
        try {
            String authHeader = headers.getHeaderString("Authorization");
            try (jakarta.ws.rs.client.Client client = jakarta.ws.rs.client.ClientBuilder.newClient()) {
                return client.target(pdUrl + "/api/internal/pd/nodes/" + id + "/stop")
                        .request()
                        .header("Authorization", authHeader)
                        .post(jakarta.ws.rs.client.Entity.json("{}"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jakarta.ws.rs.core.Response.serverError().build();
        }
    }

    @GET
    @Path("/htmx/nodes")
    @Produces(MediaType.TEXT_HTML)
    public String getNodesHtmx() {
        Collection<NodeMetadata> nodeList = getNodes();
        boolean isSuperUser = false;
        if (jwt != null) {
            String upn = jwt.getClaim("upn");
            java.util.Set<String> roles = jwt.getGroups();
            isSuperUser = "admin".equals(upn) || "super-user".equals(upn)
                    || (roles != null && roles.contains("system"));
        }
        return nodes.data("nodes", nodeList).data("isSuperUser", isSuperUser).render();
    }

    @GET
    @Path("/htmx/groups")
    @Produces(MediaType.TEXT_HTML)
    public String getGroupsHtmx() {
        Collection<RaftGroupMetadata> groupList = getGroups();
        return groups.data("groups", groupList).render();
    }
}
