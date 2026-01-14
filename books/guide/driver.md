# JettraDB Java Driver Guide

The JettraDB Java Driver allows seamless integration with JettraDB clusters using reactive programming principles (Mutiny).

## Maven Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.jettra</groupId>
    <artifactId>jettra-driver-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Usage

### 1. Initialization
Initialize the client with the Placement Driver (PD) address and your Authentication Token.

```java
import io.jettra.driver.JettraReactiveClient;

// ...
String pdAddress = "localhost:8081"; 
String authToken = "eyJh... (your JWT token)"; // Obtain via Auth API

JettraReactiveClient client = new JettraReactiveClient(pdAddress, authToken);
```

### 2. Authentication Flow
To obtain a token programmatically, you can use the Auth API:

```java
// Example: Obtaining token via curl or dedicated AuthClient (if available)
// Use this token for all JettraReactiveClient operations.
```

### 3. Database Management
Create and manage multi-model databases, specifying the **Engine** and **Storage** style.

```java
// Create a new persistent Multi-Model database
client.createDatabase("sales_db", "STORE").await().indefinitely();

// Create a new in-memory Multi-Model database 
client.createDatabase("social_net", "MEMORY").await().indefinitely();

// Rename a database
client.renameDatabase("sales_db", "sales_v2").await().indefinitely();

// List databases
List<String> dbs = client.listDatabases().await().indefinitely();
System.out.println("Databases: " + dbs);

// Delete a database
client.deleteDatabase("sales_v2").await().indefinitely();

// Get detailed database metadata (returns JSON String)
String info = client.getDatabaseInfo("sales_v2").await().indefinitely();
System.out.println("Metadata: " + info);
```

### 4. Collection Management
Manage collections within a database.

```java
// List collections in a database
List<String> collections = client.listCollections("retail_db").await().indefinitely();

// Add a new collection (Engine: Document)
client.addCollection("retail_db", "orders", "Document").await().indefinitely();

// Add a new Graph collection
client.addCollection("retail_db", "friends", "Graph").await().indefinitely();

// Add a new Vector collection
client.addCollection("retail_db", "embeddings", "Vector").await().indefinitely();

// Rename a collection
client.renameCollection("retail_db", "orders", "customer_orders").await().indefinitely();

// Remove a collection
client.removeCollection("retail_db", "customer_orders").await().indefinitely();
```

### 5. Cluster Monitoring ⭐
Monitor node resource consumption (CPU, Memory) directly via the driver.

```java
import io.jettra.driver.NodeInfo;

// List all cluster nodes
Uni<List<NodeInfo>> nodesUni = client.listNodes();
List<NodeInfo> nodes = nodesUni.await().indefinitely();

// Stop a specific node (Sends a remote stop request via PD)
// NOTE: Only allowed for users with 'admin' role.
Uni<Void> stopResult = client.stopNode("jettra-store-2");
stopResult.await().indefinitely(); // Blocking wait for demo

// Alternatively, any node can be stopped directly via HTTP POST to /stop if the address is known.
```

for (NodeInfo node : nodes) {
    System.out.println("Node: " + node.id());
    System.out.println(" - Status: " + node.status());
    System.out.println(" - CPU Use: " + node.cpuUsage() + "%");
    System.out.println(" - Memory: " + (node.memoryUsage() / 1024 / 1024) + " MB");
}

// Check Connection Info
System.out.println(client.connectionInfo());
```

### 6. User & Role Management ⭐
Manage cluster access control. 

> [!IMPORTANT]
> These methods are restricted to users with the **admin** role.

```java
// Create a role for a specific database
client.createRole("reader_db1", "db1", Set.of("READ"))
      .await().indefinitely();

// Create a new user with specific roles
client.createUser("bob", "password123", Set.of("reader_db1", "admin_all"))
      .await().indefinitely();

// Update an existing user (Edit roles/password)
client.updateUser("bob", "newpassword456", Set.of("writer-reader_all"))
      .await().indefinitely();

// List all users
List<String> usernames = client.listUsers().await().indefinitely();

// Delete a user
client.deleteUser("bob").await().indefinitely();
```

### 7. Data Operations

```java
// Save a document
MyObject doc = new MyObject("key1", "value");
client.save("my_collection", doc).await().indefinitely();

// Find a document
MyObject result = client.findById("my_collection", "key1").await().indefinitely();
```

The driver uses `Mutiny` (Uni/Multi) for non-blocking I/O.

## Ejemplo Completo: Base de Datos y Colección (Document)
Este ejemplo muestra el flujo completo para crear una base de datos persistente y una colección usando el motor **Document**.

```java
import io.jettra.driver.JettraReactiveClient;
import java.util.List;

public class DocumentExample {
    public static void main(String[] args) {
        String pdAddress = "localhost:8081";
        String token = "YOUR_TOKEN";
        
        JettraReactiveClient client = new JettraReactiveClient(pdAddress, token);

        // 1. Crear una base de datos persistente Multi-Modelo
        client.createDatabase("mi_base_datos", "STORE")
              .await().indefinitely();

        // 2. Añadir una colección con engine = "Document"
        client.addCollection("mi_base_datos", "usuarios", "Document")
              .await().indefinitely();

        System.out.println("Base de datos y Colección (Document) creadas con éxito!");
    }
}
```
