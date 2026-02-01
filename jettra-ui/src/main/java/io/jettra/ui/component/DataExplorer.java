package io.jettra.ui.component;

import java.util.ArrayList;
import java.util.List;

/**
 * DataExplorer is a specialized Tree component for JettraDB that renders
 * databases, engines, collections and sub-options (Records, Index, etc.)
 */
public class DataExplorer extends Component {
        private List<DatabaseNode> databases = new ArrayList<>();

        public DataExplorer(String id) {
                super(id);
                this.styleClass = "data-explorer-container space-y-1 mt-4";
        }

        public void addDatabase(DatabaseNode db) {
                databases.add(db);
        }

        @Override
        public String render() {
                StringBuilder sb = new StringBuilder();
                sb.append("<div id='").append(id).append("' class='").append(styleClass).append("'>");
                sb.append("<div class='tree-root flex flex-col gap-1'>");
                for (DatabaseNode db : databases) {
                        sb.append(db.render());
                }
                sb.append("</div></div>");

                // Add minimal JS for toggling. The user said "no uses html ni javascript"
                // meaning
                // they don't want to WRITE it, but the component must GENERATE it to work.
                sb.append("<script>");
                sb.append("if (typeof toggleDataExplorerNode !== 'function') {");
                sb.append("  function toggleDataExplorerNode(id, iconId) {");
                sb.append("    const el = document.getElementById(id);");
                sb.append("    if (el) {");
                sb.append("      el.classList.toggle('hidden');");
                sb.append("      const isHidden = el.classList.contains('hidden');");
                sb.append("      localStorage.setItem('data_explorer_expanded_' + id, !isHidden);");
                sb.append("      const icon = document.getElementById(iconId);");
                sb.append("      if (icon) {");
                sb.append("        if (isHidden) {");
                sb.append("          icon.style.transform = 'rotate(0deg)';");
                sb.append("        } else {");
                sb.append("          icon.style.transform = 'rotate(90deg)';");
                sb.append("        }");
                sb.append("      }");
                sb.append("    }");
                sb.append("  }");
                sb.append("}");

                sb.append("if (typeof restoreDataExplorerState !== 'function') {");
                sb.append("  function restoreDataExplorerState() {");
                sb.append("    document.querySelectorAll('[id^=\"db-children-\"], [id^=\"users-\"], [id^=\"engine-children-\"], [id^=\"col-children-\"]').forEach(el => {");
                sb.append("      const expanded = localStorage.getItem('data_explorer_expanded_' + el.id) === 'true';");
                sb.append("      if (expanded) {");
                sb.append("        el.classList.remove('hidden');");
                sb.append("        const iconId = el.id.replace('children', 'icon').replace('db-children', 'db-icon');");
                sb.append("        const icon = document.getElementById(iconId);");
                sb.append("        if (icon) icon.style.transform = 'rotate(90deg)';");
                sb.append("      }");
                sb.append("    });");
                sb.append("  }");
                sb.append("  document.addEventListener('DOMContentLoaded', restoreDataExplorerState);");
                sb.append("  document.body.addEventListener('htmx:afterOnLoad', restoreDataExplorerState);");
                sb.append("}");
                sb.append("restoreDataExplorerState();"); // Run it once in case it just loaded via HTMX
                sb.append("</script>");
                return sb.toString();
        }

        public static class DatabaseNode {
                private String name;
                private List<EngineNode> engines = new ArrayList<>();
                private java.util.Map<String, String> usersWithRoles = new java.util.LinkedHashMap<>();

                public DatabaseNode(String name) {
                        this.name = name;
                }

                public void addEngine(EngineNode engine) {
                        engines.add(engine);
                }

                public void addUser(String username, String role) {
                        usersWithRoles.put(username, role);
                }

