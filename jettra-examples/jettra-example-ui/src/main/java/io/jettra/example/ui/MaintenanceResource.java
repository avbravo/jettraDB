package io.jettra.example.ui;

import io.jettra.ui.component.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import io.jettra.example.ui.client.PlacementDriverClient;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.List;
import java.util.Map;

@Path("/dashboard/maintenance")
public class MaintenanceResource {

    @Inject
    @RestClient
    PlacementDriverClient pdClient;

    @Context
    HttpHeaders headers;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String maintenancePage() {
        Div container = new Div("maintenance-view");
        container.setStyleClass("p-6 space-y-6 animate-in fade-in duration-500");

        Div header = new Div("maint-header");
        header.setStyleClass("mb-8");
        header.addComponent(new Label("maint-title", "<h1 class='text-2xl font-bold text-slate-800 dark:text-white'>Maintenance & Backups</h1>"));
        header.addComponent(new Label("maint-desc", "<p class='text-slate-500 dark:text-slate-400'>Manage database backups, restores, and data portability.</p>"));
        container.addComponent(header);

        // Grid for Backup/Restore
        Div grid = new Div("maint-grid");
        grid.setStyleClass("grid grid-cols-1 md:grid-cols-2 gap-6");

        // Backup Card
        Div backupCard = createCard("Database Backup", "Export your database to JSON or Jettra Native format.");
        
        SelectOne dbSelect = new SelectOne("backup-db");
        dbSelect.setStyleClass("w-full bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800 rounded-lg py-2 px-3 text-sm focus:ring-2 focus:ring-indigo-500 transition-all");
        
        String token = getAuthToken();
        if (token != null) {
            try {
                List<io.jettra.example.ui.model.Database> dbs = pdClient.getDatabases("Bearer " + token);
                for (var db : dbs) dbSelect.addOption(db.getName(), db.getName());
            } catch (Exception e) {}
        }
        
        backupCard.addComponent(new Label("lbl-db", "<label class='block text-xs font-bold text-slate-500 mb-2 uppercase'>Select Database</label>"));
        backupCard.addComponent(dbSelect);

        Button backupBtn = new Button("btn-run-backup", "🚀 Run Backup");
        backupBtn.setStyleClass("mt-6 w-full py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-bold transition-all shadow-lg shadow-indigo-500/20");
        backupBtn.addAttribute("hx-post", "/dashboard/maintenance/backup");
        backupBtn.addAttribute("hx-include", "#backup-db");
        backupBtn.addAttribute("hx-target", "#maint-results");
        backupCard.addComponent(backupBtn);
        
        grid.addComponent(backupCard);

        // Restore Card
        Div restoreCard = createCard("Database Restore", "Restore a database from a previous backup.");
        restoreCard.addComponent(new Label("lbl-res-db", "<label class='block text-xs font-bold text-slate-500 mb-2 uppercase'>Database Target</label>"));
        
        InputText resDb = new InputText("res-db");
        resDb.setPlaceholder("database_name");
        restoreCard.addComponent(resDb);

        restoreCard.addComponent(new Label("lbl-res-id", "<label class='block text-xs font-bold text-slate-500 mt-4 mb-2 uppercase'>Backup ID / Path</label>"));
        InputText resId = new InputText("res-id");
        resId.setPlaceholder("backup_2024.json");
        restoreCard.addComponent(resId);

        Button restoreBtn = new Button("btn-run-restore", "📥 Restore Data");
        restoreBtn.setStyleClass("mt-6 w-full py-3 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-bold transition-all shadow-lg shadow-emerald-500/20");
        restoreBtn.addAttribute("hx-post", "/dashboard/maintenance/restore");
        restoreBtn.addAttribute("hx-include", "#res-db, #res-id");
        restoreBtn.addAttribute("hx-target", "#maint-results");
        restoreCard.addComponent(restoreBtn);

        grid.addComponent(restoreCard);
        container.addComponent(grid);

        // Results Area
        Div results = new Div("maint-results");
        results.setStyleClass("mt-8 p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 min-h-[100px] flex items-center justify-center text-slate-400 text-sm italic font-medium");
        results.addComponent(new Label("empty-res", "Operation results will appear here..."));
        container.addComponent(results);

        return container.render();
    }

    @POST
    @Path("/backup")
    @Produces(MediaType.TEXT_HTML)
    public String runBackup(@FormParam("backup-db") String db) {
        String token = getAuthToken();
        try {
            Response res = pdClient.backup(db, "Bearer " + token);
            return "<div class='p-4 bg-emerald-500/10 border border-emerald-500/20 rounded-lg text-emerald-400 w-full'>" +
                   "<strong>Success!</strong> Backup created for " + db + ". ID: " + res.readEntity(String.class) + "</div>";
        } catch (Exception e) {
            return "<div class='p-4 bg-rose-500/10 border border-rose-500/20 rounded-lg text-rose-400 w-full'>" +
                   "<strong>Error:</strong> " + e.getMessage() + "</div>";
        }
    }

    @POST
    @Path("/restore")
    @Produces(MediaType.TEXT_HTML)
    public String runRestore(@FormParam("res-db") String db, @FormParam("res-id") String id) {
        String token = getAuthToken();
        try {
            pdClient.restore(db, Map.of("backupId", id), "Bearer " + token);
            return "<div class='p-4 bg-emerald-500/10 border border-emerald-500/20 rounded-lg text-emerald-400 w-full'>" +
                   "<strong>Success!</strong> Database " + db + " restored successfully.</div>";
        } catch (Exception e) {
            return "<div class='p-4 bg-rose-500/10 border border-rose-500/20 rounded-lg text-rose-400 w-full'>" +
                   "<strong>Error:</strong> " + e.getMessage() + "</div>";
        }
    }

    private Div createCard(String title, String desc) {
        Div card = new Div("card-" + title.toLowerCase().replace(" ", ""));
        card.setStyleClass("p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md transition-all");
        card.addComponent(new Label("t", "<h3 class='text-lg font-bold text-slate-800 dark:text-white mb-1'>" + title + "</h3>"));
        card.addComponent(new Label("d", "<p class='text-sm text-slate-500 dark:text-slate-400 mb-6'>" + desc + "</p>"));
        return card;
    }

    private String getAuthToken() {
        if (headers.getCookies().containsKey("auth_token")) {
            String token = headers.getCookies().get("auth_token").getValue();
            if (token != null && token.startsWith("\"") && token.endsWith("\"")) {
                token = token.substring(1, token.length() - 1);
            }
            return token;
        }
        return null;
    }
}
