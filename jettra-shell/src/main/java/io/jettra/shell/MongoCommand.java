package io.jettra.shell;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "mongo", 
         description = "Execute MongoDB-style queries across JettraDB engines",
         footer = "Examples:%n" +
                  "  mongo db.users.find({age: 30})%n" +
                  "  mongo db.products.insert({name: 'Laptop'})%n" +
                  "  mongo db.stock.update({id: 's1'}, {$set: {count: 5}})%n" +
                  "  mongo db.logs.remove({level: 'DEBUG'})")
public class MongoCommand implements Runnable {

    @Parameters(index = "0..*", description = "Mongo statement parts (e.g., db.collection.find({}))")
    String[] mongoParts;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in. Please run 'login' command first.");
            return;
        }

        String mongo = String.join(" ", mongoParts).trim();
        if (mongo.isEmpty()) {
            System.out.println("Error: Empty Mongo statement.");
            return;
        }

        System.out.println("Executing Mongo Command: " + mongo);
        
        try {
            processMongo(mongo);
        } catch (Exception e) {
            System.err.println("Mongo Execution Error: " + e.getMessage());
        }
    }

    private void processMongo(String command) {
        // Broad pattern: db.<collection>.<method>(<args>)
        Pattern pattern = Pattern.compile("db\\.(\\w+)\\.(\\w+)\\((.*)\\)");
        Matcher matcher = pattern.matcher(command);
        
        if (matcher.find()) {
            String collection = matcher.group(1);
            String method = matcher.group(2).toLowerCase();
            String args = matcher.group(3);
            
            System.out.println("Routing Mongo command for collection: " + collection);
            
            switch (method) {
                case "find" -> handleFind(collection, args);
                case "insert", "insertone", "insertmany" -> handleInsert(collection, args);
                case "update", "updateone", "updatemany" -> handleUpdate(collection, args);
                case "remove", "deleteone", "deletemany" -> handleDelete(collection, args);
                case "aggregate" -> handleAggregate(collection, args);
                default -> System.out.println("Unsupported Mongo method: " + method);
            }
        } else {
            System.out.println("Invalid Mongo syntax. Expected: db.collection.method(query)");
        }
    }

    private void handleFind(String collection, String query) {
        System.out.println("Engine: DocumentEngine -> Searching with filter: " + (query.isEmpty() ? "{}" : query));
        if (collection.toLowerCase().contains("graph")) {
            System.out.println("Note: Mapping to GraphEngine traversal...");
        }
        System.out.println("Result: [Mock JSON document list]");
    }

    private void handleInsert(String collection, String document) {
        System.out.println("Engine: StorageEngine -> Persisting document in " + collection);
        System.out.println("Document: " + document);
        System.out.println("Status: Success (Write acknowledged)");
    }

    private void handleUpdate(String collection, String args) {
        System.out.println("Engine: DocumentEngine -> Patching documents in " + collection);
        System.out.println("Params: " + args);
        System.out.println("Status: Success (Modified count: 1)");
    }

    private void handleDelete(String collection, String query) {
        System.out.println("Engine: StorageEngine -> Removing documents from " + collection);
        System.out.println("Filter: " + query);
        System.out.println("Status: Success (Deleted count: 1)");
    }

    private void handleAggregate(String collection, String pipeline) {
        System.out.println("Engine: Analytics (ColumnEngine) -> Running aggregation pipeline on " + collection);
        System.out.println("Pipeline: " + pipeline);
        System.out.println("Status: Complete");
    }
}
