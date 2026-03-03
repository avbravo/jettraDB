package io.jettra.web;

import io.jettra.pd.NodeMetadata;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Collection;

@Path("/api/v1/graph")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GraphResource {

    @ConfigProperty(name = "jettra.pd.url")
    String pdUrl;

    @Inject
    MonitoringResource monitoringResource;

    @jakarta.ws.rs.core.Context
    HttpHeaders headers;

    private String getAuthHeader() {
        return headers.getHeaderString(HttpHeaders.AUTHORIZATION);
    }

    private String getStorageNode() {
        Collection<NodeMetadata> nodes = monitoringResource.getNodes();
        if (nodes == null)
            return null;
        return nodes.stream()
                .filter(n -> "STORAGE".equals(n.role()) && "ONLINE".equals(n.status()))
                .map(NodeMetadata::address)
                .findFirst()
                .orElse(null);
    }

    @POST
    @Path("/vertex")
    public Response addVertex(String payload) {
        String address = getStorageNode();
        if (address == null)
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\":\"No online storage node found\"}").build();

        try (Client client = ClientBuilder.newClient()) {
            return client.target("http://" + address + "/api/v1/graph/vertex")
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .post(Entity.json(payload));
        }
    }

    @POST
    @Path("/edge")
    public Response addEdge(String payload) {
        String address = getStorageNode();
        if (address == null)
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\":\"No online storage node found\"}").build();

        try (Client client = ClientBuilder.newClient()) {
            return client.target("http://" + address + "/api/v1/graph/edge")
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .post(Entity.json(payload));
        }
    }

    @GET
    @Path("/traverse/{id}")
    public Response traverse(@PathParam("id") String id, @QueryParam("depth") @DefaultValue("3") int depth) {
        String address = getStorageNode();
        if (address == null)
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\":\"No online storage node found\"}").build();

        try (Client client = ClientBuilder.newClient()) {
            return client.target("http://" + address + "/api/v1/graph/traverse/" + id)
                    .queryParam("depth", depth)
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .get();
        }
    }
}
