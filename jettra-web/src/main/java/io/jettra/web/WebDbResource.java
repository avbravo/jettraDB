package io.jettra.web;

import java.util.Collections;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/db")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WebDbResource {

    @ConfigProperty(name = "jettra.pd.url")
    String pdUrl;

    @jakarta.ws.rs.core.Context
    HttpHeaders headers;

    private String getAuthHeader() {
        return headers.getHeaderString(HttpHeaders.AUTHORIZATION);
    }

    @GET
    public java.util.Collection<io.jettra.pd.DatabaseMetadata> listDatabases() {
        try (Client client = ClientBuilder.newClient()) {
            return client.target(pdUrl + "/api/internal/pd/databases")
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .get(new GenericType<java.util.List<io.jettra.pd.DatabaseMetadata>>() {
                    });
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @POST
    public Response createDatabase(io.jettra.pd.DatabaseMetadata db) {
        try (Client client = ClientBuilder.newClient()) {
            Response response = client.target(pdUrl + "/api/internal/pd/databases")
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .post(Entity.entity(db, MediaType.APPLICATION_JSON));
            return Response.status(response.getStatus()).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @jakarta.ws.rs.PUT
    @Path("/{oldName}")
    public Response updateDatabase(@PathParam("oldName") String oldName, io.jettra.pd.DatabaseMetadata db) {
        try (Client client = ClientBuilder.newClient()) {
            Response response = client.target(pdUrl + "/api/internal/pd/databases/" + oldName)
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .put(Entity.entity(db, MediaType.APPLICATION_JSON));
            return Response.status(response.getStatus()).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("/{name}")
    public Response deleteDatabase(@PathParam("name") String name) {
        try (Client client = ClientBuilder.newClient()) {
            Response response = client.target(pdUrl + "/api/internal/pd/databases/" + name)
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .delete();
            return Response.status(response.getStatus()).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }
}
