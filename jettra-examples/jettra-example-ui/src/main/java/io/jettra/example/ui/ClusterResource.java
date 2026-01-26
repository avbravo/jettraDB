package io.jettra.example.ui;

import io.jettra.example.ui.model.Node;
import io.jettra.example.ui.model.RaftGroup;
import io.jettra.example.ui.service.ClusterService;
import io.jettra.ui.component.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/dashboard/cluster")
public class ClusterResource {

        @Inject
        ClusterService clusterService;

        @Inject
        io.jettra.example.ui.service.SecurityService securityService;

        @jakarta.ws.rs.core.Context
        jakarta.ws.rs.core.HttpHeaders headers;

        private String getAuthToken() {
                if (headers.getCookies().containsKey("auth_token")) {
                        return headers.getCookies().get("auth_token").getValue();
                }
                return null;
        }

        @GET
        @Produces(MediaType.TEXT_HTML)
        public Response getClusterView() {
                String token = getAuthToken();
                String username = headers.getCookies().containsKey("user_session")
                                ? headers.getCookies().get("user_session").getValue()
                                : null;

                boolean isSuperUser = "admin".equalsIgnoreCase(username) || "super-user".equalsIgnoreCase(username);
                if (!isSuperUser && username != null && token != null) {
                        var user = securityService.getUser(username, token);
                        if (user != null && "super-user".equalsIgnoreCase(user.getProfile())) {
                                isSuperUser = true;
                        }
                }

                Div content = new Div("cluster-view");

                Label pageTitle = new Label("page-title", "Cluster Overview");
                pageTitle.setStyleClass("text-2xl font-bold text-gray-900 dark:text-white mb-6");
                content.addComponent(pageTitle);

                // Nodes Section
                Div cardGrid = new Div("node-grid");
                cardGrid.setStyleClass("grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8");

                List<Node> nodes = clusterService.getNodes(token);
                System.out.println("DEBUG: ClusterResource nodes size=" + (nodes != null ? nodes.size() : "null"));
                if (nodes == null || nodes.isEmpty()) {
                        Label empty = new Label("empty-nodes", "No nodes found or unable to connect to PD.");
                        empty.setStyleClass("text-gray-500 dark:text-gray-400");
                        cardGrid.addComponent(empty);
                } else {
                        for (Node node : nodes) {
                                cardGrid.addComponent(createNodeCard(node, isSuperUser));
                        }
                }
                content.addComponent(cardGrid);

                // Groups Section
                Label groupTitle = new Label("group-title", "Multi-Raft Groups");
                groupTitle.setStyleClass("text-xl font-bold text-gray-900 dark:text-white mb-4");
                content.addComponent(groupTitle);

                Div groupGrid = new Div("group-grid");
                groupGrid.setStyleClass("grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6");

                List<RaftGroup> groups = clusterService.getGroups(token);
                if (groups.isEmpty()) {
                        Label empty = new Label("empty-groups", "No Raft groups found.");
                        empty.setStyleClass("text-gray-500 dark:text-gray-400");
                        groupGrid.addComponent(empty);
                } else {
                        for (RaftGroup group : groups) {
                                groupGrid.addComponent(createGroupCard(group));
                        }
                }
                content.addComponent(groupGrid);

                // Modals - Placeholders that will be populated via JS or simple structure
                // Since jettra-ui Modals are static in structure, we need a way to pass ID to
                // them.
                // For now, we will use a generic modal and JS to populate the ID if possible,
                // OR we just render the modal structure and use client-side attributes.
                content.addComponent(createStopModal());
                content.addComponent(createDetailsModal());

                return Response.ok(content.render()).build();
        }

