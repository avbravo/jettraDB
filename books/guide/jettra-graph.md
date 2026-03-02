# JettraDB Graph Engine Guide

JettraDB provides a powerful Native Graph Engine that allows you to manage and query complex relationships via direct node pointers without the need for expensive JOINs. This guide covers how to use the Graph Engine through the Java Driver (`jettra-driver-java`).

## High-Performance Graph Operations

The Graph Engine supports Labeled Property Graphs (LPG). Nodes are represented as Vertices, and relationships as Edges.

### 1. Creating Vertices

A vertex represents an entity in the graph. It requires an ID, a Label (type), and a Map of properties.

```java
import io.jettra.driver.JettraClient;
import java.util.Map;

// Obtain the client
JettraClient client = ...;

// Create a Person vertex
client.addVertex(
    "person:alice", 
    "Person", 
    Map.of("name", "Alice", "age", 30)
).subscribe().with(v -> System.out.println("Vertex created!"));

// Create another Person vertex
client.addVertex(
    "person:bob", 
    "Person", 
    Map.of("name", "Bob", "age", 32)
).subscribe().with(v -> System.out.println("Vertex created!"));
```

### 2. Creating Edges (Relationships)

An edge defines a directed relationship between two vertices.

```java
// Alice follows Bob
client.addEdge(
    "person:alice", 
    "person:bob", 
    "FOLLOWS"
).subscribe().with(e -> System.out.println("Edge created!"));
```

### 3. Graph Traversals (K-Steps BFS)

You can explore connections up to a specific depth using high-performance Breadth-First Search (BFS).

```java
// Find connections up to depth 3 from Alice
client.traverseGraph("person:alice", 3)
    .subscribe().with(vertexList -> {
        System.out.println("Traversed Vertices:");
        vertexList.forEach(System.out::println);
    });
```

## Internal Engine Representation

Under the hood, the Graph Operations translate to the engine module `jettra-engine-graph`.

- **Vertices**: Stored using the `graph:v:{id}` key pattern with properties serialized effectively.
- **Edges**: Stored using the `graph:e:{fromId}:{toId}` pattern. An optimized adjacency list updates concurrently for rapid traversal.
- **Persistence**: Vertices and relationships replicate robustly across the Raft consensus group.

## Use Cases

- **Social Networks**: Fast friend-of-friend discoveries.
- **Fraud Detection**: Spotting cyclical transfer patterns in finance.
- **Recommendations**: Content and product discovery (e.g. users who bought x also bought y).

## JettraShell Examples

You can interact with the Graph Engine directly from the interactive `jettra-shell`.

1. **Connect and Login**:
   ```bash
   jettra> connect localhost:8081
   jettra> login admin
   ```

2. **Add Vertices**:
   ```bash
   jettra> graph add-vertex "person:alice" "Person" "{\"name\":\"Alice\",\"age\":30}"
   Vertex 'person:alice' added successfully.
   
   jettra> graph add-vertex "person:bob" "Person" "{\"name\":\"Bob\",\"age\":32}"
   Vertex 'person:bob' added successfully.
   ```

3. **Add Edges**:
   ```bash
   jettra> graph add-edge "person:alice" "person:bob" "FOLLOWS"
   Edge from 'person:alice' to 'person:bob' added successfully.
   ```

4. **Traverse Graph**:
   Traverse from Alice up to a depth of 3:
   ```bash
   jettra> graph traverse "person:alice" 3
   Traversal results:
   [{"id":"person:alice","label":"Person","properties":{"name":"Alice","age":30}},{"id":"person:bob","label":"Person","properties":{"name":"Bob","age":32}}]
   ```

## REST API (cURL) Examples

You can also use standard HTTP tools like `curl` to interact with the Graph API. Ensure you have obtained a valid Bearer token from the `/api/web-auth/login` endpoint.

**Add a Vertex**:
```bash
curl -X POST http://localhost:8081/api/v1/graph/vertex \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"id": "person:charlie", "label": "Person", "properties": {"name": "Charlie", "age": 28}}'
```

**Add an Edge**:
```bash
curl -X POST http://localhost:8081/api/v1/graph/edge \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fromId": "person:alice", "toId": "person:charlie", "label": "KNOWS"}'
```

**Traverse Graph**:
```bash
curl -X GET "http://localhost:8081/api/v1/graph/traverse/person:alice?depth=2" \
  -H "Authorization: Bearer YOUR_TOKEN"
```
