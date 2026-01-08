package io.jettra.shell;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@TopCommand
@Command(name = "jettra-shell", mixinStandardHelpOptions = true, subcommands = {
    ConnectCommand.class,
    QueryCommand.class
})
public class JettraShell {
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

@Command(name = "query", description = "Execute a query")
class QueryCommand implements Runnable {
    @Parameters(index = "0", description = "The query string")
    String query;

    @Override
    public void run() {
        System.out.println("Executing query: " + query);
        // Execution logic...
    }
}
