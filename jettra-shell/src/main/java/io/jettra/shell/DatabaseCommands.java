package io.jettra.shell;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "db", description = "Database management commands", subcommands = {
        CreateDatabaseCommand.class,
        DeleteDatabaseCommand.class,
        RenameDatabaseCommand.class,
        ListDatabasesCommand.class
})
public class DatabaseCommands {
}

@Command(name = "create", description = "Create a new database")
class CreateDatabaseCommand implements Runnable {
    @Parameters(index = "0", description = "Database name")
    String name;

    @picocli.CommandLine.Option(names = { "-s",
            "--storage" }, description = "Storage style: STORE (persistent) or MEMORY (in-memory)", defaultValue = "STORE")
    String storage;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            String json = String.format("{\"name\": \"%s\", \"storage\": \"%s\"}",
                    name, storage.toUpperCase());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/db"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201)
                System.out.println("Successfully created Multi-Model database '" + name + "' [Storage: "
                        + storage.toUpperCase() + "]");
            else
                System.out.println("Error creating database: " + response.statusCode() + " " + response.body());
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }
    }
}

@Command(name = "delete", description = "Delete a database")
class DeleteDatabaseCommand implements Runnable {
    @Parameters(index = "0", description = "Database name")
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
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/db/" + name))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
                System.out.println("Database '" + name + "' deleted.");
            else
                System.out.println("Error: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }
    }
}

@Command(name = "rename", description = "Rename a database")
class RenameDatabaseCommand implements Runnable {
    @Parameters(index = "0", description = "Old name")
    String oldName;
    @Parameters(index = "1", description = "New name")
    String newName;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            String json = String.format("{\"name\": \"%s\"}", newName);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/db/" + oldName))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
                System.out.println("Database '" + oldName + "' renamed to '" + newName + "'.");
            else
                System.out.println("Error: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }
    }
}

@Command(name = "list", description = "List all databases")
class ListDatabasesCommand implements Runnable {
    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/db"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Registered Databases:");
                System.out.println(response.body());
            } else
                System.out.println("Error retrieving databases: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        }
    }
}
