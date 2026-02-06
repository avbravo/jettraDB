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
        List<io.jettra.example.ui.model.Database> getDatabases(
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        @POST
        @Path("/databases")
        @Consumes(MediaType.APPLICATION_JSON)
        Response createDatabase(io.jettra.example.ui.model.Database db,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        @jakarta.ws.rs.PUT
        @Path("/databases/{oldName}")
        @Consumes(MediaType.APPLICATION_JSON)
        Response updateDatabase(@jakarta.ws.rs.PathParam("oldName") String oldName,
                        io.jettra.example.ui.model.Database db,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        @jakarta.ws.rs.DELETE
        @Path("/databases/{name}")
        Response deleteDatabase(@jakarta.ws.rs.PathParam("name") String name,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        // Collection Management
        @GET
        @Path("/databases/{dbName}/collections")
        @Produces(MediaType.APPLICATION_JSON)
        List<io.jettra.example.ui.model.Collection> getCollections(@jakarta.ws.rs.PathParam("dbName") String dbName,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        @POST
        @Path("/databases/{dbName}/collections/{colName}")
        @Consumes(MediaType.APPLICATION_JSON)
        Response createCollection(@jakarta.ws.rs.PathParam("dbName") String dbName,
                        @jakarta.ws.rs.PathParam("colName") String colName, io.jettra.example.ui.model.Collection col,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        @jakarta.ws.rs.PUT
        @Path("/databases/{dbName}/collections/{oldName}/{newName}")
        @Consumes(MediaType.APPLICATION_JSON)
        Response renameCollection(@jakarta.ws.rs.PathParam("dbName") String dbName,
                        @jakarta.ws.rs.PathParam("oldName") String oldName,
                        @jakarta.ws.rs.PathParam("newName") String newName,
                        io.jettra.example.ui.model.Collection col,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        @jakarta.ws.rs.DELETE
        @Path("/databases/{dbName}/collections/{colName}")
        Response deleteCollection(@jakarta.ws.rs.PathParam("dbName") String dbName,
                        @jakarta.ws.rs.PathParam("colName") String colName,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        // Index Management
        @GET
        @Path("/databases/{dbName}/collections/{colName}/indexes")
        @Produces(MediaType.APPLICATION_JSON)
        List<io.jettra.example.ui.model.Index> getIndexes(@jakarta.ws.rs.PathParam("dbName") String dbName,
                        @jakarta.ws.rs.PathParam("colName") String colName,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        @POST
        @Path("/databases/{dbName}/collections/{colName}/indexes")
        @Consumes(MediaType.APPLICATION_JSON)
        Response createIndex(@jakarta.ws.rs.PathParam("dbName") String dbName,
                        @jakarta.ws.rs.PathParam("colName") String colName, java.util.Map<String, String> body,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        @jakarta.ws.rs.DELETE
        @Path("/databases/{dbName}/collections/{colName}/indexes/{indexName}")
        Response deleteIndex(@jakarta.ws.rs.PathParam("dbName") String dbName,
                        @jakarta.ws.rs.PathParam("colName") String colName,
                        @jakarta.ws.rs.PathParam("indexName") String indexName,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        // Rule Management
        @GET
        @Path("/databases/{dbName}/collections/{colName}/rules")
        @Produces(MediaType.APPLICATION_JSON)
        List<io.jettra.example.ui.model.Rule> getRules(@jakarta.ws.rs.PathParam("dbName") String dbName,
                        @jakarta.ws.rs.PathParam("colName") String colName,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        @POST
        @Path("/databases/{dbName}/collections/{colName}/rules")
        @Consumes(MediaType.APPLICATION_JSON)
        Response createRule(@jakarta.ws.rs.PathParam("dbName") String dbName,
                        @jakarta.ws.rs.PathParam("colName") String colName, io.jettra.example.ui.model.Rule rule,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        @jakarta.ws.rs.DELETE
        @Path("/databases/{dbName}/collections/{colName}/rules/{ruleName}")
        Response deleteRule(@jakarta.ws.rs.PathParam("dbName") String dbName,
                        @jakarta.ws.rs.PathParam("colName") String colName,
                        @jakarta.ws.rs.PathParam("ruleName") String ruleName,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        @POST
        @Path("/databases/{dbName}/backup")
        @Produces(MediaType.APPLICATION_JSON)
        Response backup(@jakarta.ws.rs.PathParam("dbName") String dbName,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);

        @POST
        @Path("/databases/{dbName}/restore")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        Response restore(@jakarta.ws.rs.PathParam("dbName") String dbName, java.util.Map<String, String> body,
                        @jakarta.ws.rs.HeaderParam("Authorization") String token);
}
