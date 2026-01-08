# Key-Value Engine

The Key-Value engine is the fastest way to store and retrieve data in JettraDB.

## Configuration
- **Module:** `jettra-engine-key-value`
- **Class:** `io.jettra.engine.kv.KvEngine`

## Features
- **Ultra-low Latency:** Designed for sub-millisecond response times.
- **Persistence:** Unlike Redis, data is persisted via Multi-Raft to SSD.
- **Atomicity:** Key operations are atomic within a group.

## Usage Example (Java)

```java
@Inject KvEngine kv;

// Set a value
kv.put("session:123", "logged-in").subscribe().with(v -> {});

// Get a value
kv.get("session:123")
    .subscribe().with(opt -> opt.ifPresent(System.out::println));
```
