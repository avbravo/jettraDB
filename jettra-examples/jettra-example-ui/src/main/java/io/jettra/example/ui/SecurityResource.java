package io.jettra.example.ui;

import io.jettra.example.ui.model.User;
import io.jettra.example.ui.service.SecurityService;
import io.jettra.ui.component.*;
import jakarta.inject.Inject;
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
    public Response getSecurityView() {
        String token = getAuthToken();
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
        int totalUsers = users.size();
        List<User> paginatedUsers = users.subList(0, Math.min(totalUsers, pageSize));

        for (User user : paginatedUsers) {
            List<String> row = new ArrayList<>();
            row.add("<div class='flex items-center gap-2'><div class='w-7 h-7 bg-indigo-500/20 text-indigo-400 rounded-full flex items-center justify-center text-[10px] font-bold'>"
                    + user.getUsername().substring(0, 1).toUpperCase() + "</div>" + user.getUsername() + "</div>");
            row.add("<span class='px-2 py-0.5 rounded-full text-[10px] bg-slate-800 text-slate-400 border border-slate-700'>"
                    + (user.getProfile() != null ? user.getProfile() : "end-user") + "</span>");
            row.add(user.getRoles() != null ? String.join(", ", user.getRoles()) : "");

            // Actions
            String actions = "<button class='text-indigo-400 hover:text-indigo-300 font-medium mr-3 transition-colors'>Edit</button>"
                    + "<button class='text-rose-400 hover:text-rose-300 font-medium transition-colors'>Delete</button>";
            row.add(actions);

            usersTable.addRow(row);
        }

        usersCard.addComponent(usersTable);

        // Add Pagination Info
        Div paginationInfo = new Div("sec-pagination");
        paginationInfo.setStyleClass("mt-4 flex justify-between items-center text-xs text-slate-500");
        paginationInfo.addComponent(
                new Label("lbl-pagi", "Showing " + paginatedUsers.size() + " of " + totalUsers + " users"));

        Div pagiBtns = new Div("pagi-btns");
        pagiBtns.setStyleClass("flex gap-2");
        pagiBtns.addComponent(new Label("btn-pagi-prev",
                "<button class='px-2 py-1 bg-slate-800 rounded opacity-50 cursor-not-allowed'>&lt;</button>"));
        pagiBtns.addComponent(new Label("btn-pagi-next",
                "<button class='px-2 py-1 bg-slate-800 rounded hover:bg-slate-700 transition-colors'>&gt;</button>"));
        paginationInfo.addComponent(pagiBtns);

        usersCard.addComponent(paginationInfo);
        content.addComponent(usersCard);

        // Add User Button
        Div actionsDiv = new Div("sec-actions");
        actionsDiv.setStyleClass("mt-6");
        Button addUserBtn = new Button("btn-add-user", "+ Add New User");
        addUserBtn.setStyleClass(
                "px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-bold transition-all shadow-lg shadow-indigo-900/20");
        addUserBtn.addAttribute("onclick", "openUserModal()");
        actionsDiv.addComponent(addUserBtn);

        content.addComponent(actionsDiv);

        return Response.ok(content.render()).build();
    }
}
