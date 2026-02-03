package io.jettra.example.ui.client;

import io.jettra.example.ui.model.Sequence;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "pd-api")
@Path("/api/v1/sequence")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface SequenceClient {

    @GET
    List<Sequence> list(@QueryParam("database") String database, @jakarta.ws.rs.HeaderParam("Authorization") String token);

    @POST
    Response create(SequenceCreateRequest request, @jakarta.ws.rs.HeaderParam("Authorization") String token);

    @GET
    @Path("/{name}/next")
    Response next(@PathParam("name") String name, @jakarta.ws.rs.HeaderParam("Authorization") String token);

    @DELETE
    @Path("/{name}")
    Response delete(@PathParam("name") String name, @jakarta.ws.rs.HeaderParam("Authorization") String token);

    // Inner DTOs
    public record SequenceCreateRequest(String name, String database, long startValue, long increment) {}
    public record SequenceValueResponse(long value) {}
}
