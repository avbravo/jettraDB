package io.jettra.shell;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "graph", description = "Graph database commands", subcommands = {
        AddVertexCommand.class,
        AddEdgeCommand.class,
        TraverseGraphCommand.class
})
public class GraphCommands {
}

@Command(name = "add-vertex", description = "Add a vertex to the graph")
class AddVertexCommand implements Runnable {
    @Parameters(index = "0", description = "Vertex ID")
    String id;
    
    @Parameters(index = "1", description = "Vertex Label")
    String label;
    
    @Parameters(index = "2", description = "Vertex Properties (JSON string)", defaultValue = "{}")
    String propertiesJson;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            String json = String.format("{\"id\": \"%s\", \"label\": \"%s\", \"properties\": %s}", id, label, propertiesJson);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/v1/graph/vertex"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Vertex '" + id + "' added successfully.");
            } else {
                System.out.println("Error adding vertex: " + response.statusCode() + " " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}

@Command(name = "add-edge", description = "Add an edge (relationship) between two vertices")
class AddEdgeCommand implements Runnable {
    @Parameters(index = "0", description = "From Vertex ID")
    String fromId;
    
    @Parameters(index = "1", description = "To Vertex ID")
    String toId;
    
    @Parameters(index = "2", description = "Edge Label")
    String label;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            String json = String.format("{\"fromId\": \"%s\", \"toId\": \"%s\", \"label\": \"%s\"}", fromId, toId, label);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/v1/graph/edge"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Edge from '" + fromId + "' to '" + toId + "' added successfully.");
            } else {
                System.out.println("Error adding edge: " + response.statusCode() + " " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}

@Command(name = "traverse", description = "Traverse the graph from a starting vertex")
class TraverseGraphCommand implements Runnable {
    @Parameters(index = "0", description = "Start Vertex ID")
    String startId;
    
    @Parameters(index = "1", description = "Depth of traversal", defaultValue = "3")
    int depth;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/v1/graph/traverse/" + startId + "?depth=" + depth))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Traversal results:");
                System.out.println(response.body());
            } else {
                System.out.println("Error traversing graph: " + response.statusCode() + " " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}
