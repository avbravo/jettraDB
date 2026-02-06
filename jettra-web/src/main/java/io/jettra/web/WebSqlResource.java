package io.jettra.web;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/web/sql")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WebSqlResource {

    @ConfigProperty(name = "jettra.pd.url")
    String pdUrl;

    @jakarta.ws.rs.core.Context
    HttpHeaders headers;

    private String getAuthHeader() {
        return headers.getHeaderString(HttpHeaders.AUTHORIZATION);
    }

    @POST
    public Response executeSql(java.util.Map<String, Object> body) {
        try (Client client = ClientBuilder.newClient()) {
            Response response = client.target(pdUrl + "/api/v1/sql")
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .post(Entity.json(body));
            
            if (response.hasEntity()) {
                String entity = response.readEntity(String.class);
                return Response.status(response.getStatus()).entity(entity).build();
            }
            return Response.status(response.getStatus()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }
}
