package io.jettra.example.ui;

import io.jettra.example.ui.client.PlacementDriverClient;
import io.jettra.ui.component.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/dashboard/maintenance")
public class MaintenanceResource {

    @Inject
    @RestClient
    PlacementDriverClient pdClient;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getMaintenanceView(@CookieParam("auth_token") String token, @QueryParam("db") String db) {
        if (token == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        token = "Bearer " + token.replace("\"", "");

        Div container = new Div("maintenance-view");
        container.setStyleClass("animate-in fade-in duration-500 max-w-4xl mx-auto");

        Div header = new Div("maint-header");
        header.setStyleClass("mb-8");
        header.addComponent(
                new Label("maint-h2", "<h2 class='text-3xl font-bold text-white mb-2'>Maintenance & Operations</h2>"));
        header.addComponent(new Label("maint-sub",
                "<p class='text-slate-400'>Perform administrative tasks like backups, restores, and data exports.</p>"));
        container.addComponent(header);

        Div grid = new Div("maint-grid");
        grid.setStyleClass("grid grid-cols-1 md:grid-cols-2 gap-6");

        // Backup Card
        Card backupCard = new Card("card-backup");
        backupCard.setStyleClass("p-6 bg-slate-900/40 border-slate-800 hover:border-indigo-500/50 transition-all");
        backupCard.addComponent(new Label("bk-icon", "<div class='text-3xl mb-4'>💾</div>"));
        backupCard.addComponent(
                new Label("bk-title", "<h3 class='text-xl font-bold text-white mb-2'>Database Backup</h3>"));

        if (db != null && !db.isEmpty()) {
            backupCard.addComponent(new Label("bk-desc",
                    "<p class='text-sm text-slate-400 mb-6'>Create a full snapshot of database <strong>" + db
                            + "</strong>. Backups are stored in the cluster's storage layer.</p>"));
            Button backupBtn = new Button("btn-backup", "Initiate Backup");
            backupBtn.setStyleClass(
                    "w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-bold transition-all");
            backupBtn.setHxPost("/dashboard/maintenance/backup?db=" + db);
            backupBtn.setHxSwap("none");
            backupCard.addComponent(backupBtn);
        } else {
            backupCard.addComponent(new Label("bk-desc",
                    "<p class='text-sm text-amber-500/80 mb-6 italic opacity-80'>Please select a database from the Data Explorer to perform a backup.</p>"));
            Button selectBtn = new Button("btn-maint-sel", "Select Database");
            selectBtn.setStyleClass(
                    "w-full py-2.5 bg-slate-800 text-slate-500 rounded-xl font-bold cursor-not-allowed opacity-50");
            backupCard.addComponent(selectBtn);
        }
        grid.addComponent(backupCard);

        // Restore Card
        Card restoreCard = new Card("card-restore");
        restoreCard.setStyleClass("p-6 bg-slate-900/40 border-slate-800 hover:border-rose-500/50 transition-all");
        restoreCard.addComponent(new Label("rs-icon", "<div class='text-3xl mb-4'>🔄</div>"));
        restoreCard
                .addComponent(new Label("rs-title", "<h3 class='text-xl font-bold text-white mb-2'>Restore Data</h3>"));
        restoreCard.addComponent(new Label("rs-desc",
                "<p class='text-sm text-slate-400 mb-6'>Restore a database from a previous snapshot. <span class='text-rose-400 font-bold'>Warning:</span> This will overwrite current data.</p>"));

        Button restoreBtn = new Button("btn-restore", "Initiate Restore");
        restoreBtn.setStyleClass(
                "w-full py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl font-bold transition-all border border-slate-700");
        restoreBtn.addAttribute("onclick",
                "alert('Please use the Jettra CLI for restore operations in this version.')");
        restoreCard.addComponent(restoreBtn);
        grid.addComponent(restoreCard);

        container.addComponent(grid);

        return Response.ok(container.render()).build();
    }

    @POST
    @Path("/backup")
    public Response runBackup(@QueryParam("db") String db, @CookieParam("auth_token") String token) {
        if (token == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        token = "Bearer " + token.replace("\"", "");

        try {
            pdClient.backup(db, token);
            return Response
                    .ok("<script>alert('Backup initiated for " + db + ". Check system logs for progress.');</script>")
                    .build();
        } catch (Exception e) {
            return Response.ok("<script>alert('Error initiating backup: " + e.getMessage() + "');</script>").build();
        }
    }
}
