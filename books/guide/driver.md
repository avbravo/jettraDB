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
String pdAddress = "localhost:9000"; 
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
Create and manage multi-model databases, specifying the **Storage** style.

```java
// Create a new persistent Multi-Model database
client.createDatabase("sales_db", "STORE").await().indefinitely();

// Create a new in-memory Multi-Model database
client.createDatabase("social_net", "MEMORY").await().indefinitely();

// Supported Storage: "STORE", "MEMORY"

// List databases
List<String> dbs = client.listDatabases().await().indefinitely();
System.out.println("Databases: " + dbs);

// Delete a database
client.deleteDatabase("sales_db").await().indefinitely();
```

### 4. Cluster Monitoring ⭐
Monitor node resource consumption (CPU, Memory) directly via the driver.

```java
import io.jettra.driver.NodeInfo;

// List all cluster nodes
Uni<List<NodeInfo>> nodesUni = client.listNodes();
List<NodeInfo> nodes = nodesUni.await().indefinitely();

// Stop a specific node (Sends a remote stop request)
Uni<Void> stopResult = client.stopNode("jettra-store-2");
stopResult.await().indefinitely(); // Blocking wait for demo

for (NodeInfo node : nodes) {
    System.out.println("Node: " + node.id());
    System.out.println(" - Status: " + node.status());
    System.out.println(" - CPU Use: " + node.cpuUsage() + "%");
    System.out.println(" - Memory: " + (node.memoryUsage() / 1024 / 1024) + " MB");
}

// Check Connection Info
System.out.println(client.connectionInfo());
```

### 5. Data Operations

```java
// Save a document
MyObject doc = new MyObject("key1", "value");
client.save("my_collection", doc).await().indefinitely();

// Find a document
MyObject result = client.findById("my_collection", "key1").await().indefinitely();
```

The driver uses `Mutiny` (Uni/Multi) for non-blocking I/O.
