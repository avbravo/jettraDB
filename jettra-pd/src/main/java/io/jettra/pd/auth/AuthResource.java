package io.jettra.pd.auth;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    public record LoginRequest(String username, String password) {
    }

    public record ChangePasswordRequest(String username, String oldPassword, String newPassword) {
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        User user = authService.authenticate(request.username(), request.password());
        if (user != null) {
            String token = TokenUtils.generateToken(user.username(), user.roles());
            boolean isAdmin = "admin".equals(user.username()) || user.roles().contains("admin");
            return Response.ok(Map.of(
                    "token", token,
                    "mustChangePassword", user.forcePasswordChange(),
                    "isAdmin", isAdmin)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @POST
    @Path("/change-password")
    public Response changePassword(ChangePasswordRequest request) {
        if (authService.changePassword(request.username(), request.oldPassword(), request.newPassword())) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
    }

    // User Management API
    @jakarta.ws.rs.GET
    @Path("/users")
    public Response listUsers() {
        return Response.ok(authService.listUsers()).build();
    }

    @POST
    @Path("/users")
    public Response createUser(User user) {
        authService.createUser(user);
        return Response.ok().build();
    }

    @jakarta.ws.rs.PUT
    @Path("/users/{username}")
    public Response updateUser(@jakarta.ws.rs.PathParam("username") String username, User user) {
        // Ensure the path username matches the record username, or just use the record
        // one
        authService.updateUser(user);
        return Response.ok().build();
    }

    @jakarta.ws.rs.DELETE
    @Path("/users/{username}")
    public Response deleteUser(@jakarta.ws.rs.PathParam("username") String username) {
        authService.deleteUser(username);
        return Response.ok().build();
    }

    // Role Management API
    @jakarta.ws.rs.GET
    @Path("/roles")
    public Response listRoles() {
        return Response.ok(authService.listRoles()).build();
    }

    @POST
    @Path("/roles")
    public Response createRole(Role role) {
        authService.createRole(role);
        return Response.ok().build();
    }

    @jakarta.ws.rs.PUT
    @Path("/roles/{name}")
    public Response updateRole(@jakarta.ws.rs.PathParam("name") String name, Role role) {
        authService.updateRole(role);
        return Response.ok().build();
    }

    @jakarta.ws.rs.DELETE
    @Path("/roles/{name}")
    public Response deleteRole(@jakarta.ws.rs.PathParam("name") String name) {
        authService.deleteRole(name);
        return Response.ok().build();
    }

    @POST
    @Path("/databases/{dbName}/sync-roles")
    public Response syncDatabaseRoles(@jakarta.ws.rs.PathParam("dbName") String dbName,
            Map<String, String> userRoleMapping) {
        authService.syncDatabaseRoles(dbName, userRoleMapping);
        return Response.ok().build();
    }
}
