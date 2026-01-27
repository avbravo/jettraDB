package io.jettra.example.ui;

import io.jettra.example.ui.model.User;
import io.jettra.example.ui.service.SecurityService;
import io.jettra.ui.component.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.ArrayList;

@Path("/dashboard/security")
public class SecurityResource {

        @Inject
        SecurityService securityService;

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
        public Response getSecurityView(@jakarta.ws.rs.QueryParam("page") Integer pageIdx) {
                String token = getAuthToken();
                String currentLoggedInUser = headers.getCookies().containsKey("user_session")
                                ? headers.getCookies().get("user_session").getValue()
                                : "";

                Div content = new Div("security-view");

                Label pageTitle = new Label("page-title", "Security Management");
                pageTitle.setStyleClass("text-2xl font-bold text-gray-900 dark:text-white mb-6");
                content.addComponent(pageTitle);

                // Users Section
                Card usersCard = new Card("users-card");
                usersCard.setTitle("Users");

                Table usersTable = new Table("users-table");
                usersTable.addHeader("Username");
                usersTable.addHeader("Profile");
                usersTable.addHeader("Roles");
                usersTable.addHeader("Actions");

                List<User> users = securityService.getUsers(token);
                int pageSize = 5;
                int currentPage = (pageIdx == null || pageIdx < 0) ? 0 : pageIdx;
                int totalUsers = users.size();
                int totalPages = (int) Math.ceil((double) totalUsers / pageSize);
                if (currentPage >= totalPages && totalPages > 0)
                        currentPage = totalPages - 1;

                int start = currentPage * pageSize;
                int end = Math.min(start + pageSize, totalUsers);
                List<User> paginatedUsers = (start < totalUsers) ? users.subList(start, end) : new ArrayList<>();

                for (User user : paginatedUsers) {
                        List<String> row = new ArrayList<>();
                        row.add("<div class='flex items-center gap-2'><div class='w-7 h-7 bg-indigo-500/20 text-indigo-400 rounded-full flex items-center justify-center text-[10px] font-bold'>"
                                        + user.getUsername().substring(0, 1).toUpperCase() + "</div>"
                                        + user.getUsername() + "</div>");
                        row.add("<span class='px-2 py-0.5 rounded-full text-[10px] bg-slate-800 text-slate-400 border border-slate-700'>"
                                        + (user.getProfile() != null ? user.getProfile() : "end-user") + "</span>");
                        row.add(user.getRoles() != null ? String.join(", ", user.getRoles()) : "");

                        // Actions
                        StringBuilder actions = new StringBuilder();
                        boolean isSuperUserRecord = "super-user".equalsIgnoreCase(user.getUsername());
                        boolean canEdit = !isSuperUserRecord || "super-user".equalsIgnoreCase(currentLoggedInUser);
                        boolean canDelete = !isSuperUserRecord;

                        if (canEdit) {
                                actions.append(String.format(
                                                "<button onclick=\"openEditUser('%s', '%s', '%s')\" class='text-indigo-400 hover:text-indigo-300 font-medium mr-3 transition-colors'>Edit</button>",
                                                user.getUsername(), user.getEmail() != null ? user.getEmail() : "",
                                                user.getProfile() != null ? user.getProfile() : "end-user"));
                        }
                        if (canDelete) {
                                actions.append(String.format(
                                                "<button onclick=\"prepareDeleteUser('%s')\" class='text-rose-400 hover:text-rose-300 font-medium transition-colors'>Delete</button>",
                                                user.getUsername()));
                        }
                        row.add(actions.toString());

                        usersTable.addRow(row);
                }

                usersCard.addComponent(usersTable);

                // Add Pagination Info
                Div paginationInfo = new Div("sec-pagination");
                paginationInfo.setStyleClass("mt-4 flex justify-between items-center text-xs text-slate-500");
                paginationInfo.addComponent(
                                new Label("lbl-pagi",
                                                "Showing " + (start + 1) + "-" + end + " of " + totalUsers + " users"));

                Div pagiBtns = new Div("pagi-btns");
                pagiBtns.setStyleClass("flex gap-2");

                String prevOpacity = currentPage > 0 ? "hover:bg-slate-700" : "opacity-50 cursor-not-allowed";
                String prevAttr = currentPage > 0
                                ? String.format("hx-get='/dashboard/security?page=%d' hx-target='#security-view' hx-swap='outerHTML'",
                                                currentPage - 1)
                                : "";
                pagiBtns.addComponent(new Label("btn-pagi-prev",
                                String.format("<button %s class='px-2 py-1 bg-slate-800 rounded %s transition-colors'>&lt;</button>",
                                                prevAttr, prevOpacity)));

                String nextOpacity = currentPage < totalPages - 1 ? "hover:bg-slate-700"
                                : "opacity-50 cursor-not-allowed";
                String nextAttr = currentPage < totalPages - 1
                                ? String.format("hx-get='/dashboard/security?page=%d' hx-target='#security-view' hx-swap='outerHTML'",
                                                currentPage + 1)
                                : "";
                pagiBtns.addComponent(new Label("btn-pagi-next",
                                String.format("<button %s class='px-2 py-1 bg-slate-800 rounded %s transition-colors'>&gt;</button>",
                                                nextAttr, nextOpacity)));

                paginationInfo.addComponent(pagiBtns);

                usersCard.addComponent(paginationInfo);
                content.addComponent(usersCard);

                // Add User Button
                Div actionsDiv = new Div("sec-actions");
                actionsDiv.setStyleClass("mt-6");
                Button addUserBtn = new Button("btn-add-user", "+ Add New User");
                addUserBtn.setStyleClass(
                                "px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-bold transition-all shadow-lg shadow-indigo-900/20");
                addUserBtn.addAttribute("onclick", "openAddUser()");
                actionsDiv.addComponent(addUserBtn);

                content.addComponent(actionsDiv);

                return Response.ok(content.render()).build();
        }

