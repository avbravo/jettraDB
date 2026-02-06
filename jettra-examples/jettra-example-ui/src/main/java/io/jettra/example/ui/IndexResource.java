package io.jettra.example.ui;

import io.jettra.example.ui.client.PlacementDriverClient;
import io.jettra.example.ui.model.Index;
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
import java.util.Map;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/dashboard/index")
public class IndexResource {

    @Inject
    @RestClient
    PlacementDriverClient pdClient;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getIndexView(@CookieParam("auth_token") String token,
            @QueryParam("db") String db,
            @QueryParam("col") String col) {
        if (token == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        token = "Bearer " + token.replace("\"", "");

        Div container = new Div("index-view");
        container.setStyleClass("animate-in fade-in duration-500");

        Div header = new Div("index-header");
        header.setStyleClass("flex justify-between items-center mb-6");

        Div titleGroup = new Div("idx-title-grp");
        titleGroup.addComponent(new Label("idx-h2", "<h2 class='text-2xl font-bold text-white'>Index Management</h2>"));
        titleGroup.addComponent(
                new Label("idx-sub", "<p class='text-sm text-slate-400 font-medium'>" + db + " / " + col + "</p>"));
        header.addComponent(titleGroup);

        Button createBtn = new Button("btn-create-idx", "+ Create Index");
        createBtn.setStyleClass(
                "px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-bold transition-all shadow-lg shadow-indigo-600/20");
        createBtn.addAttribute("onclick", "openIndexModal('" + db + "', '" + col + "')");
        header.addComponent(createBtn);

        container.addComponent(header);

        // Index Table
        Table table = new Table("index-table");
        table.setStyleClass("w-full text-left border-collapse bg-slate-900/40 rounded-xl overflow-hidden");
        table.setHeaders(List.of("Index Name", "Field", "Type", "Status", "Actions"));

        try {
            List<Index> indexes = pdClient.getIndexes(db, col, token);
            if (indexes != null && !indexes.isEmpty()) {
                for (Index idx : indexes) {
                    List<String> row = new ArrayList<>();
                    row.add("<span class='font-bold text-slate-200'>" + idx.getName() + "</span>");
                    row.add("<code class='text-emerald-400'>" + idx.getField() + "</code>");
                    row.add("<span class='px-2 py-0.5 rounded bg-slate-800 text-slate-400 text-[10px] border border-slate-700 uppercase font-bold'>"
                            + idx.getType() + "</span>");
                    row.add("<span class='flex items-center gap-1.5 text-emerald-400 text-xs font-bold'><div class='w-1.5 h-1.5 rounded-full bg-emerald-400'></div> ACTIVE</span>");

                    Div actions = new Div("idx-actions-" + idx.getName());
                    actions.setStyleClass("flex gap-2");

                    Button delBtn = new Button("btn-del-idx-" + idx.getName(), "Delete");
                    delBtn.setStyleClass(
                            "text-xs bg-rose-500/10 text-rose-500 px-3 py-1 rounded-lg hover:bg-rose-500/20 border border-rose-500/20 transition-all font-bold");
                    delBtn.setHxDelete("/dashboard/index/" + idx.getName() + "?db=" + db + "&col=" + col);
                    delBtn.setHxConfirm("Are you sure you want to delete index: " + idx.getName() + "?");
                    delBtn.setHxTarget("#main-content-view");
                    actions.addComponent(delBtn);

                    row.add(actions.render());
                    table.addRow(row);
                }
            } else {
                return Response.ok(container.render()
                        + "<div class='p-12 border-2 border-dashed border-slate-800 rounded-2xl text-center'><p class='text-slate-500 font-medium'>No indexes found. Create one to improve performance.</p></div>")
                        .build();
            }
        } catch (Exception e) {
            container.addComponent(new Label("idx-err",
                    "<div class='p-4 bg-rose-500/10 border border-rose-500/20 rounded-xl text-rose-500 text-sm'>Error loading indexes: "
                            + e.getMessage() + "</div>"));
        }

        container.addComponent(table);

        // Modal Script (Modal itself added in DashboardResource)
        container.addComponent(new Label("idx-script",
                "<script>" +
                        "function openIndexModal(db, col) {" +
                        "  const modal = document.getElementById('index-modal');" +
                        "  if(modal) {" +
                        "    document.getElementById('idx-form-db').value = db;" +
                        "    document.getElementById('idx-form-col').value = col;" +
                        "    modal.classList.remove('hidden');" +
                        "    modal.classList.add('flex');" +
                        "  }" +
                        "}" +
                        "</script>"));

        return Response.ok(container.render()).build();
    }

    @jakarta.ws.rs.DELETE
    @Path("/{name}")
    public Response deleteIndex(@PathParam("name") String name,
            @QueryParam("db") String db,
            @QueryParam("col") String col,
            @CookieParam("auth_token") String token) {
        if (token == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        token = "Bearer " + token.replace("\"", "");

        try {
            pdClient.deleteIndex(db, col, name, token);
            return getIndexView(token.substring(7), db, col);
        } catch (Exception e) {
            return Response.ok("<script>alert('Error deleting index: " + e.getMessage() + "');</script>").build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createIndex(@FormParam("db") String db,
            @FormParam("col") String col,
            @FormParam("field") String field,
            @FormParam("type") String type,
            @CookieParam("auth_token") String token) {
        if (token == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        token = "Bearer " + token.replace("\"", "");

        try {
            pdClient.createIndex(db, col, Map.of("field", field, "type", type), token);
            return Response.ok()
                    .header("HX-Trigger", "{\"closeModal\": \"index-modal\"}")
                    .entity(getIndexView(token.substring(7), db, col).getEntity())
                    .build();
        } catch (Exception e) {
            return Response.ok("<script>alert('Error creating index: " + e.getMessage() + "');</script>").build();
        }
    }
}
