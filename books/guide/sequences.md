# Sequential Keys (Sequences) in JettraDB

JettraDB supports persistent, monotonically increasing internal counters called **Sequences**. These are useful for generating unique non-random IDs, order numbers, or any value that requires gaps-free incrementing logic.

## Key Features
- **Auto-incrementing**: Each call to `next` increment the value by a configurable amount.
- **Persistent**: Sequences are managed by the Placement Driver (PD) and shared across the cluster.
- **Multi-tenant**: Sequences are bound to a specific database context.

## Core Concepts
- **Name**: Unique identifier for the sequence within a database.
- **Start Value**: The initial value of the sequence.
- **Increment**: The amount to add on each `next()` call (default is 1).
- **Current Value**: The last generated value.

## Usage Guide

### 1. via cURL (API)
The Placement Driver (PD) exposes the sequence API on port 8081 (default).

```bash
# Create a sequence
curl -X POST http://localhost:8081/api/v1/sequence \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "test_seq", "database": "db1", "startValue": 100, "increment": 1}'

# Get next value
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/v1/sequence/test_seq/next
# Response: {"value": 101}

# Get current value
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/v1/sequence/test_seq/current

# Reset sequence
curl -X POST http://localhost:8081/api/v1/sequence/test_seq/reset \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"newValue": 500}'

# Delete sequence
curl -X DELETE http://localhost:8081/api/v1/sequence/test_seq -H "Authorization: Bearer $TOKEN"
```

### 2. via Java Driver
The `JettraClient` provides native methods to manage sequences.

```java
// Create
client.createSequence("user_ids", "sales_db", 1, 1).await().indefinitely();

// Next value
long nextId = client.nextSequenceValue("user_ids").await().indefinitely();

// Current value
long currentId = client.currentSequenceValue("user_ids").await().indefinitely();

// List sequences
List<String> names = client.listSequences("sales_db").await().indefinitely();

// Reset/Delete
client.resetSequence("user_ids", 0).await().indefinitely();
client.deleteSequence("user_ids").await().indefinitely();
```

### 3. via JettraDB Shell
Interactive commands for quick management.

```bash
# Create
sequence create user_id_seq --start 1000 --inc 5

# Get values
sequence next user_id_seq
sequence current user_id_seq

# Management
sequence list
sequence reset user_id_seq 0
sequence delete user_id_seq
```

### 4. via Web Dashboard
Navigate to the **Sequences** section in the main menu. 
- Click **Create Sequence** to provision a new counter.
- Use the **NEXT** button in the table to test incrementing logic.
- Manage/Delete existing sequences directly from the list.
