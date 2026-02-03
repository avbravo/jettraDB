package io.jettra.example.ui;

import io.jettra.example.ui.client.SequenceClient;
import io.jettra.example.ui.model.Sequence;
import io.jettra.ui.component.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/dashboard/sequence")
public class SequenceResource {

    @Inject
    @RestClient
    SequenceClient sequenceClient;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getSequenceView(@CookieParam("auth_token") String token, @QueryParam("db") String db) {
        if (token == null) return Response.status(Response.Status.UNAUTHORIZED).build();
        token = "Bearer " + token.replace("\"", "");

        Div container = new Div("sequence-view");
        
        Div header = new Div("seq-header");
        header.setStyleClass("flex justify-between items-center mb-6");
        header.addComponent(new Label("h2", "<h2 class='text-2xl font-bold text-white'>Sequence Management</h2>"));
        
        Button createBtn = new Button("btn-create-seq", "+ Create Sequence");
        createBtn.setStyleClass("btn bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2 px-4 rounded");
        createBtn.addAttribute("onclick", "openSequenceModal('', '" + (db != null ? db : "") + "')");
        header.addComponent(createBtn);
        
        container.addComponent(header);

        // Sequence Table
        Table table = new Table("sequence-table");
        table.setStyleClass("w-full text-left border-collapse");
        table.setHeaders(List.of("Name", "Database", "Current Value", "Increment", "Actions"));

        try {
            List<Sequence> sequences = sequenceClient.list(db, token); 
            if (sequences != null) {
                for (Sequence seq : sequences) {
                    List<String> row = new ArrayList<>();
                    row.add(seq.name());
                    row.add(new Badge("db-badge", seq.database()).setColor("indigo").render());
                    row.add("<span class='font-mono text-emerald-400'>" + seq.currentValue() + "</span>");
                    row.add(String.valueOf(seq.increment()));
                    
                    Div actions = new Div("actions-" + seq.name());
                    actions.setStyleClass("flex gap-2");
                    
                    Button nextBtn = new Button("btn-next-" + seq.name(), "Next");
                    nextBtn.setStyleClass("text-xs bg-emerald-600/20 text-emerald-400 px-2 py-1 rounded hover:bg-emerald-600/40 border border-emerald-600/30");
                    nextBtn.setHxPost("/dashboard/sequence/" + seq.name() + "/next" + (db != null ? "?db=" + db : ""));
                    nextBtn.setHxSwap("outerHTML");
                    nextBtn.setHxTarget("#sequence-table-container"); 
                    actions.addComponent(nextBtn);

                    Button delBtn = new Button("btn-del-" + seq.name(), "Delete");
                    delBtn.setStyleClass("text-xs bg-rose-600/20 text-rose-400 px-2 py-1 rounded hover:bg-rose-600/40 border border-rose-600/30");
                    delBtn.setHxDelete("/dashboard/sequence/" + seq.name() + (db != null ? "?db=" + db : ""));
                    delBtn.setHxConfirm("Are you sure you want to delete sequence " + seq.name() + "?");
                    delBtn.setHxSwap("outerHTML");
                    delBtn.setHxTarget("#sequence-table-container"); 
                    actions.addComponent(delBtn);

                    row.add(actions.render());
                    table.addRow(row);
                }
            }
        } catch (Exception e) {
            container.addComponent(new Label("err", "<div class='text-rose-500'>Error loading sequences: " + e.getMessage() + "</div>"));
        }

        Div tableContainer = new Div("sequence-table-container");
        tableContainer.addComponent(table);
        container.addComponent(tableContainer);

        return Response.ok(container.render()).build();
    }

    @POST
    @Path("/{name}/next")
    public Response nextSequence(@PathParam("name") String name, @QueryParam("db") String db, @CookieParam("auth_token") String token) {
        if (token == null) return Response.status(Response.Status.UNAUTHORIZED).build();
        token = "Bearer " + token.replace("\"", "");

        try {
            sequenceClient.next(name, token);
            return getSequenceView(token.substring(7), db); 
        } catch (Exception e) {
            return Response.ok("<script>alert('Error: " + e.getMessage() + "');</script>").build();
        }
    }

    @jakarta.ws.rs.DELETE
    @Path("/{name}")
    public Response deleteSequence(@PathParam("name") String name, @QueryParam("db") String db, @CookieParam("auth_token") String token) {
         if (token == null) return Response.status(Response.Status.UNAUTHORIZED).build();
        token = "Bearer " + token.replace("\"", "");
        
        try {
            sequenceClient.delete(name, token);
            return getSequenceView(token.substring(7), db); 
        } catch (Exception e) {
             return Response.ok("<script>alert('Error deleting: " + e.getMessage() + "');</script>").build();
        }
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createSequence(@FormParam("name") String name, @FormParam("db") String db, 
                                   @FormParam("start") long start, @FormParam("inc") long inc,
                                   @CookieParam("auth_token") String token) {
         if (token == null) return Response.status(Response.Status.UNAUTHORIZED).build();
        token = "Bearer " + token.replace("\"", "");

        try {
            sequenceClient.create(new SequenceClient.SequenceCreateRequest(name, db, start, inc), token);
            return Response.ok()
                    .header("HX-Trigger", "{\"refreshSequences\": \"Created sequence " + name + "\", \"closeModal\": \"sequence-modal\"}")
                    .entity(getSequenceView(token.substring(7), db).getEntity())
                    .build();
        } catch (Exception e) {
            return Response.ok("<script>alert('Error creating: " + e.getMessage() + "');</script>").build();
        }
    }
}
