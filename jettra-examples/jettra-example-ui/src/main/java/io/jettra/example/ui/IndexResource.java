package io.jettra.example.ui;

import io.jettra.ui.component.Div;
import io.jettra.ui.component.Label;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/dashboard/index")
public class IndexResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getIndexView() {
        Div container = new Div("index-view");
        
        container.addComponent(new Label("title", "<h2 class='text-2xl font-bold text-white mb-4'>Index Management</h2>"));
        
        Div content = new Div("index-content");
        content.setStyleClass("p-8 border border-dashed border-slate-700 rounded-lg text-center bg-slate-900/50");
        content.addComponent(new Label("icon", "<div class='text-4xl mb-4'>📇</div>"));
        content.addComponent(new Label("msg", "<h3 class='text-xl text-slate-300 mb-2'>Coming Soon</h3>"));
        content.addComponent(new Label("sub", "<p class='text-slate-500'>Advanced index management is currently under development. <br>Please use the Jettra Shell or check back later.</p>"));
        
        container.addComponent(content);
        return Response.ok(container.render()).build();
    }
}
