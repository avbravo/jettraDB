package io.jettra.shell;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "role", description = "Role management commands", subcommands = {
        CreateRoleCommand.class,
        EditRoleCommand.class,
        DeleteRoleCommand.class,
        ListRolesCommand.class
})
public class RoleCommands {
}

@Command(name = "create", description = "Create a new role")
class CreateRoleCommand implements Runnable {
    @Parameters(index = "0", description = "Role name")
    String name;

    @Parameters(index = "1", description = "Database name (or _all)")
    String database;

    @Parameters(index = "2..*", description = "Privileges (e.g., READ, WRITE, ADMIN)", split = ",")
    List<String> privileges;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            String privilegesJson = privileges == null ? "[]"
                    : "[" + String.join(",", privileges.stream().map(p -> "\"" + p + "\"").toList()) + "]";
            String json = String.format("{\"name\": \"%s\", \"database\": \"%s\", \"privileges\": %s}",
                    name, database, privilegesJson);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/auth/roles"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201)
                System.out.println("Role '" + name + "' created successfully.");
            else
                System.out.println("Error creating role: " + response.statusCode() + " " + response.body());
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }
    }
}

@Command(name = "edit", description = "Edit an existing role")
class EditRoleCommand implements Runnable {
    @Parameters(index = "0", description = "Role name")
    String name;

    @Parameters(index = "1", description = "New Database name (or _all)")
    String database;

    @Parameters(index = "2..*", description = "New Privileges (e.g., READ, WRITE, ADMIN)", split = ",")
    List<String> privileges;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            String privilegesJson = privileges == null ? "[]"
                    : "[" + String.join(",", privileges.stream().map(p -> "\"" + p + "\"").toList()) + "]";
            String json = String.format("{\"name\": \"%s\", \"database\": \"%s\", \"privileges\": %s}",
                    name, database, privilegesJson);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/auth/roles/" + name))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
                System.out.println("Role '" + name + "' updated successfully.");
            else
                System.out.println("Error updating role: " + response.statusCode() + " " + response.body());
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }
    }
}

@Command(name = "delete", description = "Delete a role")
class DeleteRoleCommand implements Runnable {
    @Parameters(index = "0", description = "Role name")
    String name;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/auth/roles/" + name))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
                System.out.println("Role '" + name + "' deleted.");
            else
                System.out.println("Error deleting role: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }
    }
}

@Command(name = "list", description = "List all roles")
class ListRolesCommand implements Runnable {
    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/auth/roles"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Registered Roles:");
                System.out.println(response.body());
            } else
                System.out.println("Error retrieving roles: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }
    }
}
