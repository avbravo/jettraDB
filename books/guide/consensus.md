# Consensus: Multi-Raft Groups

Traditional Raft uses a single cluster for the entire database. JettraDB uses **Multi-Raft**, which scales by partitioning data into multiple Raft groups.

## How it works

1. **Sharding:** Data is split into ranges (shards).
2. **Raft Groups:** Each shard is managed by a separate Raft group.
3. **Nodes:** A single physical node can participate in hundreds of Raft groups.
4. **Roles:** A node can be a Leader for Group A but a Follower for Group B.

## Advantages

- **Horizontal Scalability:** Add nodes and move Raft groups to balance load.
- **Improved Availability:** Failure of one node only affects the Raft groups where it was a leader.
- **Parallelism:** Commit operations in different groups happen in parallel.

## Implementation Details

The `jettra-consensus` module uses gRPC for inter-node communication. Each message includes a `group_id` to route it to the correct state machine.

```java
// Example of proposing a change to a specific group
raftService.propose(ProposeRequest.newBuilder()
    .setGroupId(targetShardId)
    .setData(payload)
    .build());
```
