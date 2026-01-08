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
String pdAddress = "localhost:9000"; // PD address
String authToken = "eyJh... (your JWT token)"; // Obtain via Auth API

JettraReactiveClient client = new JettraReactiveClient(pdAddress, authToken);
```

### 2. Database Management

```java
// Create a new database
client.createDatabase("analytics_db").await().indefinitely();

// List databases
Set<String> dbs = client.listDatabases().await().indefinitely();
System.out.println("Databases: " + dbs);

// Delete a database
client.deleteDatabase("analytics_db").await().indefinitely();
```

### 3. Data Operations

```java
// Save a document
MyObject doc = new MyObject("key1", "value");
client.save("my_collection", doc).await().indefinitely();

// Find a document
MyObject result = client.findById("my_collection", "key1").await().indefinitely();
```

The driver uses `Mutiny` (Uni/Multi) for non-blocking I/O.
