package io.jettra.shell;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "user", description = "User management commands", subcommands = {
        CreateUserCommand.class,
        EditUserCommand.class,
        DeleteUserCommand.class,
        ListUsersCommand.class,
        ChangePasswordCommand.class
})
public class UserCommands {
}

@Command(name = "create", description = "Create a new user")
class CreateUserCommand implements Runnable {
    @Parameters(index = "0", description = "Username")
    String username;

    @Parameters(index = "1", description = "Password")
    String password;

    @Parameters(index = "2", description = "Email")
    String email;

    @Parameters(index = "3..*", description = "Roles (comma-separated or multiple arguments)", split = ",")
    List<String> roles;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            String rolesJson = roles == null ? "[]"
                    : "[" + String.join(",", roles.stream().map(r -> "\"" + r + "\"").toList()) + "]";
            String json = String.format(
                    "{\"username\": \"%s\", \"password\": \"%s\", \"email\": \"%s\", \"roles\": %s, \"forcePasswordChange\": false}",
                    username, password, email, rolesJson);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/auth/users"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201)
                System.out.println("User '" + username + "' created successfully.");
            else
                System.out.println("Error creating user: " + response.statusCode() + " " + response.body());
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }
    }
}

@Command(name = "edit", description = "Edit an existing user")
class EditUserCommand implements Runnable {
    @Parameters(index = "0", description = "Username")
    String username;

    @Parameters(index = "1", description = "New Password (optional, blank to keep current)")
    String password = "";

    @Parameters(index = "2", description = "New Email")
    String email = "";

    @Parameters(index = "3..*", description = "New Roles (comma-separated or multiple arguments)", split = ",")
    List<String> roles;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            String rolesJson = roles == null ? "[]"
                    : "[" + String.join(",", roles.stream().map(r -> "\"" + r + "\"").toList()) + "]";
            String json = String.format(
                    "{\"username\": \"%s\", \"password\": \"%s\", \"email\": \"%s\", \"roles\": %s, \"forcePasswordChange\": false}",
                    username, password, email, rolesJson);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/auth/users/" + username))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
                System.out.println("User '" + username + "' updated successfully.");
            else
                System.out.println("Error updating user: " + response.statusCode() + " " + response.body());
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }
    }
}

@Command(name = "delete", description = "Delete a user")
class DeleteUserCommand implements Runnable {
    @Parameters(index = "0", description = "Username")
    String username;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/auth/users/" + username))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
                System.out.println("User '" + username + "' deleted.");
            else
                System.out.println("Error deleting user: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }
    }
}

@Command(name = "list", description = "List all users")
class ListUsersCommand implements Runnable {
    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/auth/users"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Registered Users:");
                System.out.println(response.body());
            } else
                System.out.println("Error retrieving users: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }
    }
}

@Command(name = "change-password", description = "Change current user password")
class ChangePasswordCommand implements Runnable {
    @Parameters(index = "0", description = "Username")
    String username;

    @Parameters(index = "1", description = "Old Password")
    String oldPassword;

    @Parameters(index = "2", description = "New Password")
    String newPassword;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            String json = String.format(
                    "{\"username\": \"%s\", \"oldPassword\": \"%s\", \"newPassword\": \"%s\"}",
                    username, oldPassword, newPassword);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/auth/change-password"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
                System.out.println("Password changed successfully for user '" + username + "'.");
            else
                System.out.println("Error changing password: " + response.statusCode() + " " + response.body());
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }
    }
}
