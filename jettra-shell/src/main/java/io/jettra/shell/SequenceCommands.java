package io.jettra.shell;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "sequence", aliases = { "seq" }, description = "Sequence management commands", subcommands = {
        CreateSequenceCommand.class,
        NextSequenceCommand.class,
        CurrentSequenceCommand.class,
        ResetSequenceCommand.class,
        DeleteSequenceCommand.class,
        ListSequencesCommand.class
})
public class SequenceCommands {
}

@Command(name = "create", description = "Create a new sequence")
class CreateSequenceCommand implements Runnable {
    @Parameters(index = "0", description = "Sequence name")
    String name;

    @Option(names = { "-s", "--start" }, description = "Starting value", defaultValue = "0")
    long start;

    @Option(names = { "-i", "--inc" }, description = "Increment value", defaultValue = "1")
    long increment;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        String db = JettraShell.currentDatabase != null ? JettraShell.currentDatabase : "_all";
        try {
            // Reusing JettraShell's (future) client or direct HTTP for now
            // For consistency with other shell commands, I'll use direct HTTP
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String json = String.format(
                    "{\"name\": \"%s\", \"database\": \"%s\", \"startValue\": %d, \"increment\": %d}",
                    name, db, start, increment);
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://" + JettraShell.pdAddress + "/api/v1/sequence"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201)
                System.out.println("Sequence '" + name + "' created in database '" + db + "'.");
            else
                System.out.println("Error: " + response.statusCode() + " " + response.body());
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}

@Command(name = "next", description = "Get next value for a sequence")
class NextSequenceCommand implements Runnable {
    @Parameters(index = "0", description = "Sequence name")
    String name;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://" + JettraShell.pdAddress + "/api/v1/sequence/" + name + "/next"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println(response.body());
            } else
                System.out.println("Error: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}

@Command(name = "current", description = "Get current value of a sequence")
class CurrentSequenceCommand implements Runnable {
    @Parameters(index = "0", description = "Sequence name")
    String name;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI
                            .create("http://" + JettraShell.pdAddress + "/api/v1/sequence/" + name + "/current"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println(response.body());
            } else
                System.out.println("Error: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}

@Command(name = "reset", description = "Reset a sequence to a new value")
class ResetSequenceCommand implements Runnable {
    @Parameters(index = "0", description = "Sequence name")
    String name;
    @Parameters(index = "1", description = "New value")
    long value;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String json = String.format("{\"newValue\": %d}", value);
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://" + JettraShell.pdAddress + "/api/v1/sequence/" + name + "/reset"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
                System.out.println("Sequence '" + name + "' reset to " + value + ".");
            else
                System.out.println("Error: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}

@Command(name = "delete", description = "Delete a sequence")
class DeleteSequenceCommand implements Runnable {
    @Parameters(index = "0", description = "Sequence name")
    String name;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://" + JettraShell.pdAddress + "/api/v1/sequence/" + name))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .DELETE()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 204 || response.statusCode() == 200)
                System.out.println("Sequence '" + name + "' deleted.");
            else
                System.out.println("Error: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}

@Command(name = "list", description = "List all sequences")
class ListSequencesCommand implements Runnable {
    @Option(names = { "-db", "--database" }, description = "Database name")
    String database;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String db = database != null ? database : JettraShell.currentDatabase;
            String url = "http://" + JettraShell.pdAddress + "/api/v1/sequence" +
                    (db != null ? "?database=" + db : "");
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Sequences:");
                System.out.println(response.body());
            } else
                System.out.println("Error: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}
