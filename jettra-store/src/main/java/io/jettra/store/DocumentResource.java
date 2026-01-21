package io.jettra.store;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.jettra.engine.document.DocumentEngine;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/document")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentResource {
    private static final Logger LOG = Logger.getLogger(DocumentResource.class);

    @Inject
    DocumentEngine documentEngine;

    @ConfigProperty(name = "jettra.node.id")
    String nodeId;

    @POST
    @Path("/{collection}")
    public Uni<String> save(@PathParam("collection") String collection, 
                            @QueryParam("bucket") String bucket,
                            @QueryParam("jettraID") String jettraId, 
                            String json) {
        String effectiveBucket = (bucket == null ? "default" : bucket);
        // Requirement: jettraID identifies where it is located (physical bucket)
        String finalJettraId = (jettraId == null) 
                ? documentEngine.generateJettraId(nodeId + "/" + effectiveBucket) 
                : jettraId;
        
        LOG.infof("Direct document save request. Collection: %s, jettraID: %s", collection, finalJettraId);
        return documentEngine.save(collection, finalJettraId, json);
    }

    @GET
    @Path("/{collection}")
    public Multi<String> list(@PathParam("collection") String collection) {
        return documentEngine.findAll(collection);
    }

    @GET
    @Path("/{collection}/{jettraID}")
    public Uni<String> get(@PathParam("collection") String collection, 
                           @PathParam("jettraID") String jettraId) {
        return documentEngine.findById(collection, jettraId);
    }

    @GET
    @Path("/{collection}/{jettraID}/versions")
    public Multi<String> getVersions(@PathParam("collection") String collection, 
                                     @PathParam("jettraID") String jettraId) {
        return documentEngine.getDocumentVersions(collection, jettraId);
    }

    @GET
    @Path("/{collection}/search/tag")
    public Multi<String> findByTag(@PathParam("collection") String collection, 
                                   @QueryParam("tag") String tag) {
        return documentEngine.findByTag(collection, tag);
    }

    @DELETE
    @Path("/{collection}/{jettraID}")
    public Uni<Void> delete(@PathParam("collection") String collection, 
                             @PathParam("jettraID") String jettraId) {
        return documentEngine.delete(collection, jettraId);
    }
}
