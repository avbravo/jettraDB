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
    public java.util.Collection<DatabaseMetadata> listDatabases(
            @jakarta.ws.rs.core.Context jakarta.ws.rs.container.ContainerRequestContext requestContext) {
        String username = (String) requestContext.getProperty("auth.username");
        if (username == null) {
            return java.util.Collections.emptyList();
        }
        return pdService.listDatabases(username);
    }

    @POST
    @Path("/databases")
    public Response createDatabase(
            @jakarta.ws.rs.core.Context jakarta.ws.rs.container.ContainerRequestContext requestContext,
            DatabaseMetadata db) {
        String creator = (String) requestContext.getProperty("auth.username");
        pdService.createDatabase(db, creator != null ? creator : "system");
        return Response.ok().build();
    }

    @jakarta.ws.rs.PUT
    @Path("/databases/{oldName}")
    public Response updateDatabase(@jakarta.ws.rs.PathParam("oldName") String oldName, DatabaseMetadata db) {
        pdService.updateDatabase(oldName, db);
        return Response.ok().build();
    }

    @jakarta.ws.rs.DELETE
    @Path("/databases/{name}")
    public Response deleteDatabase(@jakarta.ws.rs.PathParam("name") String name) {
        pdService.deleteDatabase(name);
        return Response.ok().build();
    }

    @GET
    @Path("/databases/{name}")
    public Response getDatabaseInfo(@jakarta.ws.rs.PathParam("name") String name) {
        DatabaseMetadata db = pdService.getDatabaseInfo(name);
        if (db == null)
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Database not found\"}").build();
        return Response.ok(db).build();
    }

    @GET
    @Path("/databases/{name}/collections")
    public Response listCollections(@jakarta.ws.rs.PathParam("name") String name) {
        DatabaseMetadata db = pdService.getDatabaseInfo(name);
        if (db == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(db.collections()).build();
    }

    @POST
    @Path("/databases/{name}/collections/{colName}")
    public Response addCollection(@jakarta.ws.rs.PathParam("name") String name,
            @jakarta.ws.rs.PathParam("colName") String colName,
            java.util.Map<String, String> body) {
        String engine = (body != null && body.containsKey("engine")) ? body.get("engine") : "Document";
        if (pdService.addCollection(name, colName, engine)) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Database not found\"}").build();
    }

    @jakarta.ws.rs.DELETE
    @Path("/databases/{name}/collections/{colName}")
    public Response removeCollection(@jakarta.ws.rs.PathParam("name") String name,
            @jakarta.ws.rs.PathParam("colName") String colName) {
        pdService.removeCollection(name, colName);
        return Response.ok().build();
    }

    @jakarta.ws.rs.PUT
    @Path("/databases/{name}/collections/{oldName}/{newName}")
    public Response renameCollection(@jakarta.ws.rs.PathParam("name") String name,
            @jakarta.ws.rs.PathParam("oldName") String oldName,
            @jakarta.ws.rs.PathParam("newName") String newName) {
        pdService.renameCollection(name, oldName, newName);
        return Response.ok().build();
    }

    @POST
    @Path("/nodes/{id}/stop")
    public Response stopNode(@jakarta.ws.rs.PathParam("id") String id) {
        pdService.stopNode(id);
        return Response.ok().build();
    }

    @jakarta.annotation.security.RolesAllowed({ "system", "admin" })
    @POST
    @Path("/stop")
    public Response stop() {
        new Thread(() -> {
            try {
                Thread.sleep(500);
                io.quarkus.runtime.Quarkus.asyncExit(0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        return Response.ok("{\"status\":\"stopping\"}").build();
    }

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok("{\"status\":\"UP\"}").build();
    }
}
