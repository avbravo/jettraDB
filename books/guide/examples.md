# JettraDB Usage Examples

## 1. Document Storage (Java Reactive)

```java
import io.jettra.driver.JettraClient;
import io.jettra.driver.DocumentEngine;

JettraClient client = JettraClient.create("pd-address:9000");
DocumentEngine docEngine = client.getDocumentEngine();

// Persistent reactive save
docEngine.save("sensors", "sensor-01", "{\"temp\": 22.5, \"status\": \"OK\"}")
    .onItem().transform(v -> "Saved successfully!")
    .subscribe().with(System.out::println);
```

## 2. Key-Value Operations

```java
JettraClient client = JettraClient.create("pd-address:9000");
KvEngine kv = client.getKvEngine();

kv.put("config:max_retries", "5")
    .chain(() -> kv.get("config:max_retries"))
    .subscribe().with(val -> System.out.println("Max Retries: " + val));
```

## 3. Shell Interaction

```bash
# Connect to cluster
jettra-shell connect localhost:9000

# Insert a document
jettra-shell query "INSERT INTO users {name: 'Alice', role: 'admin'}"

# List nodes
jettra-shell query "SHOW NODES"
```

## 4. Vector Search (AI)

```java
VectorEngine vector = client.getVectorEngine();
float[] embedding = {0.1f, 0.5f, -0.2f};

vector.search("profiles", embedding, 10)
    .subscribe().with(results -> {
        results.forEach(res -> System.out.println("Found match: " + res.getId()));
    });
```

## 5. Load Testing & Benchmarking

We have provided scripts to test the cluster performance under high load in the `sh/` directory:

### Python Benchmark
Uses thread pools to perform simultaneous REST insertions.
```bash
bash sh/run_python_test.sh
```

### Java Reactive Benchmark
Uses the native driver with Mutiny to perform non-blocking high-throughput inserts.
```bash
bash sh/run_java_test.sh
```
