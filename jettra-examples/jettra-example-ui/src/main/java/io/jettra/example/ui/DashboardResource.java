package io.jettra.example.ui;

import io.jettra.ui.component.*;
import io.jettra.ui.template.Template;
import io.jettra.ui.template.Page;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import java.net.URI;

@Path("/dashboard")
public class DashboardResource {

    @Context
    HttpHeaders headers;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response dashboard() {
        // Simple security check (cookie-based)
        if (!headers.getCookies().containsKey("user_session")) {
            return Response.temporaryRedirect(URI.create("/")).build();
        }

        String username = headers.getCookies().get("user_session").getValue();

        Template template = new Template();

        // 1. Navbar
        Navbar navbar = new Navbar("top-nav");
        navbar.setBrandName("Jettra Manager");
        
        Div userInfo = new Div("user-info");
        userInfo.setStyleClass("flex items-center gap-3 text-sm text-slate-400 mr-4");
        userInfo.addComponent(new Label("user-name", username));
        navbar.addRightComponent(userInfo);

        Button logoutBtn = new Button("logout", "Logout");
        logoutBtn.setStyleClass("text-sm text-white bg-slate-700 hover:bg-slate-600 px-4 py-2 rounded-lg");
        logoutBtn.setHxPost("/auth/logout");
        navbar.addRightComponent(logoutBtn);
        
        template.setTop(navbar);

        // 2. Sidebar with Data Explorer
        Sidebar sidebar = new Sidebar("main-sidebar");
        
        sidebar.addItem(new Sidebar.SidebarItem("nav-cluster", "Cluster", "<svg class='w-5 h-5' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10'></path></svg>"));
        sidebar.addItem(new Sidebar.SidebarItem("nav-security", "Security", "<svg class='w-5 h-5' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z'></path></svg>"));
        
        // Data Explorer Tree
        Div explorerTitle = new Div("explorer-title");
        explorerTitle.setStyleClass("flex justify-between items-center px-2 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider");
        explorerTitle.addComponent(new Label("lbl-explorer", "Data Explorer"));
        sidebar.addComponent(explorerTitle);

        Tree dataTree = new Tree("data-explorer");
        Tree.TreeNode db1 = new Tree.TreeNode("SalesDB", "📁");
        db1.addChild(new Tree.TreeNode("Orders", "📄"));
        db1.addChild(new Tree.TreeNode("Customers", "📄"));
        dataTree.addNode(db1);
        
        Tree.TreeNode db2 = new Tree.TreeNode("Inventory", "📁");
        db2.addChild(new Tree.TreeNode("Products", "📄"));
        dataTree.addNode(db2);
        
        sidebar.addComponent(dataTree);
        
        template.setLeft(sidebar);

        // 3. Center Content (Cluster Overview)
        Div content = new Div("center-content");
        content.setStyleClass("p-6");

        Label pageTitle = new Label("page-title", "Cluster Overview");
        pageTitle.setStyleClass("text-2xl font-bold text-gray-900 dark:text-white mb-6");
        content.addComponent(pageTitle);

        Div cardGrid = new Div("node-grid");
        cardGrid.setStyleClass("grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6");
        
        // Example Node Card
        cardGrid.addComponent(createNodeCard("Node 1", "ONLINE", "192.168.1.10:9000", "85%"));
        cardGrid.addComponent(createNodeCard("Node 2", "ONLINE", "192.168.1.11:9000", "42%"));
        cardGrid.addComponent(createNodeCard("Node 3", "OFFLINE", "192.168.1.12:9000", "0%"));
        
        content.addComponent(cardGrid);
        
        template.setCenter(content);

        Page page = new Page();
        page.setTitle("Jettra Dashboard");
        page.setContent(template.render());
        
        return Response.ok(page.render()).build();
    }

    private Card createNodeCard(String id, String status, String address, String cpu) {
        Card card = new Card(id);
        card.setTitle(id);
        
        Badge statusBadge = new Badge(id + "-status", status);
        statusBadge.setColor(status.equals("ONLINE") ? "green" : "red");
        card.addComponent(statusBadge);
        
        Div info = new Div(id + "-info");
        info.setStyleClass("mt-4 space-y-1 text-sm text-gray-600 dark:text-gray-400");
        info.addComponent(new Label(id + "-addr", "Address: " + address));
        info.addComponent(new Label(id + "-cpu", "CPU Usage: " + cpu));
        card.addComponent(info);
        
        return card;
    }
}
