package io.jettra.shell;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "node", description = "Node management and monitoring commands", subcommands = {
                ListNodeResourcesCommand.class,
                StopNodeCommand.class,
                NodeDetailCommand.class
})
public class NodeCommands {
}

@Command(name = "list", description = "List all nodes and their resource consumption")
class ListNodeResourcesCommand implements Runnable {
        @Override
        public void run() {
                if (JettraShell.authToken == null) {
                        System.out.println("Error: Not logged in.");
                        return;
                }
                try {
                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create("http://" + JettraShell.pdAddress + "/api/monitor/nodes"))
                                        .header("Authorization", "Bearer " + JettraShell.authToken)
                                        .GET()
                                        .build();
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        if (response.statusCode() == 200) {
                                System.out.println("Node Resources Monitoring:");
                                System.out.println(
                                                "--------------------------------------------------------------------------------------------------------------------------");
                                System.out.printf("%-15s | %-18s | %-10s | %-10s | %-8s | %-6s | %-15s / %-15s\n",
                                                "ID", "Address", "Role", "Raft Role", "Status", "CPU%", "Memory Usage",
                                                "Max Memory");
                                System.out.println(
                                                "--------------------------------------------------------------------------------------------------------------------------");

                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                java.util.List<io.jettra.driver.NodeInfo> nodes = mapper.readValue(response.body(),
                                                new com.fasterxml.jackson.databind.JavaType[] {
                                                                mapper.getTypeFactory().constructCollectionType(
                                                                                java.util.List.class,
                                                                                io.jettra.driver.NodeInfo.class)
                                                }[0]);

                                for (io.jettra.driver.NodeInfo node : nodes) {
                                        double memUsedMb = node.memoryUsage() / (1024.0 * 1024.0);
                                        double memMaxMb = node.memoryMax() / (1024.0 * 1024.0);

                                        System.out.printf(
                                                        "%-15s | %-18s | %-10s | %-10s | %-8s | %-6.1f | %-10.1f MB   / %-10.1f MB\n",
                                                        node.id(), node.address(), node.role(), node.raftRole(),
                                                        node.status(),
                                                        node.cpuUsage(), memUsedMb, memMaxMb);
                                }
                        } else
                                System.out.println("Error retrieving nodes: " + response.statusCode());
                } catch (java.io.IOException | java.lang.InterruptedException e) {
                        System.err.println("Execution failed: " + e.getMessage());
                        if (e instanceof InterruptedException)
                                Thread.currentThread().interrupt();
                } catch (Exception e) {
                        System.err.println("Unexpected failure: " + e.getMessage());
                }
        }
}

@Command(name = "stop", description = "Stop a specific node")
class StopNodeCommand implements Runnable {
        @Parameters(index = "0", description = "The ID of the node to stop.")
        private String nodeId;

        @Override
        public void run() {
                if (JettraShell.authToken == null) {
                        System.out.println("Error: Not logged in.");
                        return;
                }
                try {
                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create("http://" + JettraShell.pdAddress + "/api/monitor/nodes/"
                                                        + nodeId + "/stop"))
                                        .header("Authorization", "Bearer " + JettraShell.authToken)
                                        .POST(HttpRequest.BodyPublishers.noBody())
                                        .build();
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        if (response.statusCode() == 200) {
                                System.out.println("Stop request sent for node: " + nodeId);
                        } else {
                                System.out.println("Error ( " + response.statusCode() + "): Failed to stop node.");
                        }
                } catch (java.io.IOException | java.lang.InterruptedException e) {
                        System.err.println("Execution failed: " + e.getMessage());
                        if (e instanceof InterruptedException)
                                Thread.currentThread().interrupt();
                } catch (Exception e) {
                        System.err.println("Unexpected failure: " + e.getMessage());
                }
        }
}

@Command(name = "node-id-placeholder", description = "Node-specific commands", subcommands = {
                StopNodeCommand.class
})
class NodeDetailCommand implements Runnable {
        @Parameters(index = "0", description = "The ID of the node")
        private String nodeId;

        @Override
        public void run() {
                System.out.println("Node: " + nodeId + ". Use 'node " + nodeId + " stop' to stop it.");
        }
}