                public String render() {
                        String safeName = name.replaceAll("[^a-zA-Z0-9]", "-");
                        String childrenId = "db-children-" + safeName;
                        String iconId = "db-icon-" + safeName;

                        StringBuilder sb = new StringBuilder();
                        sb.append("<div class='tree-db-item'>");
                        sb.append(
                                        "<div class='tree-db-name relative flex items-center gap-2 p-1.5 cursor-pointer hover:bg-indigo-600/10 rounded-lg text-slate-300 transition-all group' onclick=\"toggleDataExplorerNode('")
                                        .append(childrenId).append("', '").append(iconId).append("')\">");
                        sb.append("<svg id='").append(iconId).append(
                                        "' class='w-3 h-3 transition-transform duration-200 text-slate-500 group-hover:text-indigo-400' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='4' d='M9 5l7 7-7 7'></path></svg>");
                        sb.append("<span class='font-bold group-hover:text-white'>").append(name).append("</span>");

                        // Database Action Buttons
                        sb.append("<div class='absolute right-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity'>");

                        // Info Button
                        sb.append("<button class='p-1 hover:text-indigo-400 text-slate-500 transition-colors' title='Information' ")
                                        .append("hx-get='/dashboard/database/info?name=").append(name).append("' ")
                                        .append("hx-target='#main-content-view' onclick='event.stopPropagation()'>")
                                        .append("<svg class='w-3.5 h-3.5' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z'></path></svg>")
                                        .append("</button>");

                        // Security Button
                        sb.append(
                                        "<button class='p-1 hover:text-green-400 text-slate-500 transition-colors' title='Security & Permissions' ")
                                        .append("hx-get='/dashboard/database/security?name=").append(name).append("' ")
                                        .append("hx-target='#main-content-view' onclick='event.stopPropagation()'>")
                                        .append("<svg class='w-3.5 h-3.5' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z'></path></svg>")
                                        .append("</button>");

                        // Edit Button
                        sb.append("<button class='p-1 hover:text-amber-400 text-slate-500 transition-colors' title='Edit Config' ")
                                        .append("hx-get='/dashboard/database/edit?name=").append(name).append("' ")
                                        .append("hx-target='#main-content-view' onclick='event.stopPropagation()'>")
                                        .append("<svg class='w-3.5 h-3.5' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z'></path></svg>")
                                        .append("</button>");

                        // Delete Button
                        sb.append(
                                        "<button class='p-1 hover:text-red-400 text-slate-500 transition-colors' title='Delete Database' ")
                                        .append("hx-get='/dashboard/database/delete?name=").append(name).append("' ")
                                        .append("hx-target='#main-content-view' onclick='event.stopPropagation()'>")
                                        .append("<svg class='w-3.5 h-3.5' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16'></path></svg>")
                                        .append("</button>");

                        sb.append("</div>");
                        sb.append("</div>");
                        sb.append("<div id='").append(childrenId)
                                        .append("' class='tree-children hidden ml-4 border-l border-slate-700/30 pl-3 space-y-0.5'>");

                        // Users node (Subtree)
                        if (!usersWithRoles.isEmpty() || true) { // Always show per user request example
                                String usersId = "users-" + safeName;
                                String usersIconId = "users-icon-" + safeName;
                                sb.append(
                                                "<div class='flex items-center gap-2 p-1 cursor-pointer hover:text-white text-slate-400 text-sm transition-colors group' onclick=\"toggleDataExplorerNode('")
                                                .append(usersId).append("', '").append(usersIconId).append("')\">");
                                sb.append(
                                                "<svg class='w-3.5 h-3.5 text-slate-500 group-hover:text-indigo-400' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z'></path></svg>");
                                sb.append("<span>users</span>");
                                sb.append("</div>");
                                sb.append("<div id='").append(usersId)
                                                .append("' class='hidden ml-5 border-l border-slate-700/30 space-y-1 py-1'>");
                                for (java.util.Map.Entry<String, String> entry : usersWithRoles.entrySet()) {
                                        String username = entry.getKey();
                                        String role = entry.getValue();
                                        String roleColor = switch (role.toLowerCase()) {
                                                case "admin" -> "text-amber-400 bg-amber-400/10 border-amber-400/20";
                                                case "read-write" ->
                                                        "text-indigo-400 bg-indigo-400/10 border-indigo-400/20";
                                                case "super-user" ->
                                                        "text-purple-400 bg-purple-400/10 border-purple-400/20";
                                                default -> "text-slate-400 bg-slate-400/10 border-slate-700/50";
                                        };

                                        sb.append(
                                                        "<div class='flex items-center justify-between group/user px-2 py-0.5 hover:bg-slate-800/50 rounded transition-colors'>");
                                        sb.append("<span class='text-[11px] text-slate-400 group-hover/user:text-slate-200'>")
                                                        .append(username).append("</span>");
                                        sb.append(
                                                        "<span class='text-[9px] px-1.5 py-0 border rounded-full font-bold uppercase tracking-tighter ")
                                                        .append(roleColor).append("'>")
                                                        .append(role).append("</span>");
                                        sb.append("</div>");
                                }
                                sb.append("</div>");
                        }

                        for (EngineNode engine : engines) {
                                sb.append(engine.render(safeName));
                        }
                        sb.append("</div></div>");
                        return sb.toString();
                }
        }

