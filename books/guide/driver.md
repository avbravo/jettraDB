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
Create and manage databases, specifying the **Engine** (model) and the **Storage** style.

```java
// Create a new persistent Document database
client.createDatabase("sales_db", "STORE", "Document").await().indefinitely();

// Create a new in-memory Graph database
client.createDatabase("social_net", "MEMORY", "Graph").await().indefinitely();

// Supported Engines: "Document", "Column", "Key-Value", "Graph", "Vector", "Object", "File"
// Supported Storage: "STORE", "MEMORY"

// List databases
List<String> dbs = client.listDatabases().await().indefinitely();
System.out.println("Databases: " + dbs);

// Delete a database
client.deleteDatabase("sales_db").await().indefinitely();
```

### 4. Data Operations

```java
// Save a document
MyObject doc = new MyObject("key1", "value");
client.save("my_collection", doc).await().indefinitely();

// Find a document
MyObject result = client.findById("my_collection", "key1").await().indefinitely();
```

The driver uses `Mutiny` (Uni/Multi) for non-blocking I/O.
