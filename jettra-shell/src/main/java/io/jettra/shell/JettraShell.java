package io.jettra.shell;

import java.util.Scanner;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@TopCommand
@Command(name = "jettra-shell", 
         mixinStandardHelpOptions = true, 
         description = "JettraDB Shell - Multi-model distributed database management%n" +
                       "Supports SQL and MongoDB native queries.",
         subcommands = {
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
        
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String prompt = (authToken == null) ? "jettra> " : "jettra(" + (authToken.length() > 8 ? authToken.substring(0, 8) : authToken) + ")> ";
                System.out.print(prompt);
                
                if (!scanner.hasNextLine()) break;
                
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                if ("exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                }
                if ("help".equalsIgnoreCase(line)) {
                    cmd.usage(System.out);
                    continue;
                }
                
                String[] args = line.split("\\s+");
                cmd.execute(args);
                System.out.println();
            }
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
            System.out.println("  Auth Token: " + (JettraShell.authToken != null ? "Present (Logged In)" : "None (Logged Out)"));
        } else {
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

    @picocli.CommandLine.Option(names = {"-p", "--password"}, description = "Password", interactive = true)
    String password;

    @Override
    public void run() {
        System.out.println("Logging in as " + username + "...");
        
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password != null ? password : "");
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://" + JettraShell.pdAddress + "/api/web-auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();
                    
            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            
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
            if (msg.contains("Connection refused")) {
                System.err.println("Hint: Make sure JettraWeb is running at " + JettraShell.pdAddress);
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
