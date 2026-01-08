package io.jettra.shell;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(name))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
                System.out.println("Database created.");
            else
                System.out.println("Error: " + response.statusCode());
        } catch (Exception e) {
            e.printStackTrace();
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
                System.out.println("Database deleted.");
            else
                System.out.println("Error: " + response.statusCode());
        } catch (Exception e) {
            e.printStackTrace();
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
            if (response.statusCode() == 200)
                System.out.println(response.body());
            else
                System.out.println("Error: " + response.statusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
