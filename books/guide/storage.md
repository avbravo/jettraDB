# Storage: Optimized LSM Engine

The `jettra-store` module implements an optimized Object Storage layer using Log-Structured Merge-tree (LSM) principles.

## Structure

1. **MemTable:** In-memory buffer for the latest writes. Sorted for fast access.
2. **Commit Log (WAL):** Persistent record of writes for crash recovery.
3. **SSTables:** Immutable on-disk files containing sorted rows.
4. **Compaction:** Background process that merges SSTables to reclaim space and maintain read performance.

## Object Storage Optimization

JettraDB treats every data point as an object. This allows:
- **Variable Key Sizes:** Flexible indexing.
- **Versioning:** Built-in support for historical data.
- **Compression:** High-ratio compression for cloud-native cost efficiency.

## Interaction with Raft

The storage layer is the "State Machine" in the Raft consensus. When Raft commits an entry, it is applied to the `jettra-store`.

```java
public class JettraStateMachine implements StateMachine {
    @Inject ObjectStorage storage;

    @Override
    public void apply(byte[] data) {
        // Parse and apply to LSM store
        storage.put(key, data);
    }
}
```
