package io.jettra.example.ui;

import io.jettra.ui.component.*;
import io.jettra.example.ui.component.ThemeToggle;
import io.jettra.ui.template.Template;
import io.jettra.ui.template.Page;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import io.jettra.example.ui.client.PlacementDriverClient;
import io.jettra.example.ui.model.Database;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import io.jettra.example.ui.service.SecurityService;
import io.jettra.example.ui.model.User;

@Path("/dashboard")
public class DashboardResource {
    private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(DashboardResource.class);

    @Context
    HttpHeaders headers;

    @Inject
    @RestClient
    PlacementDriverClient pdClient;

    @Inject
    SecurityService securityService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response dashboard() {
        if (!headers.getCookies().containsKey("user_session")) {
            LOG.warn("DEBUG (Dashboard): user_session cookie NOT FOUND");
            return Response.temporaryRedirect(URI.create("/")).build();
        }

        String username = headers.getCookies().get("user_session").getValue();
        LOG.infof("DEBUG (Dashboard): Request by user: %s", username);
        LOG.infof("DEBUG (Dashboard): Cookies present: %s", headers.getCookies().keySet());

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

        Sidebar.SidebarItem queryItem = new Sidebar.SidebarItem("nav-query", "Query",
                "<svg class='w-5 h-5' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z'></path></svg>");
        queryItem.setHxGet("/dashboard/query");
        queryItem.setHxTarget("#main-content-view");
        sidebar.addItem(queryItem);

        Sidebar.SidebarItem passwordItem = new Sidebar.SidebarItem("nav-password", "Password",
                "<svg class='w-5 h-5' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z'></path></svg>");
        passwordItem.setHxGet("/dashboard/security/password");
        passwordItem.setHxTarget("#main-content-view");
        sidebar.addItem(passwordItem);

        // Data Explorer Tree
        Div explorerTitle = new Div("explorer-title");
        explorerTitle.setStyleClass(
                "flex justify-between items-center px-2 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider");
        explorerTitle.addComponent(new Label("lbl-explorer", "Data Explorer"));

        // Add DB Button
        Button addDbBtn = new Button("btn-add-db", "+");
        addDbBtn.setStyleClass("text-gray-500 hover:text-white hover:bg-indigo-600 rounded px-1");
        addDbBtn.addAttribute("title", "Add Database");
        addDbBtn.setHxGet("/dashboard/database/new");
        addDbBtn.setHxTarget("#main-content-view");
        explorerTitle.addComponent(addDbBtn);

        sidebar.addComponent(explorerTitle);

        Div explorerContainer = new Div("sidebar-explorer-container");
        explorerContainer.addAttribute("hx-get", "/dashboard/explorer");
        explorerContainer.addAttribute("hx-trigger", "load, refreshExplorer from:body");
        sidebar.addComponent(explorerContainer);

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

        // 5. Global Overlays
        template.addOverlay(createStopModal());
        template.addOverlay(createDetailsModal());
        template.addOverlay(createUserModal());
        template.addOverlay(createDeleteUserModal());
        template.addOverlay(createCollectionModal());
        template.addOverlay(createDocumentModal());
        template.addOverlay(createDocumentDeleteModal());

        Page page = new Page();
        page.setTitle("Jettra Dashboard");

        // Global Style for centering and premium look (using script injection since
        // Page lacks addStyleContent)
        page.addScriptContent(
                """
                            const extraStyle = document.createElement('style');
                            extraStyle.textContent = `
                                .modal-overlay-centered {
                                    display: flex;
                                    align-items: center;
                                    justify-content: center;
                                    position: fixed;
                                    inset: 0;
                                    z-index: 100;
                                }
                                .modal-overlay-centered.hidden {
                                    display: none !important;
                                }
                                /* Custom Scrollbar for dark theme */
                                ::-webkit-scrollbar { width: 8px; }
                                ::-webkit-scrollbar-track { background: transparent; }
                                ::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 4px; }
                                ::-webkit-scrollbar-thumb:hover { background: rgba(255,255,255,0.2); }

                                /* Explicit background for themes */
                                body {
                                    background-color: #ffffff; /* White by default */
                                    color: #1a202c;
                                    transition: background-color 0.3s, color 0.3s;
                                }
                                .dark body {
                                    background-color: #020617; /* Dark (slate-950) */
                                    color: #f8fafc;
                                }
                            `;
                            document.head.appendChild(extraStyle);
                        """);

        // Add manual script
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

                    // Re-init Flowbite after HTMX content is loaded
                    document.body.addEventListener('htmx:afterOnLoad', function(evt) {
                        if (typeof initFlowbite === 'function') {
                            initFlowbite();
                        }
                    });

                    // Explicit modal closure via HX-Trigger
                    document.body.addEventListener('closeModal', function(evt) {
                        const modalId = evt.detail.value || evt.detail || 'document-modal';
                        const modalElement = document.getElementById(modalId);
                        if (modalElement) {
                            // Manual hide to ensure it works even if Flowbite JS is still loading/busy
                            modalElement.classList.add('hidden');
                            modalElement.classList.remove('flex');
                            // Cleanup backdrop if present
                            const backdrop = document.querySelector('[modal-backdrop]');
                            if (backdrop) backdrop.remove();
                        }
                    });

                    function openDocumentModal() {
                        const modal = document.getElementById('document-modal');
                        if(modal) {
                            modal.classList.remove('hidden');
                            modal.classList.add('flex');
                        }
                    }
                    function openDocumentDeleteModal() {
                        const modal = document.getElementById('doc-delete-modal');
                        if(modal) {
                            modal.classList.remove('hidden');
                            modal.classList.add('flex');
                        }
                    }
                    function closeDocumentModal() {
                        const modal = document.getElementById('document-modal');
                        if(modal) {
                            modal.classList.add('hidden');
                            modal.classList.remove('flex');
                        }
                    }
                    function closeDocumentDeleteModal() {
                        const modal = document.getElementById('doc-delete-modal');
                        if(modal) {
                            modal.classList.add('hidden');
                            modal.classList.remove('flex');
                        }
                    }

                    // Global listener for document actions
                    document.body.addEventListener('refreshDocuments', function(evt) {
                        ['document-modal', 'doc-delete-modal'].forEach(id => {
                            const m = document.getElementById(id);
                            if(m) {
                                m.classList.add('hidden');
                                m.classList.remove('flex');
                            }
                        });
                        document.querySelectorAll('[modal-backdrop]').forEach(b => b.remove());

                        // Show success message if provided in the event detail
                        const message = (evt.detail && evt.detail.value) ? evt.detail.value : "Action completed successfully";
                        showNotification(message, 'success');
                    });

                    function showNotification(message, type) {
                        const container = document.getElementById('main-content-view');
                        if(!container) return;

                        const alertDiv = document.createElement('div');
                        const colorClass = type === 'success' ? 'text-emerald-400 bg-emerald-400/10 border-emerald-400/20' : 'text-rose-400 bg-rose-400/10 border-rose-400/20';

                        alertDiv.className = `fixed bottom-4 right-4 z-[200] p-4 rounded-xl border animate-in slide-in-from-right-full duration-300 ${colorClass}`;
                        alertDiv.innerHTML = `
                            <div class="flex items-center gap-3">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                                <span class="text-sm font-bold tracking-tight">${message}</span>
                            </div>
                        `;
                        document.body.appendChild(alertDiv);
                        setTimeout(() => {
                            alertDiv.classList.add('animate-out', 'fade-out', 'slide-out-to-right-full');
                            setTimeout(() => alertDiv.remove(), 300);
                        }, 3000);
                    }
                """;

        page.addScriptContent(themeScript);
        page.setContent(template.render());

        return Response.ok(page.render()).build();
    }

    @GET
    @Path("/explorer")
    @Produces(MediaType.TEXT_HTML)
    public String getExplorerView() {
        DataExplorer dataExplorer = new DataExplorer("data-explorer");

        List<Database> dbs = new ArrayList<>();
        List<User> allUsers = new ArrayList<>();
        String token = null;
        try {
            if (headers.getCookies().containsKey("auth_token")) {
                token = headers.getCookies().get("auth_token").getValue();
                if (token != null && token.startsWith("\"") && token.endsWith("\"")) {
                    token = token.substring(1, token.length() - 1);
                }
                dbs = pdClient.getDatabases("Bearer " + token);
                allUsers = securityService.getUsers(token);
            }
        } catch (Exception e) {
            LOG.error("Error fetching data for explorer", e);
        }

        if (dbs != null && !dbs.isEmpty()) {
            for (Database db : dbs) {
                DataExplorer.DatabaseNode dbNode = new DataExplorer.DatabaseNode(db.getName());

                // Add authorized users to the node
                for (User user : allUsers) {
                    String userRole = null;
                    if (user.getRoles() != null) {
                        for (String role : user.getRoles()) {
                            if (role.endsWith("_" + db.getName())) {
                                // Extract role name: admin_mydb -> admin
                                userRole = role.substring(0, role.length() - db.getName().length() - 1);
                                break;
                            }
                        }
                    }
                    if (userRole != null) {
                        dbNode.addUser(user.getUsername(), userRole);
                    }
                }

                // Add default engines
                DataExplorer.EngineNode docEng = new DataExplorer.EngineNode("Document (Collection)",
                        "M4 7v10a2 2 0 002 2h12a2 2 0 002-2V7a2 2 0 00-2-2H6a2 2 0 00-2 2z");

                // Fetch collections for this DB
                try {
                    List<io.jettra.example.ui.model.Collection> cols = pdClient.getCollections(db.getName(),
                            "Bearer " + token);
                    if (cols != null) {
                        for (io.jettra.example.ui.model.Collection col : cols) {
                            if ("Document".equals(col.getEngine())) {
                                docEng.addCollection(new DataExplorer.CollectionNode(col.getName()));
                            }
                        }
                    }
                } catch (Exception e) {
                    // Fallback or log if fetch fails (e.g. legacy DBs)
                    // LOG.warn("Could not fetch collections for " + db.getName());
                }

                dbNode.addEngine(docEng);
                dbNode.addEngine(new DataExplorer.EngineNode("Column",
                        "M4 7v10a2 2 0 002 2h12a2 2 0 002-2V7a2 2 0 00-2-2H6a2 2 0 00-2 2z"));
                dbNode.addEngine(new DataExplorer.EngineNode("Graph",
                        "M4 7v10a2 2 0 002 2h12a2 2 0 002-2V7a2 2 0 00-2-2H6a2 2 0 00-2 2z"));
                dbNode.addEngine(new DataExplorer.EngineNode("Vector",
                        "M4 7v10a2 2 0 002 2h12a2 2 0 002-2V7a2 2 0 00-2-2H6a2 2 0 00-2 2z"));
                dbNode.addEngine(new DataExplorer.EngineNode("Object",
                        "M4 7v10a2 2 0 002 2h12a2 2 0 002-2V7a2 2 0 00-2-2H6a2 2 0 00-2 2z"));
                dbNode.addEngine(new DataExplorer.EngineNode("Key-Value",
                        "M4 7v10a2 2 0 002 2h12a2 2 0 002-2V7a2 2 0 00-2-2H6a2 2 0 00-2-2H6a2 2 0 00-2 2z"));
                dbNode.addEngine(new DataExplorer.EngineNode("Geospatial",
                        "M4 7v10a2 2 0 002 2h12a2 2 0 002-2V7a2 2 0 00-2-2H6a2 2 0 00-2 2z"));
                dbNode.addEngine(new DataExplorer.EngineNode("Time-Series",
                        "M4 7v10a2 2 0 002 2h12a2 2 0 002-2V7a2 2 0 00-2-2H6a2 2 0 00-2 2z"));
                dbNode.addEngine(new DataExplorer.EngineNode("Files",
                        "M4 7v10a2 2 0 002 2h12a2 2 0 002-2V7a2 2 0 00-2-2H6a2 2 0 00-2 2z"));

                dataExplorer.addDatabase(dbNode);
            }
        } else {
            DataExplorer.DatabaseNode emptyDb = new DataExplorer.DatabaseNode("No Databases");
            dataExplorer.addDatabase(emptyDb);
        }

        return dataExplorer.render();
    }

    // Helper for Modals
    private Modal createStopModal() {
        Modal modal = new Modal("stop-modal", "Stop Node");
        // Ensure perfect centering and premium look
        modal.setStyleClass(
                "modal-overlay-centered fixed inset-0 z-[100] hidden items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm transition-all duration-300");

        Div content = new Div("stop-content");
        content.setStyleClass("text-center space-y-4");

        Div iconBox = new Div("stop-icon-box");
        iconBox.setStyleClass(
                "w-16 h-16 bg-rose-500/10 rounded-full flex items-center justify-center mx-auto mb-4 border border-rose-500/20");
        iconBox.addComponent(new Label("stop-icon",
                "<svg class='w-8 h-8 text-rose-500' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M6 18L18 6M6 6l12 12'></path></svg>"));
        content.addComponent(iconBox);

        Label msg = new Label("stop-msg",
                "Are you sure you want to stop node <span id='stop-node-id' class='text-rose-400 font-mono font-bold'></span>?");
        msg.setStyleClass("text-slate-300 block mb-1");
        content.addComponent(msg);

        Label warning = new Label("stop-warning",
                "This action will temporarily remove this node from the cluster consensus.");
        warning.setStyleClass("text-xs text-slate-500 block");
        content.addComponent(warning);

        modal.addComponent(content);

        Button confirmBtn = new Button("btn-stop-confirm", "🛑 Yes, Stop Node");
        confirmBtn.setStyleClass(
                "flex-1 px-6 py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-lg font-bold transition-all shadow-lg shadow-rose-900/20");
        confirmBtn.addAttribute("onclick", "confirmStopNode()");
        modal.addFooterComponent(confirmBtn);

        Button cancelBtn = new Button("btn-stop-cancel", "Cancel");
        cancelBtn.setStyleClass(
                "flex-1 px-6 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg font-medium transition-all");
        cancelBtn.addAttribute("onclick", "closeStopModal()");
        modal.addFooterComponent(cancelBtn);

        content.addComponent(new Label("script-stop",
                "<script> var targetNodeId = ''; " +
                        "function prepareStop(id) { targetNodeId = id; document.getElementById('stop-node-id').innerText = id; document.getElementById('stop-modal').classList.remove('hidden'); document.getElementById('stop-modal').classList.add('flex'); } "
                        +
                        "function closeStopModal() { document.getElementById('stop-modal').classList.add('hidden'); document.getElementById('stop-modal').classList.remove('flex'); } "
                        +
                        "function confirmStopNode() { if(targetNodeId) { htmx.ajax('POST', '/dashboard/cluster/stop/' + targetNodeId, { swap:'none' }); closeStopModal(); } }</script>"));
        return modal;
    }

    private Modal createDetailsModal() {
        Modal modal = new Modal("details-modal", "Node Details");
        Div content = new Div("details-content");
        content.addComponent(
                new Label("details-msg",
                        "Details for <span id='detail-node-id' class='font-bold'></span>..."));
        modal.addComponent(content);

        Button closeBtn = new Button("btn-details-close", "Close");
        closeBtn.setStyleClass(
                "text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800");
        closeBtn.addAttribute("data-modal-hide", "details-modal");
        modal.addFooterComponent(closeBtn);
        content.addComponent(new Label("script-detail",
                "<script> function openDetails(id) { document.getElementById('detail-node-id').innerText = id; } </script>"));
        return modal;
    }

    private Modal createDeleteUserModal() {
        Modal modal = new Modal("delete-user-modal", "Delete User");
        modal.setStyleClass(
                "modal-overlay-centered fixed inset-0 z-[100] hidden items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm transition-all duration-300");

        Div content = new Div("delete-user-content");
        content.setStyleClass("text-center space-y-4");

        Div iconBox = new Div("delete-user-icon-box");
        iconBox.setStyleClass(
                "w-16 h-16 bg-rose-500/10 rounded-full flex items-center justify-center mx-auto mb-4 border border-rose-500/20");
        iconBox.addComponent(new Label("delete-user-icon",
                "<svg class='w-8 h-8 text-rose-500' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16'></path></svg>"));
        content.addComponent(iconBox);

        Label msg = new Label("delete-user-msg",
                "Are you sure you want to delete user <span id='delete-username-display' class='text-rose-400 font-mono font-bold'></span>?");
        msg.setStyleClass("text-slate-300 block mb-1");
        content.addComponent(msg);

        Label warning = new Label("delete-user-warning",
                "This action is permanent and cannot be undone.");
        warning.setStyleClass("text-xs text-slate-500 block");
        content.addComponent(warning);

        modal.addComponent(content);

        Button confirmBtn = new Button("btn-delete-user-confirm", "🗑️ Yes, Delete User");
        confirmBtn.setStyleClass(
                "flex-1 px-6 py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-lg font-bold transition-all shadow-lg shadow-rose-900/20");
        confirmBtn.addAttribute("onclick", "confirmDeleteUser()");
        modal.addFooterComponent(confirmBtn);

        Button cancelBtn = new Button("btn-delete-user-cancel", "Cancel");
        cancelBtn.setStyleClass(
                "flex-1 px-6 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg font-medium transition-all");
        cancelBtn.addAttribute("onclick", "closeDeleteUserModal()");
        modal.addFooterComponent(cancelBtn);

        content.addComponent(new Label("script-delete-user",
                "<script> var userToDelete = ''; " +
                        "function prepareDeleteUser(username) { userToDelete = username; document.getElementById('delete-username-display').innerText = username; document.getElementById('delete-user-modal').classList.remove('hidden'); document.getElementById('delete-user-modal').classList.add('flex'); } "
                        +
                        "function closeDeleteUserModal() { document.getElementById('delete-user-modal').classList.add('hidden'); document.getElementById('delete-user-modal').classList.remove('flex'); } "
                        +
                        "function confirmDeleteUser() { if(userToDelete) { htmx.ajax('POST', '/dashboard/security/delete/' + userToDelete, { target: '#security-view', swap: 'outerHTML' }); closeDeleteUserModal(); } }</script>"));
        return modal;
    }

    private Modal createUserModal() {
        Modal modal = new Modal("user-modal", "Manage User");
        // Add a hidden input to track if it's an edit
        modal.addComponent(
                new Label("user-is-edit-container",
                        "<input type='hidden' id='user-is-edit' value='false'>"));

        modal.setStyleClass(
                "modal-overlay-centered fixed inset-0 z-[100] hidden items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm transition-all duration-300");

        Div content = new Div("user-modal-body");
        content.setStyleClass("space-y-4 min-w-[320px]");

        // Form Fields
        Div fields = new Div("user-fields");
        fields.setStyleClass("space-y-4");

        fields.addComponent(createFormField("Username", new InputText("user-username")));
        fields.addComponent(createFormField("Email", new InputText("user-email")));
        fields.addComponent(createFormField("Password", new Password("user-password")));

        SelectOne profileSelect = new SelectOne("user-profile");
        profileSelect.addOption("super-user", "Super User (Full Access + Cluster)");
        profileSelect.addOption("management", "Management (Full Access except Stopping Nodes)");
        profileSelect.addOption("end-user", "End User (Database Level Admin Only)");
        fields.addComponent(createFormField("User Profile", profileSelect));

        content.addComponent(fields);
        modal.addComponent(content);

        Button saveBtn = new Button("btn-user-save", "Save User");
        saveBtn.setStyleClass(
                "flex-1 px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-bold transition-all");
        saveBtn.addAttribute("onclick", "saveUser()");
        modal.addFooterComponent(saveBtn);

        Button cancelBtn = new Button("btn-user-cancel", "Cancel");
        cancelBtn.setStyleClass(
                "flex-1 px-6 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg font-medium transition-all");
        cancelBtn.addAttribute("onclick", "closeUserModal()");
        modal.addFooterComponent(cancelBtn);

        content.addComponent(new Label("script-user",
                "<script> " +
                        "function openUserModal() { document.getElementById('user-modal').classList.remove('hidden'); document.getElementById('user-modal').classList.add('flex'); } "
                        +
                        "function closeUserModal() { document.getElementById('user-modal').classList.add('hidden'); document.getElementById('user-modal').classList.remove('flex'); } "
                        +
                        "function saveUser() { " +
                        "  const isEdit = document.getElementById('user-is-edit').value === 'true'; "
                        +
                        "  const data = { " +
                        "    username: document.getElementById('user-username').value, " +
                        "    email: document.getElementById('user-email').value, " +
                        "    password: document.getElementById('user-password').value, " +
                        "    profile: document.getElementById('user-profile').value " +
                        "  }; " +
                        "  if(!data.username) { alert('Username is required'); return; } " +
                        "  const url = isEdit ? '/dashboard/security/save?edit=true' : '/dashboard/security/save'; "
                        +
                        "  htmx.ajax('POST', url, { values: data, target: '#security-view', swap: 'outerHTML' }); "
                        +
                        "  closeUserModal(); " +
                        "} " +
                        "function openEditUser(username, email, profile) { " +
                        "  document.getElementById('user-is-edit').value = 'true'; " +
                        "  document.getElementById('user-username').value = username; " +
                        "  document.getElementById('user-username').disabled = true; " +
                        "  document.getElementById('user-email').value = email || ''; " +
                        "  document.getElementById('user-password').value = ''; " +
                        "  document.getElementById('user-password').placeholder = '(Dejar en blanco para mantener)'; "
                        +
                        "  document.getElementById('user-profile').value = profile || 'end-user'; "
                        +
                        "  document.querySelector('#user-modal h3').innerText = 'Edit User: ' + username; "
                        +
                        "  document.getElementById('btn-user-save').innerText = 'Save Changes'; "
                        +
                        "  document.getElementById('user-modal').classList.remove('hidden'); " +
                        "  document.getElementById('user-modal').classList.add('flex'); " +
                        "} " +
                        "function openAddUser() { " +
                        "  document.getElementById('user-is-edit').value = 'false'; " +
                        "  document.getElementById('user-username').value = ''; " +
                        "  document.getElementById('user-username').disabled = false; " +
                        "  document.getElementById('user-email').value = ''; " +
                        "  document.getElementById('user-password').value = ''; " +
                        "  document.getElementById('user-password').placeholder = 'Password'; "
                        +
                        "  document.getElementById('user-profile').value = 'end-user'; " +
                        "  document.querySelector('#user-modal h3').innerText = 'Add New User'; "
                        +
                        "  document.getElementById('btn-user-save').innerText = 'Save User'; " +
                        "  document.getElementById('user-modal').classList.remove('hidden'); " +
                        "  document.getElementById('user-modal').classList.add('flex'); " +
                        "} " +
                        "</script>"));

        return modal;
    }

    private Div createFormField(String labelText, Component field) {
        Div group = new Div("field-grp-" + labelText.toLowerCase().replace(" ", "-"));
        group.setStyleClass("space-y-1.5");
        Label label = new Label("lbl-" + group.getId(), labelText);
        label.setStyleClass("block text-xs font-semibold text-slate-400 uppercase tracking-wider");
        group.addComponent(label);
        group.addComponent(field);
        return group;
    }

    private Modal createCollectionModal() {
        Modal modal = new Modal("collection-modal", "Create Collection");
        modal.setStyleClass(
                "modal-overlay-centered fixed inset-0 z-[100] hidden items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm transition-all duration-300");

        Div content = new Div("col-modal-body");
        content.setStyleClass("space-y-4 min-w-[320px]");

        // Hidden Inputs
        content.addComponent(new Label("col-db-container", "<input type='hidden' id='col-db-name'>"));
        content.addComponent(new Label("col-eng-container", "<input type='hidden' id='col-engine-name'>"));
        // Edit mode flag if needed later, but focusing on Create for now
        content.addComponent(new Label("col-edit-container", "<input type='hidden' id='col-is-edit' value='false'>"));

        // Name Field
        content.addComponent(createFormField("Collection Name", new InputText("col-name")));

        modal.addComponent(content);

        // Buttons
        Button saveBtn = new Button("btn-col-save", "Create");
        saveBtn.setStyleClass(
                "flex-1 px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-bold transition-all");
        saveBtn.addAttribute("onclick", "submitCollection()");
        modal.addFooterComponent(saveBtn);

        Button cancelBtn = new Button("btn-col-cancel", "Cancel");
        cancelBtn.setStyleClass(
                "flex-1 px-6 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg font-medium transition-all");
        cancelBtn.addAttribute("onclick", "closeCollectionModal()");
        modal.addFooterComponent(cancelBtn);

        // Script
        content.addComponent(new Label("script-col",
                "<script> " +
                        "function openCollectionModal(dbName, engineName) { " +
                        "  document.getElementById('col-db-name').value = dbName; " +
                        "  document.getElementById('col-engine-name').value = engineName; " +
                        "  document.getElementById('col-name').value = ''; " +
                        "  document.querySelector('#collection-modal h3').innerText = 'Create Collection in ' + dbName; "
                        +
                        "  document.getElementById('collection-modal').classList.remove('hidden'); " +
                        "  document.getElementById('collection-modal').classList.add('flex'); " +
                        "  setTimeout(() => document.getElementById('col-name').focus(), 100); " +
                        "} " +
                        "function closeCollectionModal() { " +
                        "  document.getElementById('collection-modal').classList.add('hidden'); " +
                        "  document.getElementById('collection-modal').classList.remove('flex'); " +
                        "} " +
                        "function submitCollection() { " +
                        "  const db = document.getElementById('col-db-name').value; " +
                        "  const eng = document.getElementById('col-engine-name').value; " +
                        "  const name = document.getElementById('col-name').value; " +
                        "  if(!name) { alert('Collection name is required'); return; } " +
                        "  htmx.ajax('POST', '/dashboard/collection/save', { " +
                        "    values: { dbName: db, engine: eng, name: name }, " +
                        "    target: '#main-content-view' " +
                        // We use hx-trigger on response header to refresh explorer, or we can manually
                        // refresh here if response doesn't trigger it.
                        // But let's rely on standard htmx response handling.
                        "  }); " +
                        "  closeCollectionModal(); " +
                        "} " +
                        "</script>"));

        return modal;
    }

    @POST
    @Path("/collection/save")
    @jakarta.ws.rs.Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response saveCollection(@jakarta.ws.rs.FormParam("dbName") String dbName,
            @jakarta.ws.rs.FormParam("name") String name,
            @jakarta.ws.rs.FormParam("engine") String engine) {
        String token = null;
        if (headers.getCookies().containsKey("auth_token")) {
            token = headers.getCookies().get("auth_token").getValue();
            if (token != null && token.startsWith("\"") && token.endsWith("\"")) {
                token = token.substring(1, token.length() - 1);
            }
        }

        if (token == null || token.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Normalize engine name (UI label "Document (Collection)" -> Backend type
        // "Document")
        if ("Document (Collection)".equals(engine)) {
            engine = "Document";
        }

        io.jettra.example.ui.model.Collection col = new io.jettra.example.ui.model.Collection(name, engine);

        try {
            pdClient.createCollection(dbName, name, col, "Bearer " + token);
            // Return empty response with trigger to refresh explorer
            return Response.ok().header("HX-Trigger", "refreshExplorer").build();
        } catch (Exception e) {
            LOG.error("Failed to create collection", e);
            // Return validation error or alert script
            return Response.ok("<script>alert('Failed to create collection: " + e.getMessage() + "');</script>")
                    .build();
        }
    }

    private Modal createDocumentModal() {
        Modal modal = new Modal("document-modal", "Manage Document");
        modal.setStyleClass(
                "modal-overlay-centered fixed top-0 left-0 right-0 z-[100] hidden w-full p-4 overflow-x-hidden overflow-y-auto md:inset-0 h-[calc(100%-1rem)] max-h-full bg-slate-950/80 backdrop-blur-sm transition-all duration-300");

        Div content = new Div("doc-modal-body");
        content.setStyleClass("space-y-4 min-w-[500px] max-w-2xl text-white");
        content.addComponent(new Label("doc-loading", "Loading form..."));

        modal.addComponent(content);

        Button cancelBtn = new Button("btn-doc-cancel", "Cancel");
        cancelBtn.setStyleClass(
                "w-full px-6 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg font-medium transition-all");
        cancelBtn.addAttribute("data-modal-hide", "document-modal");
        cancelBtn.addAttribute("type", "button");
        modal.addFooterComponent(cancelBtn);

        return modal;
    }

    private Modal createDocumentDeleteModal() {
        Modal modal = new Modal("doc-delete-modal", "Delete Document");
        modal.setStyleClass(
                "modal-overlay-centered fixed top-0 left-0 right-0 z-[100] hidden w-full p-4 overflow-x-hidden overflow-y-auto md:inset-0 h-[calc(100%-1rem)] max-h-full bg-slate-950/80 backdrop-blur-sm transition-all duration-300");

        Div content = new Div("doc-del-body");
        content.setStyleClass("text-center space-y-4 text-white");
        content.addComponent(new Label("doc-del-loading", "Loading..."));

        modal.addComponent(content);

        Button cancelBtn = new Button("btn-doc-del-cancel", "Cancel");
        cancelBtn.setStyleClass(
                "w-full px-6 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg font-medium transition-all");
        cancelBtn.addAttribute("data-modal-hide", "doc-delete-modal");
        cancelBtn.addAttribute("type", "button");
        modal.addFooterComponent(cancelBtn);

        return modal;
    }
}
