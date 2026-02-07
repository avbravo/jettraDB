package io.jettra.shell;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "mongo", description = "Execute MongoDB-style queries across JettraDB engines", footer = "Examples:%n" +
        "  mongo db.users.find({age: 30})%n" +
        "  mongo db.products.insert({name: 'Laptop'})%n" +
        "  mongo db.stock.update({id: 's1'}, {$set: {count: 5}})%n" +
        "  mongo db.logs.remove({level: 'DEBUG'})")
public class MongoCommand implements Runnable {
    
    private final ObjectMapper mapper = new ObjectMapper();

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
                case "replacemany" -> handleReplaceMany(collection, args);
                case "remove", "deleteone" -> handleDeleteOne(collection, args);
                case "deletemany" -> handleDeleteMany(collection, args);
                case "aggregate" -> handleAggregate(collection, args);
                case "createindex", "ensureindex" -> handleCreateIndex(collection, args);
                case "dropindex" -> handleDropIndex(collection, args);
                case "getindexes" -> handleGetIndexes(collection, args);
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
        try {
            List<Object> docs = mapper.readValue(documents, List.class);
            getClient().insertMany(collection, docs).await().indefinitely();
            System.out.println("Status: Success (Inserted " + docs.size() + " documents)");
        } catch (Exception e) {
            System.err.println("Error parsing or inserting documents: " + e.getMessage());
        }
    }

    private void handleReplaceOne(String collection, String args) {
         // Expects: query, replacement
         // args string parsing is tricky without a proper parser.
         // We'll assume args are valid JSONs separated by comma?? 
         // Shell arguments handling via regex is limited.
         System.out.println("replaceOne is limited in shell regex mode. Use Java Driver for complex objects.");
         // Try to split by "}, {"
         int splitUserInfo = args.indexOf("}, {");
         if (splitUserInfo > 0) {
             String query = args.substring(0, splitUserInfo + 1).trim();
             String replacement = args.substring(splitUserInfo + 3).trim();
             try {
                Object doc = mapper.readValue(replacement, Object.class);
                getClient().replaceOne(collection, query, doc).await().indefinitely();
                System.out.println("Status: Success (Replaced)");
             } catch(Exception e) {
                 e.printStackTrace();
             }
         } else {
             System.out.println("Invalid syntax for replaceOne. Expected: {query}, {replacement}");
         }
    }

    private void handleReplaceMany(String collection, String args) {
         System.out.println("replaceMany not fully implemented in shell (requires cursor iteration).");
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
        // Try to map to SQL for basic aggregations
        // pipeline: [{"$group": {"_id": null, "total": {"$sum": "$amount"}}}]
        try {
             if (pipeline.contains("$sum") || pipeline.contains("$avg")) {
                 String sql = "SELECT * FROM " + collection; // Fallback
                 if (pipeline.contains("$sum")) {
                     // Extract field?
                     // Verify simple pattern: "$sum": "$field"
                     Pattern p = Pattern.compile("\"\\$sum\"\\s*:\\s*\"\\$(\\w+)\"");
                     Matcher m = p.matcher(pipeline);
                     if (m.find()) {
                         String field = m.group(1);
                         sql = "SELECT SUM(" + field + ") FROM " + collection;
                     }
                 } else if (pipeline.contains("$avg")) {
                     Pattern p = Pattern.compile("\"\\$avg\"\\s*:\\s*\"\\$(\\w+)\"");
                     Matcher m = p.matcher(pipeline);
                     if (m.find()) {
                         String field = m.group(1);
                         sql = "SELECT AVG(" + field + ") FROM " + collection;
                     }
                 }
                 System.out.println("Translated Mongo Aggregation to SQL: " + sql);
                 String result = getClient().executeSql(sql).await().indefinitely();
                 System.out.println("Result: " + result);
             } else {
                 System.out.println("Complex pipeline not supported in shell translation yet: " + pipeline);
             }
        } catch (Exception e) {
            System.err.println("Aggregation failed: " + e.getMessage());
        }
    }
    
    private void handleCreateIndex(String collection, String args) {
        // args: {field: 1}, {unique: true} ... we simplify to field name and type
        // Parsing "{ name: 1 }" -> field="name", type="ASC"
        // For shell demo, simple parsing:
        try {
            // naive parse
            String field = "unknown";
            String type = "ASC";
            if (args.contains(":")) {
                String[] parts = args.replace("{","").replace("}","").split(":");
                field = parts[0].trim().replace("\"", "").replace("'", "");
            }
            getClient().createIndex(JettraShell.currentDatabase, collection, field, type).await().indefinitely();
            System.out.println("Index created on " + field);
        } catch(Exception e) {
             System.out.println("Error creating index: " + e.getMessage());
        }
    }

    private void handleDropIndex(String collection, String args) {
         // args: "indexName"
         String indexName = args.replace("\"", "").replace("'", "").trim();
         try {
            getClient().deleteIndex(JettraShell.currentDatabase, collection, indexName).await().indefinitely();
            System.out.println("Index dropped: " + indexName);
         } catch(Exception e) {
              System.out.println("Error dropping index: " + e.getMessage());
         }
    }

    private void handleGetIndexes(String collection, String args) {
        try {
            List<String> indexes = getClient().listIndexes(JettraShell.currentDatabase, collection).await().indefinitely();
            System.out.println("Indexes on " + collection + ": " + indexes);
        } catch (Exception e) {
             System.out.println("Error listing indexes: " + e.getMessage());
        }
    }
}
