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
        private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(ClusterResource.class);

        @Inject
        ClusterService clusterService;

        @Inject
        io.jettra.example.ui.service.SecurityService securityService;

        @jakarta.ws.rs.core.Context
        jakarta.ws.rs.core.HttpHeaders headers;

        private String getAuthToken() {
                if (headers == null) {
                        LOG.error("DEBUG: HttpHeaders @Context IS NULL!");
                        return null;
                }
                if (headers.getCookies().containsKey("auth_token")) {
                        String token = headers.getCookies().get("auth_token").getValue();
                        LOG.infof("DEBUG (Cluster): Raw auth_token cookie: [%s]", token);
                        if (token != null && token.startsWith("\"") && token.endsWith("\"")) {
                                token = token.substring(1, token.length() - 1);
                                LOG.infof("DEBUG (Cluster): Cleaned auth_token: [%s]", token);
                        }
                        return token;
                }
                LOG.warn("DEBUG (Cluster): auth_token cookie NOT FOUND");
                return null;
        }

        @GET
        @Produces(MediaType.TEXT_HTML)
        public Response getClusterView() {
                String token = getAuthToken();
                LOG.infof("DEBUG (Cluster): Cookies present: %s", headers.getCookies().keySet());
                String username = headers.getCookies().containsKey("user_session")
                                ? headers.getCookies().get("user_session").getValue()
                                : null;
                LOG.infof("DEBUG (Cluster): Request by user: %s", username);

                boolean isSuperUser = "admin".equalsIgnoreCase(username) || "super-user".equalsIgnoreCase(username);
                if (!isSuperUser && username != null && token != null) {
                        var user = securityService.getUser(username, token);
                        if (user != null && "super-user".equalsIgnoreCase(user.getProfile())) {
                                isSuperUser = true;
                        }
                }

                Div content = new Div("cluster-view");
                content.addAttribute("hx-get", "/dashboard/cluster");
                content.addAttribute("hx-trigger", "every 5s");
                content.addAttribute("hx-swap", "outerHTML");

                Label pageTitle = new Label("page-title", "Cluster Overview");
                pageTitle.setStyleClass("text-2xl font-bold text-gray-900 dark:text-white mb-6");
                content.addComponent(pageTitle);

                // Nodes Section
                Div cardGrid = new Div("node-grid");
                cardGrid.setStyleClass("grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8");

                List<Node> nodes = clusterService.getNodes(token);
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

                return Response.ok(content.render()).build();
        }

        @POST
        @Path("/stop/{id}")
        public Response stopNode(@PathParam("id") String id) {
                boolean success = clusterService.stopNode(id, getAuthToken());
                if (success) {
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
                if (canStop && isOnline) {
                        Div actions = new Div(node.getId() + "-actions");
                        actions.setStyleClass("mt-6");

                        Button stopBtn = new Button(node.getId() + "-stop", "🛑 Stop Node");
                        stopBtn.setStyleClass(
                                        "w-full px-4 py-2.5 text-xs font-bold bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 rounded-lg border border-rose-500/20 transition-all");
                        stopBtn.addAttribute("onclick", "prepareStop('" + node.getId() + "')");
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
}
