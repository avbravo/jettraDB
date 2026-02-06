package io.jettra.example;

import io.jettra.driver.JettraReactiveClient;

public class SqlExample {

    public static void main(String[] args) {
        String pdAddress = "localhost:8081";
        // Ensure you have a valid token (login first via curl or Main.java code)
        String token = System.getenv("JETTRA_TOKEN"); 
        
        if (token == null) {
            System.err.println("Please set JETTRA_TOKEN env variable.");
            System.exit(1);
        }

        JettraReactiveClient client = new JettraReactiveClient(pdAddress, token);

        System.out.println("=== SQL Example ===");

        // 1. Create Data
        System.out.println("--> Inserting data via SQL...");
        client.executeSql("INSERT INTO sales_db.products VALUES ('p1', 'Laptop', 1500)")
              .await().indefinitely();
        client.executeSql("INSERT INTO sales_db.products VALUES ('p2', 'Mouse', 25)")
              .await().indefinitely();

        // 2. Query Data
        System.out.println("--> Selecting all products...");
        String inventory = client.executeSql("SELECT * FROM sales_db.products").await().indefinitely();
        System.out.println(inventory);

        // 3. Update Data
        System.out.println("--> Updating product price...");
        client.executeSql("UPDATE sales_db.products SET price = 1400 WHERE id = 'p1'")
              .await().indefinitely();

        // 4. Query with Reference Resolution
        // Assuming products might have links, just demonstrating the flag usage
        System.out.println("--> Selecting with resolveRefs=true...");
        String resolved = client.executeSql("SELECT * FROM sales_db.products", true).await().indefinitely();
        System.out.println(resolved);

         // 5. Delete Data
        System.out.println("--> Deleting product...");
        client.executeSql("DELETE FROM sales_db.products WHERE id = 'p2'")
              .await().indefinitely();
              
        System.out.println("Querying final state...");
        System.out.println(client.executeSql("SELECT * FROM sales_db.products").await().indefinitely());
    }
}
