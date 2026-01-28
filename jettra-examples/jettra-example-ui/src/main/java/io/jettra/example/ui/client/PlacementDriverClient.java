package io.jettra.example.ui.client;

import io.jettra.example.ui.model.Node;
import io.jettra.example.ui.model.RaftGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "pd-api")
@Path("/api/internal/pd")
public interface PlacementDriverClient {

    @GET
    @Path("/nodes")
    @Produces(MediaType.APPLICATION_JSON)
    List<Node> getNodes(@jakarta.ws.rs.HeaderParam("Authorization") String token);

    @GET
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON)
    List<RaftGroup> getGroups(@jakarta.ws.rs.HeaderParam("Authorization") String token);

    @POST
    @Path("/nodes/{id}/stop")
    Response stopNode(@jakarta.ws.rs.PathParam("id") String id,
            @jakarta.ws.rs.HeaderParam("Authorization") String token);

    @GET
    @Path("/databases")
    @Produces(MediaType.APPLICATION_JSON)
    List<io.jettra.example.ui.model.Database> getDatabases(@jakarta.ws.rs.HeaderParam("Authorization") String token);

    @POST
    @Path("/databases")
    @Consumes(MediaType.APPLICATION_JSON)
    Response createDatabase(io.jettra.example.ui.model.Database db, @jakarta.ws.rs.HeaderParam("Authorization") String token);

    @jakarta.ws.rs.PUT
    @Path("/databases/{oldName}")
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateDatabase(@jakarta.ws.rs.PathParam("oldName") String oldName, io.jettra.example.ui.model.Database db, @jakarta.ws.rs.HeaderParam("Authorization") String token);

    @jakarta.ws.rs.DELETE
    @Path("/databases/{name}")
    Response deleteDatabase(@jakarta.ws.rs.PathParam("name") String name, @jakarta.ws.rs.HeaderParam("Authorization") String token);
}
