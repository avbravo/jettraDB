package io.jettra.example.ui;

import io.jettra.example.ui.client.PlacementDriverClient;
import io.jettra.example.ui.model.Rule;
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

@Path("/dashboard/rule")
public class RuleResource {

    @Inject
    @RestClient
    PlacementDriverClient pdClient;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getRuleView(@CookieParam("auth_token") String token,
            @QueryParam("db") String db,
            @QueryParam("col") String col) {
        if (token == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        token = "Bearer " + token.replace("\"", "");

        Div container = new Div("rule-view");
        container.setStyleClass("animate-in fade-in duration-500");

        Div header = new Div("rule-header");
        header.setStyleClass("flex justify-between items-center mb-6");

        Div titleGroup = new Div("rule-title-grp");
        titleGroup.addComponent(new Label("rule-h2", "<h2 class='text-2xl font-bold text-white'>Rule Engine</h2>"));
        titleGroup.addComponent(
                new Label("rule-sub", "<p class='text-sm text-slate-400 font-medium'>" + db + " / " + col + "</p>"));
        header.addComponent(titleGroup);

        Button createBtn = new Button("btn-create-rule", "+ Add Rule");
        createBtn.setStyleClass(
                "px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-bold transition-all shadow-lg shadow-indigo-600/20");
        createBtn.addAttribute("onclick", "openRuleModal('" + db + "', '" + col + "')");
        header.addComponent(createBtn);

        container.addComponent(header);

        // Rule Table
        Table table = new Table("rule-table");
        table.setStyleClass("w-full text-left border-collapse bg-slate-900/40 rounded-xl overflow-hidden");
        table.setHeaders(List.of("Rule Name", "Condition", "Action", "Status", "Actions"));

        try {
            List<Rule> rules = pdClient.getRules(db, col, token);
            if (rules != null && !rules.isEmpty()) {
                for (Rule rule : rules) {
                    List<String> row = new ArrayList<>();
                    row.add("<span class='font-bold text-slate-200'>" + rule.getName() + "</span>");
                    row.add("<code class='text-amber-400'>" + rule.getCondition() + "</code>");
                    row.add("<code class='text-indigo-400'>" + rule.getAction() + "</code>");
                    String statusColor = rule.isActive() ? "emerald" : "slate";
                    row.add("<span class='flex items-center gap-1.5 text-" + statusColor
                            + "-400 text-xs font-bold'><div class='w-1.5 h-1.5 rounded-full bg-" + statusColor
                            + "-400'></div> " + (rule.isActive() ? "ACTIVE" : "INACTIVE") + "</span>");

                    Div actions = new Div("rule-actions-" + rule.getName());
                    actions.setStyleClass("flex gap-2");

                    Button delBtn = new Button("btn-del-rule-" + rule.getName(), "Delete");
                    delBtn.setStyleClass(
                            "text-xs bg-rose-500/10 text-rose-500 px-3 py-1 rounded-lg hover:bg-rose-500/20 border border-rose-500/20 transition-all font-bold");
                    delBtn.setHxDelete("/dashboard/rule/" + rule.getName() + "?db=" + db + "&col=" + col);
                    delBtn.setHxConfirm("Are you sure you want to delete rule: " + rule.getName() + "?");
                    delBtn.setHxTarget("#main-content-view");
                    actions.addComponent(delBtn);

                    row.add(actions.render());
                    table.addRow(row);
                }
            } else {
                return Response.ok(container.render()
                        + "<div class='p-12 border-2 border-dashed border-slate-800 rounded-2xl text-center'><p class='text-slate-500 font-medium'>No rules defined for this collection.</p></div>")
                        .build();
            }
        } catch (Exception e) {
            container.addComponent(new Label("rule-err",
                    "<div class='p-4 bg-rose-500/10 border border-rose-500/20 rounded-xl text-rose-500 text-sm'>Error loading rules: "
                            + e.getMessage() + "</div>"));
        }

        container.addComponent(table);

        // Modal Script
        container.addComponent(new Label("rule-script",
                "<script>" +
                        "function openRuleModal(db, col) {" +
                        "  const modal = document.getElementById('rule-modal');" +
                        "  if(modal) {" +
                        "    document.getElementById('rule-form-db').value = db;" +
                        "    document.getElementById('rule-form-col').value = col;" +
                        "    modal.classList.remove('hidden');" +
                        "    modal.classList.add('flex');" +
                        "  }" +
                        "}" +
                        "</script>"));

        return Response.ok(container.render()).build();
    }

    @jakarta.ws.rs.DELETE
    @Path("/{name}")
    public Response deleteRule(@PathParam("name") String name,
            @QueryParam("db") String db,
            @QueryParam("col") String col,
            @CookieParam("auth_token") String token) {
        if (token == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        token = "Bearer " + token.replace("\"", "");

        try {
            pdClient.deleteRule(db, col, name, token);
            return getRuleView(token.substring(7), db, col);
        } catch (Exception e) {
            return Response.ok("<script>alert('Error deleting rule: " + e.getMessage() + "');</script>").build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createRule(@FormParam("db") String db,
            @FormParam("col") String col,
            @FormParam("name") String name,
            @FormParam("condition") String condition,
            @FormParam("action") String action,
            @CookieParam("auth_token") String token) {
        if (token == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        token = "Bearer " + token.replace("\"", "");

        try {
            Rule rule = new Rule(name, condition, action, true);
            pdClient.createRule(db, col, rule, token);
            return Response.ok()
                    .header("HX-Trigger", "{\"closeModal\": \"rule-modal\"}")
                    .entity(getRuleView(token.substring(7), db, col).getEntity())
                    .build();
        } catch (Exception e) {
            return Response.ok("<script>alert('Error creating rule: " + e.getMessage() + "');</script>").build();
        }
    }
}
