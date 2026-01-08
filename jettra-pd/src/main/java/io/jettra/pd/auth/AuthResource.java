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
            return Response.ok(Map.of(
                    "token", token,
                    "mustChangePassword", user.forcePasswordChange())).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @POST
    @Path("/change-password")
    public Response changePassword(ChangePasswordRequest request) {
        // Here we ideally check the token too, but for simplicity we assume the user
        // knows the old password
        if (authService.changePassword(request.username(), request.oldPassword(), request.newPassword())) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
    }
}
