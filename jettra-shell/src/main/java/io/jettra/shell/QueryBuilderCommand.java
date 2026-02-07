package io.jettra.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import picocli.CommandLine.Command;

@Command(name = "query-builder", description = "Interactive tool to build queries")
public class QueryBuilderCommand implements Runnable {

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("-------------------------------------");
        System.out.println("      JettraDB Query Builder        ");
        System.out.println("-------------------------------------");

        System.out.print("Select Engine (1: Document/Mongo, 2: SQL): ");
        String engineChoice = scanner.nextLine().trim();

        if ("2".equals(engineChoice)) {
            buildSqlQuery(scanner);
        } else {
            buildMongoQuery(scanner);
        }
    }

    private void buildMongoQuery(Scanner scanner) {
        System.out.print("Enter Collection Name: ");
        String collection = scanner.nextLine().trim();

        StringBuilder query = new StringBuilder("{");
        List<String> conditions = new ArrayList<>();

        while (true) {
            System.out.print("Field Name: ");
            String field = scanner.nextLine().trim();
            if (field.isEmpty()) break;

            System.out.print("Operator (=, >, <, !=): ");
            String operator = scanner.nextLine().trim();

            System.out.print("Value: ");
            String value = scanner.nextLine().trim();

            // Simple mapping
            if ("=".equals(operator)) {
                conditions.add("\"" + field + "\": \"" + value + "\"");
            } else if (">".equals(operator)) {
                conditions.add("\"" + field + "\": {\"$gt\": \"" + value + "\"}");
            } else if ("<".equals(operator)) {
                conditions.add("\"" + field + "\": {\"$lt\": \"" + value + "\"}");
            } else if ("!=".equals(operator)) {
                conditions.add("\"" + field + "\": {\"$ne\": \"" + value + "\"}");
            }

            System.out.print("Add another condition? (y/n): ");
            String cont = scanner.nextLine().trim();
            if (!"y".equalsIgnoreCase(cont)) break;
        }

        query.append(String.join(", ", conditions));
        query.append("}");

        System.out.println("\nGenerated Mongo Query:");
        System.out.println("db." + collection + ".find(" + query.toString() + ")");
        
        System.out.print("Execute this query now? (y/n): ");
        if ("y".equalsIgnoreCase(scanner.nextLine().trim())) {
             MongoCommand cmd = new MongoCommand();
             cmd.mongoParts = new String[] { "db." + collection + ".find(" + query.toString() + ")" };
             cmd.run();
        }
    }

    private void buildSqlQuery(Scanner scanner) {
        System.out.print("Enter Table/Collection Name: ");
        String table = scanner.nextLine().trim();

        StringBuilder sql = new StringBuilder("SELECT * FROM " + table);
        List<String> whereClauses = new ArrayList<>();

        while (true) {
            System.out.print("Column Name (or enter to finish): ");
            String col = scanner.nextLine().trim();
            if (col.isEmpty()) break;

            System.out.print("Operator (=, >, <, LIKE): ");
            String op = scanner.nextLine().trim();

            System.out.print("Value: ");
            String val = scanner.nextLine().trim();

            boolean isText = !val.matches("-?\\d+(\\.\\d+)?");
            String formattedVal = isText ? "'" + val + "'" : val;

            whereClauses.add(col + " " + op + " " + formattedVal);

            System.out.print("Add another condition? (y/n): ");
            if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) break;
        }

        if (!whereClauses.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereClauses));
        }

        System.out.println("\nGenerated SQL Query:");
        System.out.println(sql.toString());
        
        System.out.print("Execute this query now? (y/n): ");
        if ("y".equalsIgnoreCase(scanner.nextLine().trim())) {
             // Access SQL command logic directly or via client
             if (JettraShell.authToken == null) {
                 System.out.println("Error: Not logged in.");
                 return;
             }
             try {
                // Quick hack: invoke JettraReactiveClient directly since SqlCommand is not easily exposed/instantiable with args
                // Or better, just print instructions
                System.out.println("Executing SQL...");
                io.jettra.driver.JettraReactiveClient client = new io.jettra.driver.JettraReactiveClient(JettraShell.pdAddress, JettraShell.authToken);
                String result = client.executeSql(sql.toString()).await().indefinitely();
                System.out.println("Result: " + result);
             } catch(Exception e) {
                 System.out.println("Error: " + e.getMessage());
             }
        }
    }
}
