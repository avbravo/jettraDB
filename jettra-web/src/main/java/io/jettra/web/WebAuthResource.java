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

    @jakarta.ws.rs.core.Context
    jakarta.ws.rs.core.HttpHeaders headers;

    private String getAuthHeader() {
        return headers.getHeaderString(jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION);
    }

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

    // User Management Proxy
    @jakarta.ws.rs.GET
    @Path("/users")
    public Response proxyListUsers() {
        try (Client client = ClientBuilder.newClient()) {
            return client.target(pdUrl + "/api/auth/users")
                    .request(MediaType.APPLICATION_JSON)
                    .header(jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .get();
        }
    }

    @POST
    @Path("/users")
    public Response proxyCreateUser(String json) {
        try (Client client = ClientBuilder.newClient()) {
            return client.target(pdUrl + "/api/auth/users")
                    .request(MediaType.APPLICATION_JSON)
                    .header(jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .post(Entity.json(json));
        }
    }

    @jakarta.ws.rs.PUT
    @Path("/users/{username}")
    public Response proxyUpdateUser(@jakarta.ws.rs.PathParam("username") String username, String json) {
        try (Client client = ClientBuilder.newClient()) {
            return client.target(pdUrl + "/api/auth/users/" + username)
                    .request(MediaType.APPLICATION_JSON)
                    .header(jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .put(Entity.json(json));
        }
    }

    @jakarta.ws.rs.DELETE
    @Path("/users/{username}")
    public Response proxyDeleteUser(@jakarta.ws.rs.PathParam("username") String username) {
        try (Client client = ClientBuilder.newClient()) {
            return client.target(pdUrl + "/api/auth/users/" + username)
                    .request(MediaType.APPLICATION_JSON)
                    .header(jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .delete();
        }
    }

    // Role Management Proxy
    @jakarta.ws.rs.GET
    @Path("/roles")
    public Response proxyListRoles() {
        try (Client client = ClientBuilder.newClient()) {
            return client.target(pdUrl + "/api/auth/roles")
                    .request(MediaType.APPLICATION_JSON)
                    .header(jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .get();
        }
    }

    @POST
    @Path("/roles")
    public Response proxyCreateRole(String json) {
        try (Client client = ClientBuilder.newClient()) {
            return client.target(pdUrl + "/api/auth/roles")
                    .request(MediaType.APPLICATION_JSON)
                    .header(jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .post(Entity.json(json));
        }
    }

    @jakarta.ws.rs.PUT
    @Path("/roles/{name}")
    public Response proxyUpdateRole(@jakarta.ws.rs.PathParam("name") String name, String json) {
        try (Client client = ClientBuilder.newClient()) {
            return client.target(pdUrl + "/api/auth/roles/" + name)
                    .request(MediaType.APPLICATION_JSON)
                    .header(jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .put(Entity.json(json));
        }
    }

    @jakarta.ws.rs.DELETE
    @Path("/roles/{name}")
    public Response proxyDeleteRole(@jakarta.ws.rs.PathParam("name") String name) {
        try (Client client = ClientBuilder.newClient()) {
            return client.target(pdUrl + "/api/auth/roles/" + name)
                    .request(MediaType.APPLICATION_JSON)
                    .header(jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .delete();
        }
    }
}
