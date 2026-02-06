package io.jettra.shell;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import java.net.URI;
import java.net.http.HttpClient;
import java.io.IOException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import jakarta.enterprise.context.ApplicationScoped;

@TopCommand
@ApplicationScoped
@Command(name = "jettra-shell", mixinStandardHelpOptions = true, description = "JettraDB Shell - Multi-model distributed database management%n"
        +
        "Supports SQL and MongoDB native queries.", subcommands = {
                ConnectCommand.class,
                LoginCommand.class,
                DatabaseCommands.class,
                CollectionCommands.class,
                ShowCommand.class,
                UseCommand.class,
                InfoCommand.class,
                NodeCommands.class,
                QueryCommand.class,
                SqlCommand.class,
                MongoCommand.class,
                SequenceCommands.class,
                UserCommands.class,
                RoleCommands.class,
                RestoreCommand.class
        })
public class JettraShell implements Runnable {
    public static String authToken;
    public static String pdAddress = "localhost:8081"; // Default to web dashboard port
    public static String currentDatabase;

    @Override
    public void run() {
        System.out.println("---------------------------------------------------------");
        System.out.println("   _____         __    __               ____   ____  ");
        System.out.println("  |__  / ___  __/ /_  / /_  _________ _/ __ \\ / __ ) ");
        System.out.println("    / / / _ \\/ __  / / __/ / ___/ __ `/ / / // __  | ");
        System.out.println(" __/ / /  __/ /_/ / / /_  / /  / /_/ / /_/ // /_/ /  ");
        System.out.println("/____/ \\___/\\__,_/  \\__/ /_/   \\__,_/_____//_____/   ");
        System.out.println("                                                     ");
        System.out.println(" JettraDB Interactive Shell v1.0.0-SNAPSHOT");
        System.out.println(" [SQL and MongoDB support enabled]");
        System.out.println(" Type 'help' for commands, 'exit' to quit.");
        System.out.println("---------------------------------------------------------");

        CommandLine cmd = new CommandLine(new JettraShell());

        try {
            org.jline.terminal.Terminal terminal = org.jline.terminal.TerminalBuilder.builder().build();
            org.jline.reader.LineReader reader = org.jline.reader.LineReaderBuilder.builder()
                    .terminal(terminal)
                    .variable(org.jline.reader.LineReader.HISTORY_FILE,
                            System.getProperty("user.home") + "/.jettra_history")
                    .build();

            while (true) {
                String dbIndicator = (currentDatabase == null) ? "" : "(" + currentDatabase + ")";
                String prompt = (authToken == null) ? "jettra" + dbIndicator + "> "
                        : "jettra" + dbIndicator + "("
                                + (authToken.length() > 8 ? authToken.substring(0, 8) : authToken) + ")> ";

                String line;
                try {
                    line = reader.readLine(prompt);
                } catch (org.jline.reader.UserInterruptException e) {
                    continue;
                } catch (org.jline.reader.EndOfFileException e) {
                    break;
                }

                line = line.trim();
                if (line.isEmpty())
                    continue;
                if ("exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                }
                if ("help".equalsIgnoreCase(line)) {
                    System.out
                            .println("==============================================================================");
                    System.out.println("                        JettraDB Shell Help");
                    System.out
                            .println("==============================================================================");

                    System.out.println("@|bold,underline Connection & Session|@");
                    System.out.printf("  %-35s %s%n", "connect <host>:<port>",
                            "Connect to a JettraDB cluster (e.g., localhost:8081)");
                    System.out.printf("  %-35s %s%n", "connect info",
                            "Show current connection details and auth status");
                    System.out.printf("  %-35s %s%n", "login <username>",
                            "Log in to the cluster (interactive password)");
                    System.out.printf("  %-35s %s%n", "exit, quit", "Exit the shell");
                    System.out.println();

                    System.out.println("@|bold,underline Navigation & Discovery|@");
                    System.out.printf("  %-35s %s%n", "show dbs", "List all available databases");
                    System.out.printf("  %-35s %s%n", "show collections", "List collections in current database");
                    System.out.printf("  %-35s %s%n", "use <database>", "Switch to target database context");
                    System.out.printf("  %-35s %s%n", "info <database>", "Show detailed settings for a database");
                    System.out.println();

                    System.out.println("@|bold,underline Database Management|@");
                    System.out.printf("  %-35s %s%n", "db list", "List all databases (legacy)");
                    System.out.printf("  %-35s %s%n", "db create <name>",
                            "Create a new database (default: STORE)");
                    System.out.printf("  %-35s %s%n", "  --storage <mode>",
                            "  Modes: STORE (Persistent), MEMORY (Volatile)");
                    System.out.printf("  %-35s %s%n", "db rename <old> <new>", "Rename an existing database");
                    System.out.printf("  %-35s %s%n", "db delete <name>", "Delete a database");
                    System.out.println();

                    System.out.println("@|bold,underline Collection Management|@");
                    System.out.printf("  %-35s %s%n", "collection add <name>",
                            "Create a new collection in current DB");
                    System.out.printf("  %-35s %s%n", "  --engine <type>",
                            "  Types: Document, Column, Graph, Vector, Object, Key-value, Geospatial, Time-Series, Files");
                    System.out.printf("  %-35s %s%n", "collection rename <old> <new>",
                            "Rename a collection in current DB");
                    System.out.printf("  %-35s %s%n", "collection delete <name>",
                            "Delete a collection from current DB");
                    System.out.println();

                    System.out.println("@|bold,underline Cluster Management|@");
                    System.out.printf("  %-35s %s%n", "node list", "Show cluster nodes and resource usage");
                    System.out.printf("  %-35s %s%n", "node stop <id>", "Gracefully shut down a node");
                    System.out.printf("  %-35s %s%n", "node <id> stop", "Alternative syntax for shutdown");
                    System.out.println();

                    System.out.println("@|bold,underline Native Language Support|@");
                    System.out.printf("  %-35s %s%n", "sql <query> [--resolve-refs]",
                            "Execute SQL (e.g., SELECT * FROM users)");
                    System.out.printf("  %-35s %s%n", "mongo <query> [--resolve-refs]",
                            "Execute MongoDB (e.g., db.users.find({}))");
                    System.out.printf("  %-35s %s%n", "query <command>", "Execute low-level engine commands");
                    System.out.println();

                    System.out.println("@|bold,underline Security & Users|@");
                    System.out.printf("  %-35s %s%n", "user create <u> <p> <e> [r1,r2]",
                            "Create user (User, Pass, Email, Roles)");
                    System.out.printf("  %-35s %s%n", "user edit <u> <p> <e> [r1,r2]",
                            "Edit user (User, Pass, Email, Roles)");
                    System.out.printf("  %-35s %s%n", "user delete <username>", "Delete a user from the system");
                    System.out.printf("  %-35s %s%n", "user list", "List all registered users");
                    System.out.printf("  %-35s %s%n", "user change-password <u> <o> <n>", "Change user password");
                    System.out.println();

                    System.out
                            .println("==============================================================================");
                    System.out.println("For detailed help on specific syntax, use: sql --help, mongo --help");

                    continue;
                }

                String[] args = line.split("\\s+");
                cmd.execute(args);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}

@Command(name = "connect", description = "Connect to a JettraDB cluster or show connection info")
class ConnectCommand implements Runnable {
    @Parameters(index = "0", description = "PD address or 'info'", defaultValue = "info")
    String arg;

    @Override
    public void run() {
        if ("info".equalsIgnoreCase(arg)) {
            System.out.println("Current Connection Info:");
            System.out.println("  PD/Web Address: " + JettraShell.pdAddress);
            System.out.println(
                    "  Auth Token: " + (JettraShell.authToken != null ? "Present (Logged In)" : "None (Logged Out)"));
        } else {
            // Sanitize: Remove http:// or https:// if user provided it
            String sanitized = arg.toLowerCase();
            if (sanitized.startsWith("http://")) {
                arg = arg.substring(7);
            } else if (sanitized.startsWith("https://")) {
                arg = arg.substring(8);
            }

            JettraShell.pdAddress = arg;
            System.out.println("Connecting to JettraDB cluster at " + JettraShell.pdAddress + "...");
            System.out.println("Successfully connected!");
        }
    }
}

@Command(name = "login", description = "Login to the cluster")
class LoginCommand implements Runnable {
    @Parameters(index = "0", description = "Username")
    String username;

    @picocli.CommandLine.Option(names = { "-p", "--password" }, description = "Password", interactive = true)
    String password;

    @Override
    public void run() {
        System.out.println("Logging in as " + username + "...");

        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username,
                    password != null ? password : "");

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://" + JettraShell.pdAddress + "/api/web-auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse token from response (simple string lookup for now, or use Jackson)
                String body = response.body();
                if (body.contains("\"token\":\"")) {
                    int start = body.indexOf("\"token\":\"") + 9;
                    int end = body.indexOf("\"", start);
                    JettraShell.authToken = body.substring(start, end);
                    System.out.println("Login successful! Token stored.");
                } else {
                    System.out.println("Login failed: Token not found in response.");
                }
            } else {
                System.out.println("Login failed. Status: " + response.statusCode());
            }
        } catch (Exception e) {
            String msg = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
            System.err.println("Login failed: " + msg);
            if (msg.contains("Connection refused") || msg.contains("ConnectException")) {
                System.err.println("Hint: Make sure JettraWeb is running at " + JettraShell.pdAddress);
                System.err.println("      Start the stack with: docker-compose up -d");
            }
        }
    }
}

@Command(name = "query", description = "Execute a query")
class QueryCommand implements Runnable {
    @picocli.CommandLine.Parameters(index = "0", description = "The query string")
    String query;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in. Please run 'login' command first.");
            return;
        }
        System.out.println("Executing query: " + query + " [Auth: " + JettraShell.authToken + "]");
    }
}

