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
        if (users.isEmpty()) {
            // Mock if empty or failed? Or just show empty.
            // Let's assume empty list is valid.
        }

        for (User user : users) {
            List<String> row = new ArrayList<>();
            row.add(user.getUsername());
            row.add(user.getProfile() != null ? user.getProfile() : "N/A");
            row.add(user.getRoles() != null ? String.join(", ", user.getRoles()) : "");

            // Actions
            String actions = "<button class='text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 mr-2'>Edit</button>"
                    +
                    "<button class='text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300'>Delete</button>";
            row.add(actions);

            usersTable.addRow(row);
        }

        usersCard.addComponent(usersTable);
        content.addComponent(usersCard);

        // Add User Button (Mock)
        Div actionsDiv = new Div("sec-actions");
        actionsDiv.setStyleClass("mt-4");
        Button addUserBtn = new Button("btn-add-user", "Add User");
        // Reuse blue button style from Button default or ensure consistency
        // Jettra-UI Button default might be generic, let's enforce Flowbite style
        addUserBtn.setStyleClass(
                "text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 mr-2 mb-2 dark:bg-blue-600 dark:hover:bg-blue-700 focus:outline-none dark:focus:ring-blue-800");
        addUserBtn.addAttribute("onclick", "alert('Add User modal not implemented yet')");
        actionsDiv.addComponent(addUserBtn);

        content.addComponent(actionsDiv);

        return Response.ok(content.render()).build();
    }
}
