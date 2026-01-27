package io.jettra.example.ui.client;

import io.jettra.example.ui.model.Role;
import io.jettra.example.ui.model.User;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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

    @POST
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response createUser(User user, @jakarta.ws.rs.HeaderParam("Authorization") String token);

    @PUT
    @Path("/users/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateUser(@jakarta.ws.rs.PathParam("username") String username, User user,
            @jakarta.ws.rs.HeaderParam("Authorization") String token);

    @DELETE
    @Path("/users/{username}")
    Response deleteUser(@jakarta.ws.rs.PathParam("username") String username,
            @jakarta.ws.rs.HeaderParam("Authorization") String token);
}
