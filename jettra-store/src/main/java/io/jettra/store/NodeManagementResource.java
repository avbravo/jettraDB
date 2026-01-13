package io.jettra.store;

import org.jboss.logging.Logger;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/")
public class NodeManagementResource {
    private static final Logger LOG = Logger.getLogger(NodeManagementResource.class);

    @jakarta.inject.Inject
    PDConnector pdConnector;

    @jakarta.annotation.security.RolesAllowed({ "system", "admin" })
    @POST
    @Path("/stop")
    @jakarta.ws.rs.Consumes(jakarta.ws.rs.core.MediaType.WILDCARD)
    public Response stopNodeRoot() {
        return stopNode();
    }

    @jakarta.annotation.security.RolesAllowed({ "system", "admin" })
    @POST
    @Path("/api/internal/node/stop")
    @jakarta.ws.rs.Consumes(jakarta.ws.rs.core.MediaType.WILDCARD)
    public Response stopNode() {
        LOG.info("Received stop request from PD. Shutting down...");
        pdConnector.stop();

        // Execute async exit to stop the container/node
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