        public static class EngineNode {
                private String name;
                private String iconPath;
                private List<CollectionNode> collections = new ArrayList<>();

                public EngineNode(String name, String iconPath) {
                        this.name = name;
                        this.iconPath = iconPath;
                }

                public void addCollection(CollectionNode col) {
                        collections.add(col);
                }

                public String render(String dbId) {
                        String safeName = name.replaceAll("[^a-zA-Z0-9]", "-");
                        String childrenId = "engine-children-" + dbId + "-" + safeName;
                        String iconId = "engine-icon-" + dbId + "-" + safeName;

                        StringBuilder sb = new StringBuilder();
                        sb.append("<div class='tree-engine-node'>");
                        sb.append(
                                        "<div class='tree-type-item flex items-center gap-2 p-1 cursor-pointer hover:text-white text-slate-400 text-sm transition-colors group' onclick=\"toggleDataExplorerNode('")
                                        .append(childrenId).append("', '").append(iconId).append("')\">");
                        sb.append(
                                        "<svg class='w-3.5 h-3.5 text-slate-500 group-hover:text-indigo-400' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='")
                                        .append(iconPath).append("'></path></svg>");
                        sb.append("<span>").append(name).append("</span>");

                        // Engine Actions (Add Collection, Refresh)
                        sb.append("<div class='flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity ml-auto'>");

                        // Add Collection Button (Only for Document engine initially, or all if generic)
                        // Using HTMX to open modal
                        sb.append(
                                        "<button class='p-0.5 hover:text-indigo-400 text-slate-500 transition-colors' title='Add Collection' ")
                                        .append("onclick=\"openCollectionModal('").append(dbId).append("', '")
                                        .append(name)
                                        .append("'); event.stopPropagation()\">")
                                        .append("<svg class='w-3.5 h-3.5' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M12 4v16m8-8H4'></path></svg>")
                                        .append("</button>");

                        // Refresh Button
                        sb.append("<button class='p-0.5 hover:text-emerald-400 text-slate-500 transition-colors' title='Refresh' ")
                                        .append("onclick='event.stopPropagation(); htmx.trigger(\"#sidebar-explorer-container\", \"refreshExplorer\")'>")
                                        .append("<svg class='w-3.5 h-3.5' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15'></path></svg>")
                                        .append("</button>");

                        sb.append("</div>"); // End actions

                        sb.append("</div>");
                        sb.append("<div id='").append(childrenId)
                                        .append("' class='hidden ml-5 border-l border-slate-700/30 flex flex-col'>");
                        for (CollectionNode col : collections) {
                                sb.append(col.render(dbId, safeName));
                        }
                        sb.append("</div></div>");
                        return sb.toString();
                }
        }

        public static class CollectionNode {
                private String name;

                public CollectionNode(String name) {
                        this.name = name;
                }

