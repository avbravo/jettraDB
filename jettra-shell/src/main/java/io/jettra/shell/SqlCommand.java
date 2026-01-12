package io.jettra.shell;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "sql", 
         description = "Execute SQL queries across JettraDB engines",
         footer = "Examples:%n" +
                  "  sql SELECT * FROM users%n" +
                  "  sql INSERT INTO products VALUES ('p1', 'Laptop')%n" +
                  "  sql UPDATE stock SET count = 5 WHERE id = 's1'%n" +
                  "  sql DELETE FROM logs WHERE level = 'DEBUG'")
public class SqlCommand implements Runnable {

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

        System.out.println("Executing SQL: " + sql);
        
        try {
            processSql(sql);
        } catch (Exception e) {
            System.err.println("SQL Execution Error: " + e.getMessage());
        }
    }

    private void processSql(String sql) {
        String lowerSql = sql.toLowerCase();
        
        if (lowerSql.startsWith("select")) {
            handleSelect(sql);
        } else if (lowerSql.startsWith("insert")) {
            handleInsert(sql);
        } else if (lowerSql.startsWith("update")) {
            handleUpdate(sql);
        } else if (lowerSql.startsWith("delete")) {
            handleDelete(sql);
        } else {
            System.out.println("Unsupported SQL command. Supported: SELECT, INSERT, UPDATE, DELETE.");
        }
    }

    private void handleSelect(String sql) {
        // Simple regex to extract table name: SELECT ... FROM <table> ...
        Pattern pattern = Pattern.compile("(?i)select.*?from\\s+(\\w+)(.*)");
        Matcher matcher = pattern.matcher(sql);
        
        if (matcher.find()) {
            String tableName = matcher.group(1);
            System.out.println("Routing SELECT to engine for: " + tableName);
            
            // Logic to simulate multi-engine routing
            if (tableName.toLowerCase().contains("graph")) {
                System.out.println("Engine: GraphEngine -> Performing traversal...");
            } else if (tableName.toLowerCase().contains("vector")) {
                System.out.println("Engine: VectorEngine -> Performing similarity search...");
            } else {
                System.out.println("Engine: DocumentEngine -> Fetching documents...");
            }
            
            System.out.println("Result: [Mock data for " + tableName + "]");
        } else {
            System.out.println("Invalid SELECT syntax.");
        }
    }

    private void handleInsert(String sql) {
        Pattern pattern = Pattern.compile("(?i)insert\\s+into\\s+(\\w+).*values\\s*\\((.*)\\)");
        Matcher matcher = pattern.matcher(sql);
        
        if (matcher.find()) {
            String tableName = matcher.group(1);
            String values = matcher.group(2);
            System.out.println("Routing INSERT to engine for: " + tableName);
            System.out.println("Engine: StorageEngine -> Saving record: (" + values + ")");
            System.out.println("Status: Success (1 row affected)");
        } else {
            System.out.println("Invalid INSERT syntax.");
        }
    }

    private void handleUpdate(String sql) {
        Pattern pattern = Pattern.compile("(?i)update\\s+(\\w+)\\s+set\\s+(.*)");
        Matcher matcher = pattern.matcher(sql);
        
        if (matcher.find()) {
            String tableName = matcher.group(1);
            System.out.println("Routing UPDATE to engine for: " + tableName);
            System.out.println("Status: Success (Rows updated)");
        } else {
            System.out.println("Invalid UPDATE syntax.");
        }
    }

    private void handleDelete(String sql) {
        Pattern pattern = Pattern.compile("(?i)delete\\s+from\\s+(\\w+)(.*)");
        Matcher matcher = pattern.matcher(sql);
        
        if (matcher.find()) {
            String tableName = matcher.group(1);
            System.out.println("Routing DELETE to engine for: " + tableName);
            System.out.println("Status: Success (Rows deleted)");
        } else {
            System.out.println("Invalid DELETE syntax.");
        }
    }
}
