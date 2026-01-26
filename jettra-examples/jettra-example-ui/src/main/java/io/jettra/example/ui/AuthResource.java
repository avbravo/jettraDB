package io.jettra.example.ui;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import java.net.URI;

@Path("/auth")
public class AuthResource {

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "jettra.pd.addr", defaultValue = "jettra-pd:8080")
    String pdAddr;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("email") String email,
            @FormParam("username") String username,
            @FormParam("password") String password) {

        String loginUser = (email != null && !email.isEmpty()) ? email : username;

        if (loginUser != null && !loginUser.isEmpty() && password != null && !password.isEmpty()) {
            try {
                // Using Jettra Driver as requested for true Jettra User validation
                io.jettra.driver.JettraReactiveClient client = new io.jettra.driver.JettraReactiveClient(pdAddr);

                // Execute login via driver
                String token = client.login(loginUser, password).await().indefinitely();

                if (token != null) {
                    // Success
                    NewCookie sessionCookie = new NewCookie.Builder("user_session")
                            .value(loginUser)
                            .path("/")
                            .build();

                    NewCookie tokenCookie = new NewCookie.Builder("auth_token")
                            .value(token)
                            .path("/")
                            .build();

                    return Response.ok()
                            .header("HX-Redirect", "/dashboard")
                            .cookie(sessionCookie)
                            .cookie(tokenCookie)
                            .build();
                }
            } catch (Exception e) {
                // If login fails, driver throws Exception
                return returnErrorForm("Invalid Jettra User credentials or Service Unavailable");
            }
        }

        return returnErrorForm("Username and password are required");
    }

    private Response returnErrorForm(String message) {
        io.jettra.example.ui.form.LoginForm form = new io.jettra.example.ui.form.LoginForm("login-form");
        form.setError(message);
        return Response.ok(form.render()).build();
    }

    @POST
    @Path("/logout")
    public Response logout() {
        NewCookie expiredCookie = new NewCookie.Builder("user_session")
                .value("")
                .path("/")
                .maxAge(0) // Expire immediately
                .build();

        return Response.ok()
                .header("HX-Redirect", "/")
                .cookie(expiredCookie)
                .build();
    }
}
