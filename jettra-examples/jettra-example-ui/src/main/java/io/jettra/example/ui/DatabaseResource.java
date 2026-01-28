package io.jettra.example.ui;

import io.jettra.example.ui.client.PlacementDriverClient;
import io.jettra.example.ui.model.Database;
import io.jettra.example.ui.form.DatabaseForm;
import io.jettra.ui.component.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/dashboard/database")
public class DatabaseResource {

    @Inject
    @RestClient
    PlacementDriverClient pdClient;

    @Context
    HttpHeaders headers;

    private String getAuthToken() {
        if (headers.getCookies().containsKey("auth_token")) {
            return headers.getCookies().get("auth_token").getValue();
        }
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
                 Div result = new Div("save-result");
                 result.setStyleClass("max-w-4xl mx-auto space-y-8 animate-in fade-in zoom-in duration-500");

                 // Hero Image View
                 Div hero = new Div("success-hero");
                 hero.setStyleClass("relative h-64 rounded-3xl overflow-hidden shadow-2xl border border-white/10");
                 hero.addAttribute("style", "background-image: url('https://raw.githubusercontent.com/avbravo/jettraDB/main/images/nocturnal_urban_database_success.png'); background-size: cover; background-position: center;");
                 
                 Div overlay = new Div("hero-overlay");
                 overlay.setStyleClass("absolute inset-0 bg-gradient-to-t from-slate-950 via-transparent to-transparent flex items-end p-8");
                 Label heroText = new Label("hero-text", "DATABASE DEPLOYED");
                 heroText.setStyleClass("text-4xl font-black text-white tracking-tighter italic shadow-black drop-shadow-lg");
                 overlay.addComponent(heroText);
                 hero.addComponent(overlay);
                 result.addComponent(hero);

                 Alert success = new Alert("db-success", "Database '" + name + "' initialized successfully in the cluster.");
                 success.setType("success");
                 success.setStyleClass("bg-green-500/10 border border-green-500/20 text-green-400 p-6 rounded-2xl backdrop-blur-md");
                 result.addComponent(success);
                 
                 Button backBtn = new Button("back-to-cluster", "RETURN TO COMMAND CENTER");
                 backBtn.setStyleClass("w-full py-4 bg-indigo-600 hover:bg-indigo-500 text-white font-black rounded-xl transition-all shadow-lg shadow-indigo-500/30 tracking-tighter");
                 backBtn.setHxGet("/dashboard/cluster");
                 backBtn.setHxTarget("#main-content-view");
                 result.addComponent(backBtn);
                 
                 return Response.ok(result.render()).build();
            } else {
                DatabaseForm form = new DatabaseForm("new-db-form");
                form.setError("Error creating database: " + resp.getStatus());
                return Response.ok(form.render()).build();
            }
        } catch (Exception e) {
            DatabaseForm form = new DatabaseForm("new-db-form");
            form.setError("Error: " + e.getMessage());
            return Response.ok(form.render()).build();
        }
    }
}
