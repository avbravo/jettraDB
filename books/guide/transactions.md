# Distributed Transactions in JettraDB

Ensuring consistency across multiple shards (Raft Groups) using the Two-Phase Commit (2PC) protocol.

## Introduction
In a distributed database like JettraDB, a single operation (like a bank transfer) might involve data residing in different Raft physical groups. To ensure **Atomicity**, we implement a Transaction Coordinator (TC) that manages the lifecycle of these operations.

## Architecture
- **Coordinator**: Managed by the Placement Driver (PD) or a dedicated `jettra-tx` node.
- **Participants**: Individual Raft Group Leaders.
- **Protocol**: Two-Phase Commit (2PC).

## Transaction Lifecycle (2PC)

### Phase 1: Prepare
1. The client starts a transaction and receives a `txId`.
2. The client performs operations (Save, Update, etc.).
3. The Coordinator sends a **PREPARE** message to all involved Raft groups.
4. Each group locks the records locally and logs a "Prepared" intent in its Raft Log.
5. Participants respond with a vote: `COMMIT` or `ABORT`.

### Phase 2: Commit / Abort
1. If **ALL** participants voted `COMMIT`, the Coordinator sends a **GLOBAL_COMMIT**.
2. If **ANY** participant voted `ABORT` (or timed out), the Coordinator sends a **GLOBAL_ABORT** (Rollback).
3. Participants finalize the changes or release the locks.

## Usage Example (Java API)

```java
@Inject TransactionCoordinator tc;

public Uni<Void> transferMoney(String from, String to, double amount) {
    return tc.begin().chain(txId -> {
        return engine.saveDocument("accounts", from, "{...}")
            .chain(() -> engine.saveDocument("accounts", to, "{...}"))
            // Execute 2PC
            .chain(() -> tc.prepare(txId, List.of(groupA, groupB)))
            .chain(success -> success ? tc.commit(txId) : tc.abort(txId));
    });
}
```

## Saga Pattern (Alternative)
For very long-running transactions where holding locks is not feasible, JettraDB supports **Sagas** via compensating actions. 

- **Success path**: Op A -> Op B -> Op C.
- **Failure path**: If B fails, execute Undo A.

## Guarantees
- **Acid Compliance**: JettraDB transactions provide ACID guarantees even in a sharded environment.
- **Fault Tolerance**: If the Coordinator fails during Phase 2, the new Coordinator can reconstruct the state from the participants' status.
