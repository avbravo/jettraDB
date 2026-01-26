package io.jettra.example.ui.client;

import io.jettra.example.ui.model.Role;
import io.jettra.example.ui.model.User;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "auth-api")
@Path("/api/web-auth")
public interface AuthClient {

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    List<User> getUsers(@jakarta.ws.rs.HeaderParam("Authorization") String token);

    @GET
    @Path("/roles")
    @Produces(MediaType.APPLICATION_JSON)
    List<Role> getRoles(@jakarta.ws.rs.HeaderParam("Authorization") String token);

    @GET
    @Path("/users/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    User getUser(@jakarta.ws.rs.PathParam("username") String username,
            @jakarta.ws.rs.HeaderParam("Authorization") String token);
}
