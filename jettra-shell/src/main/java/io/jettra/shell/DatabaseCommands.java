package io.jettra.shell;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "db", description = "Database management commands", subcommands = {
        CreateDatabaseCommand.class,
        DeleteDatabaseCommand.class,
        ListDatabasesCommand.class
})
public class DatabaseCommands {
}

@Command(name = "create", description = "Create a new database")
class CreateDatabaseCommand implements Runnable {
    @Parameters(index = "0", description = "Database name")
    String name;

    @picocli.CommandLine.Option(names = { "-s", "--storage" }, description = "Storage style: STORE (persistent) or MEMORY (in-memory)", defaultValue = "STORE")
    String storage;

    @picocli.CommandLine.Option(names = { "-e", "--engine" }, description = "Engine type: Document, Column, Key-Value, Graph, Vector, Object, File", defaultValue = "Multi-Model")
    String engine;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            String json = String.format("{\"name\": \"%s\", \"storage\": \"%s\", \"engine\": \"%s\"}", 
                name, storage.toUpperCase(), engine);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/internal/pd/databases"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
                System.out.println("Successfully created Multi-Model database '" + name + "' [Storage: " + storage.toUpperCase() + "]");
            else
                System.out.println("Error creating database: " + response.statusCode() + " " + response.body());
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
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
                    .uri(URI.create("http://localhost:8080/api/internal/pd/databases/" + name))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
                System.out.println("Database '" + name + "' deleted.");
            else
                System.out.println("Error: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
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
                    .uri(URI.create("http://localhost:8080/api/internal/pd/databases"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Registered Databases:");
                System.out.println(response.body());
            } else
                System.out.println("Error retrieving databases: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}
