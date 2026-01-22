package io.jettra.shell;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "sql", description = "Execute SQL queries across JettraDB engines", footer = "Examples:%n" +
        "  sql SELECT * FROM users%n" +
        "  sql INSERT INTO products VALUES ('p1', 'Laptop')%n" +
        "  sql UPDATE stock SET count = 5 WHERE id = 's1'%n" +
        "  sql DELETE FROM logs WHERE level = 'DEBUG'")
public class SqlCommand implements Runnable {

    @picocli.CommandLine.Option(names = { "--resolve-refs" }, description = "Resolve JettraID references automatically")
    boolean resolveRefs;

    @Parameters(index = "0..*", description = "SQL statement parts")
    String[] sqlParts;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in. Please run 'login' command first.");
            return;
        }

        String sql = String.join(" ", sqlParts).trim();
        if (sql.isEmpty()) {
            System.out.println("Error: Empty SQL statement.");
            return;
        }

        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String json = String.format("{\"sql\": \"%s\", \"resolveRefs\": %b}", sql.replace("\"", "\\\""),
                    resolveRefs);
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://" + JettraShell.pdAddress + "/api/v1/sql"))
                    .header("Authorization", "Bearer " + JettraShell.authToken)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Result:\n" + response.body());
            } else {
                System.out.println("SQL Execution Error: " + response.statusCode() + " " + response.body());
            }
        } catch (Exception e) {
            System.err.println("SQL Execution Error: " + e.getMessage());
        }
    }
}
