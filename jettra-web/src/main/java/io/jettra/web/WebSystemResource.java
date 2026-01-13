package io.jettra.web;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/")
public class WebSystemResource {
    private static final Logger LOG = Logger.getLogger(WebSystemResource.class);

    @jakarta.annotation.security.RolesAllowed({ "system", "admin" })
    @POST
    @Path("/stop")
    public Response stop() {
        LOG.info("Received stop request. Shutting down JettraWeb...");
        new Thread(() -> {
            try {
                Thread.sleep(500);
                io.quarkus.runtime.Quarkus.asyncExit(0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        return Response.ok("{\"status\":\"stopping\"}").build();
    }
}
