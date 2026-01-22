package io.jettra.pd;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/sql")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SqlResource {

    @Inject
    SqlService sqlService;

    @POST
    public Response executeSql(
            @jakarta.ws.rs.core.Context jakarta.ws.rs.container.ContainerRequestContext requestContext,
            SqlRequest request) {
        String username = (String) requestContext.getProperty("auth.username");
        if (username == null) {
            username = "guest";
        }

        String result = sqlService.executeSql(request.sql(), username, request.resolveRefs());
        return Response.ok(result).build();
    }

    public record SqlRequest(String sql, boolean resolveRefs) {
    }
}
