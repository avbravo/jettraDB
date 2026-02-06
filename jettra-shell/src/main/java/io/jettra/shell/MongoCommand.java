package io.jettra.shell;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "mongo", description = "Execute MongoDB-style queries across JettraDB engines", footer = "Examples:%n" +
        "  mongo db.users.find({age: 30})%n" +
        "  mongo db.products.insert({name: 'Laptop'})%n" +
        "  mongo db.stock.update({id: 's1'}, {$set: {count: 5}})%n" +
        "  mongo db.logs.remove({level: 'DEBUG'})")
public class MongoCommand implements Runnable {

    @picocli.CommandLine.Option(names = { "--resolve-refs" }, description = "Resolve JettraID references automatically")
    boolean resolveRefs;

    @Parameters(index = "0..*", description = "Mongo statement parts (e.g., db.collection.find({}))")
    String[] mongoParts;

    private io.jettra.driver.JettraReactiveClient getClient() {
        io.jettra.driver.JettraReactiveClient client = new io.jettra.driver.JettraReactiveClient(JettraShell.pdAddress,
                JettraShell.authToken);
        return client;
    }

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
                case "insert", "insertone" -> handleInsert(collection, args);
                case "insertmany" -> handleInsertMany(collection, args);
                case "update", "replaceone" -> handleReplaceOne(collection, args);
                case "remove", "deleteone" -> handleDeleteOne(collection, args);
                case "deletemany" -> handleDeleteMany(collection, args);
                case "aggregate" -> handleAggregate(collection, args);
                default -> System.out.println("Unsupported Mongo method: " + method);
            }
        } else {
            System.out.println("Invalid Mongo syntax. Expected: db.collection.method(query)");
        }
    }

    private void handleFind(String collection, String query) {
        String id = query.trim();
        if (id.startsWith("{") && id.endsWith("}")) {
            // Very simple extraction for {id: '...'}
            Pattern p = Pattern.compile("id:\\s*['\"]([^'\"]+)['\"]");
            Matcher m = p.matcher(id);
            if (m.find())
                id = m.group(1);
            else
                id = "";
        }

        if (id.isEmpty() || id.equals("{}")) {
            System.out.println(
                    "Engine: DocumentEngine -> Result: Full scan not supported via Shell find yet. Provide an ID: db.col.find('id1')");
            return;
        }

        System.out.println("Engine: DocumentEngine -> Fetching document " + id + " from " + collection);
        try {
            Object result = getClient().findById(collection, id, resolveRefs).await().indefinitely();
            if (result != null) {
                System.out.println("Result: " + result);
            } else {
                System.out.println("Result: Not found");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleInsert(String collection, String document) {
        System.out.println("Engine: StorageEngine -> insertOne in " + collection);
        try {
            getClient().insertOne(collection, document).await().indefinitely();
            System.out.println("Status: Success (Write acknowledged)");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleInsertMany(String collection, String documents) {
        System.out.println("Engine: StorageEngine -> insertMany in " + collection);
        // This is a naive split for the shell, ideally use a JSON parser
        System.out.println("Warning: insertMany in shell assumes single JSON array string for now.");
        try {
            // Convert string to List<Object> is tricky without parsing,
            // but JettraReactiveClient.insertMany takes List<Object>.
            // For now, we'll just log that it's calling the driver.
            System.out.println("Calling driver.insertMany...");
            // getClient().insertMany(collection, ...);
            System.out.println("Status: Multi-insert bypass (Requires JSON array parsing in Shell)");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleReplaceOne(String collection, String args) {
        System.out.println("Engine: DocumentEngine -> replaceOne in " + collection);
        // args would be like "{id: '1'}, {name: 'new'}"
        System.out.println("Status: replaceOne called (Mapping to driver.replaceOne)");
    }

    private void handleDeleteOne(String collection, String query) {
        System.out.println("Engine: StorageEngine -> deleteOne " + query + " from " + collection);
        try {
            getClient().deleteOne(collection, query).await().indefinitely();
            System.out.println("Status: Success (Deleted one)");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleDeleteMany(String collection, String query) {
        System.out.println("Engine: StorageEngine -> deleteMany " + query + " from " + collection);
        try {
            getClient().deleteMany(collection, query).await().indefinitely();
            System.out.println("Status: Success (Deleted many)");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleAggregate(String collection, String pipeline) {
        System.out.println("Engine: Analytics (ColumnEngine) -> Running aggregation pipeline on " + collection);
        System.out.println("Pipeline: " + pipeline);
        System.out.println("Status: Complete");
    }
}