        @POST
        @Path("/stop/{id}")
        public Response stopNode(@PathParam("id") String id) {
                boolean success = clusterService.stopNode(id, getAuthToken());
                if (success) {
                        // Return HTMX redirect or simple success message
                        return Response.ok().header("HX-Refresh", "true").build();
                } else {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
        }

        private Card createNodeCard(Node node, boolean canStop) {
                Card card = new Card(node.getId());

                Div header = new Div(node.getId() + "-header");
                header.setStyleClass("flex justify-between items-start mb-4");

                Div titleContainer = new Div(node.getId() + "-titles");
                Label title = new Label(node.getId() + "-lbl", "Node: " + node.getId());
                title.setStyleClass("text-lg font-bold text-white mb-1");
                titleContainer.addComponent(title);

                if ("LEADER".equalsIgnoreCase(node.getRaftRole())) {
                        Badge leaderBadge = new Badge(node.getId() + "-leader", "LEADER");
                        leaderBadge.setStyleClass(
                                        "text-[10px] bg-indigo-500/20 text-indigo-400 border border-indigo-500/30 px-2 py-0.5 rounded font-bold uppercase tracking-wider");
                        titleContainer.addComponent(leaderBadge);
                }
                header.addComponent(titleContainer);

                // Status Dot & Text
                Div statusContainer = new Div(node.getId() + "-status-cont");
                statusContainer.setStyleClass("flex items-center gap-2");

                boolean isOnline = "ONLINE".equalsIgnoreCase(node.getStatus())
                                || "UP".equalsIgnoreCase(node.getStatus());
                String colorClass = isOnline ? "bg-emerald-500 shadow-[0_0_8px_#10b981]"
                                : "bg-rose-500 shadow-[0_0_8px_#ef4444]";

                Span dot = new Span(node.getId() + "-dot");
                dot.setStyleClass("status-dot " + colorClass);
                statusContainer.addComponent(dot);

                Label statusText = new Label(node.getId() + "-status-text", node.getStatus().toUpperCase());
                statusText.setStyleClass("text-xs font-bold " + (isOnline ? "text-emerald-400" : "text-rose-400"));
                statusContainer.addComponent(statusText);

                header.addComponent(statusContainer);
                card.addComponent(header);

                Div info = new Div(node.getId() + "-info");
                info.setStyleClass("space-y-3 text-sm text-slate-400");
                info.addComponent(new Label(node.getId() + "-role-lbl", "Role: " + node.getRole()));
                info.addComponent(new Label(node.getId() + "-addr-lbl", "Address: " + node.getAddress()));

                // Resource Metrics
                Div metrics = new Div(node.getId() + "-metrics");
                metrics.setStyleClass("pt-3 border-t border-white/5 space-y-2");

                String cpuVal = String.format("%.1f%%", node.getCpuUsage());
                metrics.addComponent(createMetricItem("CPU Load", cpuVal, (int) node.getCpuUsage(), "indigo"));

                String ramVal = formatBytes(node.getMemoryUsage()) + " / " + formatBytes(node.getMemoryMax());
                int ramPct = node.getMemoryMax() > 0 ? (int) (node.getMemoryUsage() * 100 / node.getMemoryMax()) : 0;
                metrics.addComponent(createMetricItem("Memory (RAM)", ramVal, ramPct, "emerald"));

                String diskVal = formatBytes(node.getDiskUsage()) + " / " + formatBytes(node.getDiskMax());
                int diskPct = node.getDiskMax() > 0 ? (int) (node.getDiskUsage() * 100 / node.getDiskMax()) : 0;
                metrics.addComponent(createMetricItem("Disk Storage", diskVal, diskPct, "blue"));

                card.addComponent(info);
                card.addComponent(metrics);

                // Actions
                if (canStop) {
                        Div actions = new Div(node.getId() + "-actions");
                        actions.setStyleClass("mt-6");

                        Button stopBtn = new Button(node.getId() + "-stop", "🛑 Stop Node");
                        stopBtn.setStyleClass(
                                        "w-full px-4 py-2.5 text-xs font-bold bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 rounded-lg border border-rose-500/20 transition-all");
                        stopBtn.addAttribute("onclick", "prepareStop('" + node.getId() + "')");
                        stopBtn.addAttribute("data-modal-target", "stop-modal");
                        stopBtn.addAttribute("data-modal-toggle", "stop-modal");
                        actions.addComponent(stopBtn);

                        card.addComponent(actions);
                }

                return card;
        }

        private Div createMetricItem(String label, String value, int percent, String color) {
                Div item = new Div(label.replace(" ", "-").toLowerCase());
                item.setStyleClass("space-y-1");

                Div header = new Div(item.getId() + "-h");
                header.setStyleClass("flex justify-between text-[11px] font-medium");
                header.addComponent(new Label(item.getId() + "-lbl", label));
                header.addComponent(new Label(item.getId() + "-val", value));
                item.addComponent(header);

                Div barBg = new Div(item.getId() + "-bar-bg");
                barBg.setStyleClass("w-full bg-white/5 rounded-full h-1.5 overflow-hidden");

                String barColor = switch (color) {
                        case "indigo" -> "bg-indigo-500 shadow-[0_0_8px_rgba(99,102,241,0.4)]";
                        case "emerald" -> "bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.4)]";
                        default -> "bg-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.4)]";
                };

                Div bar = new Div(item.getId() + "-bar");
                bar.setStyleClass(barColor + " h-full transition-all duration-500");
                bar.addAttribute("style", "width: " + Math.min(percent, 100) + "%;");

                barBg.addComponent(bar);
                item.addComponent(barBg);
                return item;
        }

        private String formatBytes(long bytes) {
                if (bytes < 1024)
                        return bytes + " B";
                int exp = (int) (Math.log(bytes) / Math.log(1024));
                String pre = "KMGTPE".charAt(exp - 1) + "";
                return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
        }

        private Card createGroupCard(RaftGroup group) {
                Card card = new Card("group-" + group.getGroupId());
                card.setTitle("Group " + group.getGroupId());

                Div info = new Div("grp-" + group.getGroupId() + "-info");
                info.setStyleClass("mt-2 space-y-1 text-sm text-gray-600 dark:text-gray-400");

                Label leaderLbl = new Label("grp-" + group.getGroupId() + "-L", "Leader: " + group.getLeaderId());
                leaderLbl.setStyleClass("font-semibold text-indigo-600 dark:text-indigo-400");
                info.addComponent(leaderLbl);

                info.addComponent(new Label("grp-" + group.getGroupId() + "-P", "Peers: " + group.getPeers().size()));

                card.addComponent(info);
                return card;
        }

        private Modal createStopModal() {
                Modal modal = new Modal("stop-modal", "Stop Node");

                Div content = new Div("stop-content");
                content.addComponent(new Label("stop-msg",
                                "Are you sure you want to stop node <span id='stop-node-id' class='font-bold'></span>? This action cannot be undone."));
                modal.addComponent(content);

                Div footer = new Div("stop-footer");
                footer.setStyleClass(
                                "flex items-center p-6 space-x-2 border-t border-gray-200 rounded-b dark:border-gray-600");

                Button confirmBtn = new Button("btn-stop-confirm", "Yes, Stop Node");
                confirmBtn.setStyleClass(
                                "text-white bg-red-600 hover:bg-red-800 focus:ring-4 focus:outline-none focus:ring-red-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-red-600 dark:hover:bg-red-700 dark:focus:ring-red-800");
                // We use a custom JS function to trigger HTMX
                confirmBtn.addAttribute("onclick", "confirmStopNode()");
                modal.addFooterComponent(confirmBtn);

                Button cancelBtn = new Button("btn-stop-cancel", "Cancel");
                cancelBtn.setStyleClass(
                                "text-gray-500 bg-white hover:bg-gray-100 focus:ring-4 focus:outline-none focus:ring-blue-300 rounded-lg border border-gray-200 text-sm font-medium px-5 py-2.5 hover:text-gray-900 focus:z-10 dark:bg-gray-700 dark:text-gray-300 dark:border-gray-500 dark:hover:text-white dark:hover:bg-gray-600 dark:focus:ring-gray-600");
                cancelBtn.addAttribute("data-modal-hide", "stop-modal");
                modal.addFooterComponent(cancelBtn);

                // Add minimal script for passing ID
                content.addComponent(new Label("script-stop",
                                "<script> var targetNodeId = ''; function prepareStop(id) { targetNodeId = id; document.getElementById('stop-node-id').innerText = id; } function confirmStopNode() { if(targetNodeId) { htmx.ajax('POST', '/dashboard/cluster/stop/' + targetNodeId, { swap:'none' }).then(() => { FlowbiteInstances.getInstance('Modal', 'stop-modal').hide(); }); } }</script>"));

                return modal;
        }

        private Modal createDetailsModal() {
                Modal modal = new Modal("details-modal", "Node Details");

                Div content = new Div("details-content");
                content.addComponent(new Label("details-msg",
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
}
