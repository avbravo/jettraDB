package io.jettra.store;

import io.quarkus.runtime.Quarkus;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/api/internal/node")
public class NodeManagementResource {
    private static final Logger LOG = Logger.getLogger(NodeManagementResource.class);

    @jakarta.annotation.security.PermitAll
    @POST
    @Path("/stop")
    public Response stopNode() {
        LOG.info("Received stop request from PD. Shutting down...");
        // Schedule shutdown in a separate thread so we can return the 200 OK response
        new Thread(() -> {
            try {
                Thread.sleep(500); // Small delay to allow response to be sent
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            LOG.info("Initiating Quarkus shutdown...");
            Quarkus.asyncExit();
        }).start();
        return Response.ok("{\"status\":\"stopping\"}").build();
    }
}
