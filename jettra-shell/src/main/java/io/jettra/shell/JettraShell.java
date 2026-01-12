package io.jettra.shell;

import io.quarkus.picocli.runtime.annotations.TopCommand;
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
                NodeCommands.class,
                QueryCommand.class,
                SqlCommand.class,
                MongoCommand.class
        })
public class JettraShell implements Runnable {
    public static String authToken;
    public static String pdAddress = "localhost:8081"; // Default to web dashboard port

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
                String prompt = (authToken == null) ? "jettra> "
                        : "jettra(" + (authToken.length() > 8 ? authToken.substring(0, 8) : authToken) + ")> ";

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

                    System.out.println("@|bold,underline Database Management|@");
                    System.out.printf("  %-35s %s%n", "db list", "List all databases");
                    System.out.printf("  %-35s %s%n", "db create <name>",
                            "Create a new database (default: Document/Store)");
                    System.out.printf("  %-35s %s%n", "  --engine <type>",
                            "  Types: Document, Graph, Key-Value, Time-Series, Vector");
                    System.out.printf("  %-35s %s%n", "  --storage <mode>",
                            "  Modes: STORE (Persistent), MEMORY (Volatile)");
                    System.out.printf("  %-35s %s%n", "db delete <name>", "Delete a database");
                    System.out.println();

                    System.out.println("@|bold,underline Cluster Management|@");
                    System.out.printf("  %-35s %s%n", "node list", "Show cluster nodes and resource usage");
                    System.out.printf("  %-35s %s%n", "node stop <id>", "Gracefully shut down a node");
                    System.out.printf("  %-35s %s%n", "node <id> stop", "Alternative syntax for shutdown");
                    System.out.println();

                    System.out.println("@|bold,underline Data & Querying|@");
                    System.out.printf("  %-35s %s%n", "sql <query>",
                            "Execute SQL queries (SELECT, INSERT, UPDATE, DELETE)");
                    System.out.printf("  %-35s %s%n", "mongo <query>",
                            "Execute MongoDB-style queries (db.col.find({...}))");
                    System.out.printf("  %-35s %s%n", "query <command>", "Execute low-level engine commands");
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
    @Parameters(index = "0", description = "The query string")
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