        @jakarta.ws.rs.POST
        @Path("/save")
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Produces(MediaType.TEXT_HTML)
        public Response saveUser(@jakarta.ws.rs.FormParam("username") String username,
                        @jakarta.ws.rs.FormParam("email") String email,
                        @jakarta.ws.rs.FormParam("password") String password,
                        @jakarta.ws.rs.FormParam("profile") String profile,
                        @jakarta.ws.rs.QueryParam("edit") boolean isEdit) {

                String token = getAuthToken();
                User user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(password);
                user.setProfile(profile);

                // Enforce constraints
                if (!isEdit) {
                        // Check if super-user already exists if trying to create one
                        if ("super-user".equalsIgnoreCase(profile)) {
                                List<User> existing = securityService.getUsers(token);
                                boolean hasSuper = existing.stream()
                                                .anyMatch(u -> "super-user".equalsIgnoreCase(u.getProfile()));
                                if (hasSuper) {
                                        return Response.status(400).entity("Only one super-user is allowed.").build();
                                }
                        }
                } else {
                        // Check if non-superuser is trying to edit a superuser (already checked in UI,
                        // but for safety)
                        String currentUser = headers.getCookies().containsKey("user_session")
                                        ? headers.getCookies().get("user_session").getValue()
                                        : "";
                        if ("super-user".equalsIgnoreCase(username) && !"super-user".equalsIgnoreCase(currentUser)) {
                                return Response.status(403)
                                                .entity("Forbidden: Only super-user can edit their own profile.")
                                                .build();
                        }
                }

                boolean success = securityService.saveUser(user, isEdit, token);
                if (success) {
                        return getSecurityView(0);
                } else {
                        return Response.status(500).build();
                }
        }

        @jakarta.ws.rs.POST
        @Path("/delete/{username}")
        @Produces(MediaType.TEXT_HTML)
        public Response deleteUser(@jakarta.ws.rs.PathParam("username") String username) {
                String token = getAuthToken();
                if ("super-user".equalsIgnoreCase(username)) {
                        return Response.status(400).entity("Cannot delete super-user.").build();
                }

                boolean success = securityService.deleteUser(username, token);
                if (success) {
                        return getSecurityView(0);
                } else {
                        return Response.status(500).build();
                }
        }
}
