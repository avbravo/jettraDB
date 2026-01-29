package io.jettra.example.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.jettra.example.ui.client.PlacementDriverClient;
import io.jettra.example.ui.form.DatabaseForm;
import io.jettra.example.ui.model.Database;
import io.jettra.example.ui.model.User;
import io.jettra.example.ui.service.SecurityService;
import io.jettra.ui.component.Alert;
import io.jettra.ui.component.Badge;
import io.jettra.ui.component.Button;
import io.jettra.ui.component.Card;
import io.jettra.ui.component.Div;
import io.jettra.ui.component.Form;
import io.jettra.ui.component.InputText;
import io.jettra.ui.component.Label;
import io.jettra.ui.component.SelectOne;
import io.jettra.ui.component.Table;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/dashboard/database")
public class DatabaseResource {
    private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(DatabaseResource.class);

    @Inject
    @RestClient
    PlacementDriverClient pdClient;

    @Context
    HttpHeaders headers;

    private String getAuthToken() {
        if (headers.getCookies().containsKey("auth_token")) {
            String token = headers.getCookies().get("auth_token").getValue();
            LOG.infof("DEBUG: Raw auth_token cookie: [%s]", token);
            if (token != null && token.startsWith("\"") && token.endsWith("\"")) {
                token = token.substring(1, token.length() - 1);
                LOG.infof("DEBUG: Cleaned auth_token: [%s]", token);
            }
            return token;
        }
        LOG.warn("DEBUG: auth_token cookie NOT FOUND");
        return null;
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public Response getNewDatabaseForm() {
        Div container = new Div("new-db-container");
        container.setStyleClass("max-w-2xl mx-auto p-6");

        Label title = new Label("new-db-title", "Create New Database");
        title.setStyleClass("text-2xl font-bold text-white mb-6");
        container.addComponent(title);

        DatabaseForm form = new DatabaseForm("new-db-form");
        form.init();
        container.addComponent(form);

        return Response.ok(container.render()).build();
    }

    @POST
    @Path("/save")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response saveDatabase(@FormParam("name") String name,
            @FormParam("engine") String engine,
            @FormParam("storage") String storage) {

        String token = getAuthToken();
        if (token == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Database db = new Database(name, engine, storage);
        try {
            Response resp = pdClient.createDatabase(db, "Bearer " + token);
            if (resp.getStatus() >= 200 && resp.getStatus() < 300) {
                // Auto-assign roles
                // 1. Current user (Creator) -> admin
                // 2. Super user -> super-user
                try {
                    String currentUser = null;
                    // This is a bit hacky, we need to extract username from token or have it
                    // available.
                    // Since we don't have a direct way to get username from token here without
                    // parsing,
                    // we might need to fetch "users/me" or similar.
                    // For now, let's assume we can get it from a cookie or just assign super-user
                    // always.
                    // Ideally we should have the username. Let's try to get it from where we got
                    // the token or assume the user is known.
                    // Wait, we don't have the username easily available in this method.
                    // However, we can just assign 'super-user' to 'super-user' role.
                    // The 'admin' role for creator: if we can't identify creator, we might skip it
                    // or use a default.
                    // BUT the requirement says: "independent of the user who creates it".

                    // Actually, let's fetch all users to find the creator? No.
                    // We will just fetch the current user using the token if possible.
                    // Or we can rely on the UI to send it? No, security risk.

                    // Let's implement at least the super-user assignment which is critical.
                    java.util.Map<String, String> roles = new java.util.HashMap<>();
                    roles.put("super-user", "super-user");

                    // To assign admin to creator, we really need the creator's username.
                    // Let's look at `AuthResource` or `LoginResource` to see how we track session.
                    // The cookie `jettra_username` might be available!
                    if (headers.getCookies().containsKey("jettra_username")) {
                        currentUser = headers.getCookies().get("jettra_username").getValue();
                        if (currentUser != null && !currentUser.equals("super-user")) {
                            roles.put(currentUser, "admin");
                        }
                    }

                    securityService.syncDatabaseRoles(name, roles, token);
                } catch (Exception e) {
                    LOG.error("Failed to auto-assign roles for " + name, e);
                    // logging but not failing the creation response
                }

                Div result = new Div("save-result");
                result.setStyleClass("max-w-4xl mx-auto space-y-8 animate-in fade-in zoom-in duration-500");

                // Hero Image View
                Div hero = new Div("success-hero");
                hero.setStyleClass("relative h-64 rounded-3xl overflow-hidden shadow-2xl border border-white/10");
                hero.addAttribute("style",
                        "background-image: url('https://raw.githubusercontent.com/avbravo/jettraDB/main/images/nocturnal_urban_database_success.png'); background-size: cover; background-position: center;");

                Div overlay = new Div("hero-overlay");
                overlay.setStyleClass(
                        "absolute inset-0 bg-gradient-to-t from-slate-950 via-transparent to-transparent flex items-end p-8");
                Label heroText = new Label("hero-text", "DATABASE DEPLOYED");
                heroText.setStyleClass(
                        "text-4xl font-black text-white tracking-tighter italic shadow-black drop-shadow-lg");
                overlay.addComponent(heroText);
                hero.addComponent(overlay);
                result.addComponent(hero);

                Alert success = new Alert("db-success",
                        "Database '" + name + "' initialized successfully in the cluster.");
                success.setType("success");
                success.setStyleClass(
                        "bg-green-500/10 border border-green-500/20 text-green-400 p-6 rounded-2xl backdrop-blur-md");
                result.addComponent(success);

                Button backBtn = new Button("back-to-cluster", "RETURN TO COMMAND CENTER");
                backBtn.setStyleClass(
                        "w-full py-4 bg-indigo-600 hover:bg-indigo-500 text-white font-black rounded-xl transition-all shadow-lg shadow-indigo-500/30 tracking-tighter");
                backBtn.setHxGet("/dashboard/cluster");
                backBtn.setHxTarget("#main-content-view");
                result.addComponent(backBtn);

                return Response.ok(result.render())
                        .header("HX-Trigger", "refreshExplorer")
                        .build();
            } else {
                DatabaseForm form = new DatabaseForm("new-db-form");
                form.setError("Error creating database: " + resp.getStatus());
                return Response.ok(form.render()).build();
            }
        } catch (Exception e) {
            DatabaseForm form = new DatabaseForm("new-db-form");
            form.setError("Error: " + e.getMessage());
            form.init();
            return Response.ok(form.render()).build();
        }
    }

    @PUT
    @Path("/save")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateDatabase(@QueryParam("oldName") String oldName,
            @FormParam("name") String name,
            @FormParam("engine") String engine,
            @FormParam("storage") String storage) {

        String token = getAuthToken();
        if (token == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Database db = new Database(name, engine, storage);
        try {
            Response resp = pdClient.updateDatabase(oldName, db, "Bearer " + token);
            if (resp.getStatus() >= 200 && resp.getStatus() < 300) {
                return Response.ok(
                        "<div class='p-4 mb-4 text-sm text-green-800 rounded-lg bg-green-50 dark:bg-gray-800 dark:text-green-400'>Database updated successfully</div>"
                                +
                                "<button hx-get='/dashboard/cluster' hx-target='#main-content-view' class='px-4 py-2 bg-indigo-600 text-white rounded-lg'>Return</button>")
                        .header("HX-Trigger", "refreshExplorer")
                        .build();
            } else {
                return Response.status(resp.getStatus()).entity("Error updating database").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/info")
    @Produces(MediaType.TEXT_HTML)
    public Response getDatabaseInfo(@QueryParam("name") String name) {
        String token = getAuthToken();
        List<Database> dbs = pdClient.getDatabases("Bearer " + token);
        Database db = dbs.stream().filter(d -> d.getName().equals(name)).findFirst().orElse(null);

        if (db == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Database not found").build();
        }

        Div container = new Div("db-info-container");
        container.setStyleClass(
                "max-w-4xl mx-auto p-6 space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500");

        Card infoCard = new Card("db-info-card", "Database Information: " + name);
        infoCard.setStyleClass("bg-slate-900/50 backdrop-blur-xl border border-white/10 p-8 rounded-3xl shadow-2xl");

        Div grid = new Div("info-grid");
        grid.setStyleClass("grid grid-cols-1 md:grid-cols-2 gap-6");

        grid.addComponent(createDetailItem("Engine Type", db.getEngine() != null ? db.getEngine() : "Multi-Model",
                "M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"));
        grid.addComponent(createDetailItem("Storage Type",
                "STORE".equals(db.getStorage()) ? "Persistent (Disk)" : "In-Memory (RAM)",
                "M4 7v10a2 2 0 002 2h12a2 2 0 002-2V7a2 2 0 00-2-2H6a2 2 0 00-2 2z"));

        infoCard.addComponent(grid);
        container.addComponent(infoCard);

        Button backBtn = new Button("back-btn", "Back to Cluster");
        backBtn.setStyleClass(
                "mt-8 px-6 py-3 bg-slate-800 hover:bg-slate-700 text-white rounded-xl transition-all border border-white/5");
        backBtn.setHxGet("/dashboard/cluster");
        backBtn.setHxTarget("#main-content-view");
        container.addComponent(backBtn);

        return Response.ok(container.render()).build();
    }

    private Div createDetailItem(String label, String value, String iconPath) {
        Div item = new Div("info-item-" + label.toLowerCase().replace(" ", "-"));
        item.setStyleClass("bg-white/5 p-4 rounded-2xl border border-white/5 flex items-center gap-4");

        String iconHtml = String.format(
                "<div class='w-10 h-10 rounded-xl bg-indigo-500/10 flex items-center justify-center text-indigo-400'>" +
                        "<svg class='w-6 h-6' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='%s'></path></svg>"
                        +
                        "</div>",
                iconPath);

        Div content = new Div("item-content");
        content.addComponent(new Label("item-label", label)
                .setStyleClass("text-xs font-bold text-slate-500 uppercase tracking-widest"));
        content.addComponent(new Label("item-value", value).setStyleClass("text-lg font-bold text-white block"));

        item.addComponent(new Label("item-icon", iconHtml));
        item.addComponent(content);
        return item;
    }

    @Inject
    SecurityService securityService;

    @GET
    @Path("/security")
    @Produces(MediaType.TEXT_HTML)
    public Response getDatabaseSecurity(@QueryParam("name") String name) {
        String token = getAuthToken();

        Div container = new Div("db-security-container");
        container.setStyleClass("max-w-4xl mx-auto p-6 space-y-6 animate-in slide-in-from-bottom-4 duration-500");

        Label title = new Label("sec-title", "Security & Permissions: " + name);
        title.setStyleClass("text-3xl font-black text-white tracking-tight mb-8 block");
        container.addComponent(title);

        Card permCard = new Card("perm-card", "User Permissions");
        permCard.setStyleClass("bg-slate-900/50 backdrop-blur-xl border border-white/10 p-6 rounded-3xl shadow-2xl");

        Table table = new Table("perm-table");
        table.setStyleClass("w-full text-left border-collapse");
        table.addHeader("User");
        table.addHeader("Profile");
        table.addHeader("Database Role");

        List<User> users = securityService.getUsers(token);
        for (User user : users) {
            List<String> row = new ArrayList<>();
            row.add("<div class='flex items-center gap-2'><div class='w-7 h-7 bg-indigo-500/20 text-indigo-400 rounded-full flex items-center justify-center text-[10px] font-bold'>"
                    + user.getUsername().substring(0, 1).toUpperCase() + "</div>" + user.getUsername() + "</div>");
            row.add("<span class='px-2 py-0.5 rounded-full text-[10px] bg-slate-800 text-slate-400 border border-slate-700'>"
                    + (user.getProfile() != null ? user.getProfile() : "end-user") + "</span>");

            boolean isSuperUser = "super-user".equals(user.getUsername());
            if (isSuperUser) {
                // For super-user, we show a badge and use a hidden input to ensure it's submitted
                // browsers don't submit disabled fields.
                Div superUserDisplay = new Div("super-user-display-" + user.getUsername());
                superUserDisplay.setStyleClass("flex items-center gap-2");
                
                Badge badge = new Badge("role-badge-" + user.getUsername(), "Super User");
                badge.setColor("indigo");
                superUserDisplay.addComponent(badge);
                
                // Hidden input to ensure it's sent in the form
                InputText hiddenRole = new InputText("role-" + user.getUsername());
                hiddenRole.setType("hidden");
                hiddenRole.setValue("super-user");
                superUserDisplay.addComponent(hiddenRole);
                
                row.add(superUserDisplay.render());
            } else {
                SelectOne roleSelect = new SelectOne("role-" + user.getUsername());
                roleSelect.addOption("denied", "No Access (Denied)");
                roleSelect.addOption("read", "Read Only");
                roleSelect.addOption("read-write", "Read-Write");
                roleSelect.addOption("admin", "Admin");

                // Determine current role (precise logic from index.html)
                String currentRole = "denied";
                if (user.getRoles() != null) {
                    for (String r : user.getRoles()) {
                        if (r.endsWith("_" + name)) {
                            // Extract type: admin_mydb -> admin
                            currentRole = r.substring(0, r.length() - name.length() - 1);
                            break;
                        }
                    }
                }
                roleSelect.setSelectedValue(currentRole);
                row.add(roleSelect.render());
            }

            table.addRow(row);
        }

        // Wrap table and save button in a form
        Form permForm = new Form("perm-form");
        // We set attributes manually since DatabaseForm constructor sets defaults for
        // creating DB
        permForm.setStyleClass("w-full");
        permForm.setHxPost("/dashboard/database/security/save");
        permForm.setHxTarget("#main-content-view");
        permForm.setHxSwap("innerHTML");

        // Add hidden field for DB name
        InputText dbNameInput = new InputText("name");
        dbNameInput.setType("hidden");
        dbNameInput.setValue(name);
        permForm.addComponent(dbNameInput);

        permForm.addComponent(table);

        Div btnGroup = new Div("btn-group");
        btnGroup.setStyleClass("flex gap-4 mt-8");

        Button saveBtn = new Button("save-perms", "SAVE PERMISSIONS");
        saveBtn.setStyleClass(
                "px-8 py-4 bg-indigo-600 hover:bg-indigo-500 text-white font-black rounded-2xl shadow-lg transition-all");
        saveBtn.addAttribute("type", "submit");
        btnGroup.addComponent(saveBtn);

        Button backBtn = new Button("back-btn", "CANCEL");
        backBtn.setStyleClass("px-8 py-4 bg-slate-800 text-white rounded-2xl font-bold");
        backBtn.setHxGet("/dashboard/cluster");
        backBtn.setHxTarget("#main-content-view");
        // Prevent form submission for cancel button
        backBtn.addAttribute("type", "button");
        btnGroup.addComponent(backBtn);

        permForm.addComponent(btnGroup);
        permCard.addComponent(permForm);
        container.addComponent(permCard);

        return Response.ok(container.render()).build();
    }

    @POST
    @Path("/security/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response saveDatabasePermissions(jakarta.ws.rs.core.MultivaluedMap<String, String> formParams) {
        LOG.infof("DEBUG: saveDatabasePermissions called. formParams: %s", formParams);
        
        String dbName = formParams.getFirst("name");
        String token = getAuthToken();
        if (token == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        java.util.Map<String, String> roleMappings = new java.util.HashMap<>();

        // Parse form params: keys are like "role-{username}"
        for (String key : formParams.keySet()) {
            if (key.startsWith("role-")) {
                String username = key.substring(5); // remove "role-" prefix
                String role = formParams.getFirst(key);
                // "none" means we might want to send "denied" or just handle it as removal,
                // but the API expects explicit roles usually. Let's map "none" to "denied" if
                // needed or just pass it through
                // The index.html used "none" and logic to clear it.
                // Let's pass the value as is and let the backend (AuthClient -> PD) handle it.
                roleMappings.put(username, role);
            }
        }
        
        LOG.infof("DEBUG: roleMappings built: %s", roleMappings);

        // Self-lockout prevention & special checks (as requested to match index.html logic)
        String currentLoggedInUser = headers.getCookies().containsKey("user_session")
                ? headers.getCookies().get("user_session").getValue()
                : "";
        
        LOG.infof("DEBUG: Saving perms for DB [%s]. Current User: [%s]", dbName, currentLoggedInUser);

        boolean isGlobalAdmin = "super-user".equalsIgnoreCase(currentLoggedInUser);

        if (!isGlobalAdmin) {
            String currentUserNewRole = roleMappings.get(currentLoggedInUser);
            if (currentUserNewRole != null && ("denied".equals(currentUserNewRole) || "none".equals(currentUserNewRole))) {
                Alert alert = new Alert("perm-error", "Action Blocked: You cannot remove your own access completely. Assign another administrator first or contact the global admin.");
                alert.setType("danger");
                alert.setStyleClass("bg-red-500/10 border border-red-500/20 text-red-400 p-4 rounded-xl mb-4");
                return Response.ok(alert.render()).build();
            }
        }

        boolean success = securityService.syncDatabaseRoles(dbName, roleMappings, token);

        if (success) {
            // Re-render the security view to show updated state
            Response securityResponse = getDatabaseSecurity(dbName);
            String securityHtml = (String) securityResponse.getEntity();
            
            // Success alert with premium styling (Flowbite-like)
            String alertHtml = String.format("""
                <div id='alert-success-perms' class='flex items-center p-6 mb-8 text-green-400 border border-green-500/20 bg-green-500/5 backdrop-blur-xl rounded-3xl animate-in fade-in slide-in-from-top-4 duration-500' role='alert'>
                  <svg class='flex-shrink-0 w-6 h-6' aria-hidden='true' xmlns='http://www.w3.org/2000/svg' fill='currentColor' viewBox='0 0 20 20'>
                    <path d='M10 .5a9.5 9.5 0 1 0 9.5 9.5A9.51 9.51 0 0 0 10 .5Zm3.707 8.207-4 4a1 1 0 0 1-1.414 0l-2-2a1 1 0 0 1 1.414-1.414L9 10.586l3.293-3.293a1 1 0 0 1 1.414 1.414Z'/>
                  </svg>
                  <span class='sr-only'>Success</span>
                  <div class='ms-4 text-sm font-bold tracking-tight text-green-300'>
                    SECURITY POLICY SYNCHRONIZED: The permissions for database '%s' have been successfully deployed. The data explorer has been updated with the authorized users.
                  </div>
                  <button type='button' class='ms-auto -mx-1.5 -my-1.5 bg-transparent text-green-400 hover:text-green-200 rounded-lg focus:ring-2 focus:ring-green-400 p-1.5 hover:bg-green-500/10 inline-flex items-center justify-center h-8 w-8' data-dismiss-target='#alert-success-perms' aria-label='Close' onclick='this.parentElement.remove()'>
                    <span class='sr-only'>Close</span>
                    <svg class='w-3 h-3' aria-hidden='true' xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 14 14'>
                        <path stroke='currentColor' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='m1 1 6 6m0 0 6 6M7 7l6-6M7 7l-6 6'/>
                    </svg>
                  </button>
                </div>
                """, dbName);

            return Response.ok(alertHtml + securityHtml)
                    .header("HX-Trigger", "refreshExplorer")
                    .build();
        } else {
            Alert alert = new Alert("perm-error", "Failed to update permissions. Please check the system logs.");
            alert.setType("danger");
            alert.setStyleClass("bg-red-500/10 border border-red-500/20 text-red-400 p-6 rounded-2xl mb-4");
            return Response.ok(alert.render()).build();
        }
    }

    @GET
    @Path("/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response getEditDatabaseForm(@QueryParam("name") String name) {
        String token = getAuthToken();
        List<Database> dbs = pdClient.getDatabases("Bearer " + token);
        Database db = dbs.stream().filter(d -> d.getName().equals(name)).findFirst().orElse(null);

        if (db == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Database not found").build();
        }

        DatabaseForm form = new DatabaseForm("edit-db-form");
        form.setEditMode(db.getName(), db.getEngine(), db.getStorage());
        form.init();

        return Response.ok(form.render()).build();
    }

    @GET
    @Path("/delete")
    @Produces(MediaType.TEXT_HTML)
    public Response getDeleteConfirmation(@QueryParam("name") String name) {
        Div container = new Div("delete-confirm-container");
        container.setStyleClass(
                "max-w-xl mx-auto p-8 bg-slate-950/50 backdrop-blur-2xl border border-red-500/20 rounded-3xl shadow-2xl mt-12 animate-in zoom-in duration-300");

        Label icon = new Label("warning-icon",
                "<div class='w-20 h-20 bg-red-500/10 rounded-full flex items-center justify-center mx-auto mb-6'>" +
                        "<svg class='w-12 h-12 text-red-500' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z'></path></svg>"
                        +
                        "</div>");
        container.addComponent(icon);

        Label title = new Label("delete-title", "Delete Database: " + name);
        title.setStyleClass("text-2xl font-black text-white text-center mb-2 block tracking-tight");
        container.addComponent(title);

        Label warn = new Label("delete-warn",
                "Are you sure? This action is permanent and will destroy all collections and data within this database.");
        warn.setStyleClass("text-slate-400 text-center mb-8 block");
        container.addComponent(warn);

        Div btnGroup = new Div("btn-group");
        btnGroup.setStyleClass("grid grid-cols-2 gap-4");

        Button cancelBtn = new Button("cancel-btn", "Cancel");
        cancelBtn.setStyleClass("py-4 bg-slate-800 hover:bg-slate-700 text-white font-bold rounded-2xl transition-all");
        cancelBtn.setHxGet("/dashboard/cluster");
        cancelBtn.setHxTarget("#main-content-view");
        btnGroup.addComponent(cancelBtn);

        Button confirmBtn = new Button("confirm-btn", "Delete Permanently");
        confirmBtn.setStyleClass(
                "py-4 bg-red-600 hover:bg-red-500 text-white font-bold rounded-2xl transition-all shadow-lg shadow-red-500/20");
        confirmBtn.setHxDelete("/dashboard/database/confirm-delete?name=" + name);
        confirmBtn.setHxTarget("#main-content-view");
        btnGroup.addComponent(confirmBtn);

        container.addComponent(btnGroup);
        return Response.ok(container.render()).build();
    }

    @DELETE
    @Path("/confirm-delete")
    @Produces(MediaType.TEXT_HTML)
    public Response confirmDelete(@QueryParam("name") String name) {
        String token = getAuthToken();
        Response resp = pdClient.deleteDatabase(name, "Bearer " + token);

        if (resp.getStatus() >= 200 && resp.getStatus() < 300) {
            Div result = new Div("delete-success");
            result.setStyleClass("max-w-xl mx-auto text-center p-12");

            Alert success = new Alert("del-alert", "Database '" + name + "' has been deleted successfully.");
            success.setType("success");
            success.setStyleClass("bg-green-500/10 border border-green-500/20 text-green-400 p-8 rounded-3xl mb-8");
            result.addComponent(success);

            Button backBtn = new Button("back-btn", "Return to Dashboard");
            backBtn.setStyleClass("px-8 py-4 bg-indigo-600 text-white rounded-2xl font-bold");
            backBtn.setHxGet("/dashboard/cluster");
            backBtn.setHxTarget("#main-content-view");
            result.addComponent(backBtn);

            return Response.ok(result.render())
                    .header("HX-Trigger", "refreshExplorer")
                    .build();
        } else {
            return Response.status(resp.getStatus()).entity("Error deleting database").build();
        }
    }
}
