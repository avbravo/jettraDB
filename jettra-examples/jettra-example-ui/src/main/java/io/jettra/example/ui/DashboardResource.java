package io.jettra.example.ui;

import io.jettra.ui.component.*;
import io.jettra.example.ui.component.ThemeToggle;
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

        Div rightSide = new Div("nav-right");
        rightSide.setStyleClass("flex items-center gap-3");

        // Theme Toggle Button
        ThemeToggle themeBtn = new ThemeToggle("theme-toggle");
        rightSide.addComponent(themeBtn);

        Div userInfo = new Div("user-info");
        userInfo.setStyleClass("flex items-center gap-3 text-sm text-slate-400 mr-4 ml-2");
        userInfo.addComponent(new Label("user-name", username));
        rightSide.addComponent(userInfo);

        Button logoutBtn = new Button("logout", "Logout");
        logoutBtn.setStyleClass("text-sm text-white bg-slate-700 hover:bg-slate-600 px-4 py-2 rounded-lg");
        logoutBtn.setHxPost("/auth/logout");
        rightSide.addComponent(logoutBtn);

        navbar.addRightComponent(rightSide);

        template.setTop(navbar);

        // 2. Sidebar with Data Explorer
        // Currently Jettra-UI Navbar targets 'drawer-navigation' for mobile toggle.
        // So we MUST use that ID for the sidebar.
        // 2. Sidebar with Data Explorer
        // Currently Jettra-UI Navbar targets 'drawer-navigation' for mobile toggle.
        // So we MUST use that ID for the sidebar.
        Sidebar sidebar = new Sidebar("drawer-navigation");

        Sidebar.SidebarItem clusterItem = new Sidebar.SidebarItem("nav-cluster", "Cluster",
                "<svg class='w-5 h-5' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10'></path></svg>");
        clusterItem.setHxGet("/dashboard/cluster");
        clusterItem.setHxTarget("#main-content-view");
        sidebar.addItem(clusterItem);

        Sidebar.SidebarItem securityItem = new Sidebar.SidebarItem("nav-security", "Security",
                "<svg class='w-5 h-5' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z'></path></svg>");
        securityItem.setHxGet("/dashboard/security");
        securityItem.setHxTarget("#main-content-view");
        sidebar.addItem(securityItem);

        // Data Explorer Tree
        Div explorerTitle = new Div("explorer-title");
        explorerTitle.setStyleClass(
                "flex justify-between items-center px-2 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider");
        explorerTitle.addComponent(new Label("lbl-explorer", "Data Explorer"));

        // Add DB Button
        Button addDbBtn = new Button("btn-add-db", "+");
        addDbBtn.setStyleClass("text-gray-500 hover:text-white hover:bg-indigo-600 rounded px-1");
        addDbBtn.addAttribute("title", "Add Database");
        // For now, simpler action or modal trigger can be added later as per user
        // request just to have the button
        addDbBtn.addAttribute("onclick", "alert('Add Database feature coming soon')");
        explorerTitle.addComponent(addDbBtn);

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

        // 3. Center Content (Initially Cluster Overview)
        // We wrap it in a Div that matches HTMX target
        Div contentWrapper = new Div("main-content-view");

        // Use HTMX trigger to load it on page load
        contentWrapper.addAttribute("hx-get", "/dashboard/cluster");
        contentWrapper.addAttribute("hx-trigger", "load");

        template.setCenter(contentWrapper);

        // 4. Footer
        template.setFooter(new Footer());

        Page page = new Page();
        page.setTitle("Jettra Dashboard");

        // Add manual script for Theme Toggle
        String themeScript = """
                    // On page load or when changing themes, best to add inline in `head` to avoid FOUC
                    if (localStorage.getItem('color-theme') === 'dark' || (!('color-theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
                        document.documentElement.classList.add('dark');
                    } else {
                        document.documentElement.classList.remove('dark')
                    }

                    function toggleTheme() {
                        var themeToggleDarkIcon = document.getElementById('theme-toggle-dark-icon');
                        var themeToggleLightIcon = document.getElementById('theme-toggle-light-icon');

                        // Change the icons inside the button based on previous settings
                        if (localStorage.getItem('color-theme') === 'dark' || (!('color-theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
                            themeToggleLightIcon.classList.remove('hidden');
                            themeToggleDarkIcon.classList.add('hidden');
                        } else {
                            themeToggleLightIcon.classList.add('hidden');
                            themeToggleDarkIcon.classList.remove('hidden');
                        }

                        // if set via local storage previously
                        if (localStorage.getItem('color-theme')) {
                            if (localStorage.getItem('color-theme') === 'light') {
                                document.documentElement.classList.add('dark');
                                localStorage.setItem('color-theme', 'dark');
                            } else {
                                document.documentElement.classList.remove('dark');
                                localStorage.setItem('color-theme', 'light');
                            }
                        // if NOT set via local storage previously
                        } else {
                            if (document.documentElement.classList.contains('dark')) {
                                document.documentElement.classList.remove('dark');
                                localStorage.setItem('color-theme', 'light');
                            } else {
                                document.documentElement.classList.add('dark');
                                localStorage.setItem('color-theme', 'dark');
                            }
                        }
                    }

                    // Run on load to set correct icon
                    document.addEventListener('DOMContentLoaded', function() {
                        var themeToggleDarkIcon = document.getElementById('theme-toggle-dark-icon');
                        var themeToggleLightIcon = document.getElementById('theme-toggle-light-icon');
                        if (localStorage.getItem('color-theme') === 'dark' || (!('color-theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
                            themeToggleLightIcon.classList.remove('hidden');
                            themeToggleDarkIcon.classList.add('hidden');
                        } else {
                            themeToggleLightIcon.classList.add('hidden');
                            themeToggleDarkIcon.classList.remove('hidden');
                        }
                    });
                """;

        page.addScriptContent(themeScript);
        page.setContent(template.render());

        return Response.ok(page.render()).build();
    }

    // Helper removed as it's now in ClusterResource
}
