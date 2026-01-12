package io.jettra.web;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/web-auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WebAuthResource {

    @ConfigProperty(name = "jettra.pd.url")
    String pdUrl;


    @POST
    @Path("/login")
    public Response proxyLogin(LoginRequest request) {
        try (Client client = ClientBuilder.newClient()) {
            Response pdResponse = client.target(pdUrl + "/api/auth/login")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(request));

            String entity = pdResponse.readEntity(String.class);
            return Response.status(pdResponse.getStatus())
                    .entity(entity)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().type(MediaType.APPLICATION_JSON).entity("{\"error\":\"Login proxy failed\"}")
                    .build();
        }
    }

    @POST
    @Path("/change-password")
    public Response proxyChangePassword(ChangePasswordRequest request) {
        try (Client client = ClientBuilder.newClient()) {
            Response pdResponse = client.target(pdUrl + "/api/auth/change-password")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(request));

            String entity = pdResponse.readEntity(String.class);
            return Response.status(pdResponse.getStatus())
                    .entity(entity)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"Change password proxy failed\"}").build();
        }
    }
}
