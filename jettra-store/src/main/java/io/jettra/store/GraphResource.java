package io.jettra.store;

import org.jboss.logging.Logger;

import io.jettra.engine.graph.GraphEngine;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/graph")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GraphResource {
    private static final Logger LOG = Logger.getLogger(GraphResource.class);

    @Inject
    GraphEngine graphEngine;

    @POST
    @Path("/vertex")
    public Uni<Void> addVertex(GraphEngine.Vertex vertex) {
        LOG.infof("Adding vertex: %s", vertex.id());
        return graphEngine.addVertex(vertex);
    }

    @POST
    @Path("/edge")
    public Uni<Void> addEdge(GraphEngine.Edge edge) {
        LOG.infof("Adding edge from %s to %s", edge.fromId(), edge.toId());
        return graphEngine.addEdge(edge);
    }

    @GET
    @Path("/traverse/{startId}")
    public Multi<GraphEngine.Vertex> traverse(
            @PathParam("startId") String startId,
            @QueryParam("depth") @jakarta.ws.rs.DefaultValue("3") int depth) {
        LOG.infof("Traversing graph from %s with depth %d", startId, depth);
        return graphEngine.traverse(startId, depth);
    }
}
