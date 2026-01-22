package io.jettra.pd;

import java.util.List;

import jakarta.inject.Inject;
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

@Path("/api/v1/sequence")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SequenceResource {

    @Inject
    SequenceService sequenceService;

    @GET
    public List<SequenceMetadata> list(@QueryParam("database") String database) {
        return sequenceService.listSequences(database);
    }

    @POST
    public Response create(SequenceCreateRequest request) {
        sequenceService.createSequence(request.name(), request.database(), request.startValue(), request.increment());
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{name}/next")
    public Response next(@PathParam("name") String name) {
        Long val = sequenceService.nextValue(name);
        if (val == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(new SequenceValueResponse(val)).build();
    }

    @GET
    @Path("/{name}/current")
    public Response current(@PathParam("name") String name) {
        Long val = sequenceService.currentValue(name);
        if (val == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(new SequenceValueResponse(val)).build();
    }

    @POST
    @Path("/{name}/reset")
    public Response reset(@PathParam("name") String name, SequenceResetRequest request) {
        boolean success = sequenceService.resetSequence(name, request.newValue());
        if (!success) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("/{name}")
    public Response delete(@PathParam("name") String name) {
        boolean success = sequenceService.deleteSequence(name);
        if (!success) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

    public record SequenceCreateRequest(String name, String database, long startValue, long increment) {
    }

    public record SequenceResetRequest(long newValue) {
    }

    public record SequenceValueResponse(long value) {
    }
}
