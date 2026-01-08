package io.jettra.shell;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@TopCommand
@Command(name = "jettra-shell", mixinStandardHelpOptions = true, subcommands = {
        ConnectCommand.class,
        LoginCommand.class,
        DatabaseCommands.class,
        QueryCommand.class
})
public class JettraShell {
    public static String authToken;
}

@Command(name = "connect", description = "Connect to a JettraDB cluster")
class ConnectCommand implements Runnable {
    @Parameters(index = "0", description = "PD address")
    String address;

    @Override
    public void run() {
        System.out.println("Connecting to JettraDB cluster at " + address + "...");
        System.out.println("Successfully connected!");
    }
}

@Command(name = "login", description = "Login to the cluster")
class LoginCommand implements Runnable {
    @Parameters(index = "0", description = "Username")
    String username;

    @Parameters(index = "1", description = "Password", interactive = true)
    String password;

    @Override
    public void run() {
        System.out.println("Logging in as " + username + "...");
        // Implement actual HTTP call to PD here (simplified for now)
        // In a real scenario, use HttpClient to POST /api/auth/login
        if ("admin".equals(username) && "adminadmin".equals(password)) {
            JettraShell.authToken = "dummy-token-for-demo";
            System.out.println("Login successful! Token stored.");
        } else {
            System.out.println("Login failed.");
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
        // Execution logic would send "Authorization: Bearer " + token
    }
}
