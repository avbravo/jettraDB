package io.jettra.pd;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/internal/pd")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlacementDriverResource {

    @Inject
    PlacementDriverService pdService;

    @GET
    @Path("/nodes")
    public java.util.Collection<NodeMetadata> getNodes() {
        return pdService.getNodes().values();
    }

    @GET
    @Path("/groups")
    public java.util.Collection<RaftGroupMetadata> getGroups() {
        return pdService.getGroups().values();
    }

    @POST
    @Path("/groups")
    public Response updateGroup(RaftGroupMetadata group) {
        pdService.updateGroup(group);
        return Response.ok().build();
    }

    @POST
    @Path("/register")
    public Response register(NodeMetadata node) {
        pdService.registerNode(node);
        return Response.ok().build();
    }

    @GET
    @Path("/databases")
    public java.util.Set<String> listDatabases() {
        return pdService.listDatabases();
    }

    @POST
    @Path("/databases")
    public Response createDatabase(String name) {
        pdService.createDatabase(name);
        return Response.ok().build();
    }

    @jakarta.ws.rs.DELETE
    @Path("/databases/{name}")
    public Response deleteDatabase(@jakarta.ws.rs.PathParam("name") String name) {
        pdService.deleteDatabase(name);
        return Response.ok().build();
    }
}
