# JettraDB Architecture Overview

JettraDB is a high-performance, multi-model, cloud-native database designed for modern workloads. It leverages Multi-Raft groups for consistency and a specialized engine architecture for diverse data types.

## Components

### 1. Placement Driver (PD)
The "brain" of the cluster. It manages:
- Node health and heartbeats.
- Raft group assignments.
- Database and collection metadata.
- Load balancing.

### 2. Jettra Store
The storage layer where data resides. Each store node contains:
- Multiple specialized engines (Document, Graph, Vector, Column).
- Local Raft state machines.
- Versioned storage (jettra-store).

### 3. Jettra Engines
Specialized processing units for different data models:
- **Document Engine**: Handles JSON documents with automatic indexing and versioning.
- **Graph Engine**: Specialized for relationship traversal and graph algorithms.
- **Vector Engine**: Performs high-speed similarity search for AI/ML embeddings.
- **Column Engine**: Optimized for analytical queries (aggregations).

### 4. Jettra Web / UI
The management console provided via a web interface, allowing:
- Monitoring node resources.
- Managing databases and collections.
- Executing SQL and Mongo queries.
- Managing users and roles.

### 5. Jettra Shell
Terminal-based interactive CLI for administrative and data operations.

### 6. Jettra Driver (Java)
Fluent and reactive Java client supporting:
- Repository patterns.
- Annotation-based entity mapping.
- Fluent Query builder.

## Architecture Diagram
![Architecture Diagram](resources/architecture.png)

## Data Replication (Multi-Raft)
JettraDB shunts data into "groups", each managed by its own Raft instance. This allows horizontal scaling of both reads and writes.
![Raft Groups](resources/raft_groups.png)