@Command(name = "show", description = "Discovery commands")
class ShowCommand implements Runnable {
    @picocli.CommandLine.Parameters(index = "0", description = "What to show: dbs, collections")
    String target;

    @Override
    public void run() {
        if ("dbs".equalsIgnoreCase(target)) {
            new ListDatabasesCommand().run();
        } else if ("collections".equalsIgnoreCase(target)) {
            if (JettraShell.currentDatabase == null) {
                System.out.println("Error: No database selected. Use 'use <database>' first.");
                return;
            }
            new ListCollectionsCommand(JettraShell.currentDatabase).run();
        } else {
            System.out.println("Unknown target: " + target + ". Supported: dbs, collections");
        }
    }
}

@Command(name = "use", description = "Switch database context")
class UseCommand implements Runnable {
    @picocli.CommandLine.Parameters(index = "0", description = "Database name")
    String name;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        System.out.println("Switching to database: " + name);
        JettraShell.currentDatabase = name;
    }
}

@Command(name = "info", description = "Show database information")
class InfoCommand implements Runnable {
    @picocli.CommandLine.Parameters(index = "0", description = "Database name")
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
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/db/" + name))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Database Information: " + name);
                System.out.println(response.body());
            } else {
                System.out.println("Error retrieving info: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}

@Command(name = "collection", description = "Collection management commands", subcommands = {
        AddCollectionCommand.class,
        RenameCollectionCommand.class,
        DeleteCollectionCommand.class
})
class CollectionCommands {
}

@Command(name = "add", description = "Add a new collection")
class AddCollectionCommand implements Runnable {
    @picocli.CommandLine.Parameters(index = "0", description = "Collection name")
    String name;

    @picocli.CommandLine.Option(names = { "-e",
            "--engine" }, description = "Engine: Document, Column, Graph, Vector, Object, Key-value, Geospatial, Time-Series, Files", defaultValue = "Document")
    String engine;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        if (JettraShell.currentDatabase == null) {
            System.out.println("Error: No database selected.");
            return;
        }
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String json = String.format("{\"engine\": \"%s\"}", engine);
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/db/" + JettraShell.currentDatabase
                            + "/collections/" + name))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Collection '" + name + "' [Engine: " + engine + "] added to database '"
                        + JettraShell.currentDatabase + "'.");
            } else {
                System.out.println("Error: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}

@Command(name = "rename", description = "Rename a collection")
class RenameCollectionCommand implements Runnable {
    @picocli.CommandLine.Parameters(index = "0", description = "Old collection name")
    String oldName;
    @picocli.CommandLine.Parameters(index = "1", description = "New collection name")
    String newName;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        if (JettraShell.currentDatabase == null) {
            System.out.println("Error: No database selected.");
            return;
        }
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/db/" + JettraShell.currentDatabase
                            + "/collections/" + oldName + "/" + newName))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .PUT(java.net.http.HttpRequest.BodyPublishers.noBody())
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Collection '" + oldName + "' renamed to '" + newName + "' in database '"
                        + JettraShell.currentDatabase + "'.");
            } else {
                System.out.println("Error: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}

@Command(name = "delete", description = "Delete a collection")
class DeleteCollectionCommand implements Runnable {
    @picocli.CommandLine.Parameters(index = "0", description = "Collection name")
    String name;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in.");
            return;
        }
        if (JettraShell.currentDatabase == null) {
            System.out.println("Error: No database selected.");
            return;
        }
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/db/" + JettraShell.currentDatabase
                            + "/collections/" + name))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .DELETE()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println(
                        "Collection '" + name + "' deleted from database '" + JettraShell.currentDatabase + "'.");
            } else {
                System.out.println("Error: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}

class ListCollectionsCommand implements Runnable {
    String dbName;

    public ListCollectionsCommand(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public void run() {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create("http://" + JettraShell.pdAddress + "/api/db/" + dbName + "/collections"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Collections in '" + dbName + "':");
                System.out.println(response.body());
            } else {
                System.out.println("Error retrieving collections: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}