                public String render(String dbId, String engineId) {
                        String safeName = name.replaceAll("[^a-zA-Z0-9]", "-");
                        String childrenId = "col-children-" + dbId + "-" + engineId + "-" + safeName;

                        StringBuilder sb = new StringBuilder();
                        sb.append("<div class='tree-col-item'>");
                        sb.append(
                                        "<div class='flex items-center gap-2 p-0.5 mt-0.5 cursor-pointer hover:text-white text-slate-400 text-sm transition-colors group' onclick=\"toggleDataExplorerNode('")
                                        .append(childrenId).append("', '')\">");
                        sb.append(
                                        "<svg class='w-3 h-3 text-slate-600 group-hover:text-indigo-400' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M4 7v10a2 2 0 002 2h12a2 2 0 002-2V7a2 2 0 00-2-2H6a2 2 0 00-2 2z'></path></svg>");
                        sb.append("<span class='font-medium'>").append(name).append("</span>");

                        // Collection Actions
                        sb.append("<div class='flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity ml-auto'>");

                        // Info Button
                        sb.append("<div class='cursor-pointer text-slate-500 hover:text-blue-400' title='Info' ")
                                        .append("onclick=\"event.stopPropagation(); htmx.ajax('GET', '/dashboard/collection/info?db=")
                                        .append(dbId).append("&col=").append(name)
                                        .append("', {target:'#main-content-view'})\">")
                                        .append("<svg class='w-3 h-3' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z'></path></svg>")
                                        .append("</div>");

                        // Rename Button
                        sb.append("<div class='cursor-pointer text-slate-500 hover:text-amber-400' title='Rename' ")
                                        .append("onclick=\"event.stopPropagation(); htmx.ajax('GET', '/dashboard/collection/edit?db=")
                                        .append(dbId).append("&col=").append(name).append("&engine=").append(engineId)
                                        .append("', {target:'#main-content-view'})\">")
                                        .append("<svg class='w-3 h-3' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z'></path></svg>")
                                        .append("</div>");

                        // Delete Button
                        sb.append("<div class='cursor-pointer text-slate-500 hover:text-red-400' title='Delete' ")
                                        .append("onclick=\"event.stopPropagation(); openCollectionDeleteModal('")
                                        .append(dbId).append("', '").append(name)
                                        .append("')\">")
                                        .append("<svg class='w-3 h-3' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16'></path></svg>")
                                        .append("</div>");

                        sb.append("</div>"); // End actions

                        sb.append("</div>");

                        sb.append("<div id='").append(childrenId)
                                        .append("' class='hidden ml-3 border-l border-slate-700/30 pl-2 flex flex-col gap-0.5 my-0.5'>");
                        sb.append(renderOption("Record(Document)",
                                        "M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10",
                                        "indigo-400",
                                        "/dashboard/document/explorer?db=" + dbId + "&col=" + name,
                                        "#main-content-view"));
                        sb.append(renderOption("Index", "M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z", "slate-500",
                                        null, null));
                        sb.append(renderOption("Sequences", "M12 4v16m8-8H4", "slate-500", null, null));
                        sb.append(renderOption("Rules",
                                        "M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z",
                                        "slate-500", null, null));
                        sb.append("</div>");
                        sb.append("</div>");
                        return sb.toString();
                }

                private String renderOption(String label, String iconPath, String colorClass, String hxGet,
                                String hxTarget) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("<div class='flex items-center gap-2 p-0.5 cursor-pointer hover:text-white text-")
                                        .append(colorClass)
                                        .append(" text-[10px] uppercase font-semibold tracking-wider transition-colors'");

                        if (hxGet != null) {
                                sb.append(" hx-get='").append(hxGet).append("'");
                        }
                        if (hxTarget != null) {
                                sb.append(" hx-target='").append(hxTarget).append("'");
                        }

                        sb.append(">");
                        sb.append("<svg class='w-2.5 h-2.5 opacity-70' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='")
                                        .append(iconPath).append("'></path></svg>");
                        sb.append("<span>").append(label).append("</span>");
                        sb.append("</div>");

                        return sb.toString();
                }
        }
}
