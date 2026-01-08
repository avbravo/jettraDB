# JettraDB Java Driver

The JettraDB Java Driver provides a high-performance, reactive interface for interacting with JettraDB clusters. It is built on top of **Mutiny** and **gRPC**, ensuring efficient resource usage and asynchronous communication.

## Connectivity

The driver connects to the cluster via the **Placement Driver (PD)**, which provides the addresses of the relevant shards and leaders.

### Basic Initialization

```java
import io.jettra.driver.JettraReactiveClient;

// Connect to the Placement Driver
JettraReactiveClient client = new JettraReactiveClient("localhost:9000");
```

## Reactive API (`Uni`)

The driver uses `Uni<T>` from Project Mutiny to handle asynchronous results.

### Saving Documents

```java
Map<String, Object> doc = Map.of("id", "123", "name", "Example");

client.save("my_collection", doc)
    .subscribe().with(
        success -> System.out.println("Document saved successfully"),
        failure -> System.err.println("Failed to save: " + failure.getMessage())
    );
```

### Finding by ID

```java
client.findById("my_collection", "123")
    .onItem().ifNotNull().transform(obj -> (Map<String, Object>) obj)
    .subscribe().with(doc -> {
        System.out.println("Found: " + doc.get("name"));
    });
```

## Multi-Model Support

The Java driver provides specialized methods to interact with JettraDB's various engines.

### Vector Similarity Search

```java
float[] queryVector = {0.1f, 0.5f, 0.9f};
int topK = 5;

client.searchVector(queryVector, topK)
    .subscribe().with(results -> {
        results.forEach(id -> System.out.println("Similar Document ID: " + id));
    });
```

### Graph Traversal

```java
String startNode = "user:101";
int depth = 2;

client.traverseGraph(startNode, depth)
    .subscribe().with(path -> {
        System.out.println("Traversed path: " + String.join(" -> ", path));
    });
```

## Integration with Repository Pattern

While the `JettraReactiveClient` provides low-level access, it is recommended to use the **Repository Pattern** for a more expressive, annotation-based approach.

See the [Repository Pattern Guide](repository.md) for more details.

## Error Handling

The driver uses reactive streams principles for error handling. You can use operators like `.onFailure().retry()` or `.onFailure().recoverWithItem()` to build resilient data access logic.

```java
client.findById("users", "admin")
    .onFailure().retry().atMost(3)
    .subscribe().with(user -> System.out.println("Retrieved: " + user));
```
