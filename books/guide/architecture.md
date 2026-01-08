# JettraDB Architecture: Multi-Raft Groups (Stratified Architecture)

JettraDB implements a state-of-the-art stratified architecture designed for cloud-native environments. This guide explains the core components and how they work together.

## 1. Stratified Architecture Layers

### Layer 1: Storage Layer (jettra-store)
The foundation of JettraDB. It uses an optimized LSM-tree (Log-Structured Merge-tree) inspired approach for efficient writes and SSD optimization.
- **Key Features:** Object-based storage, high compression, cloud-native storage integration.

### Layer 2: Replication Layer (jettra-consensus)
Implements the **Multi-Raft Groups** algorithm. Instead of a single Raft cluster, JettraDB manages thousands of small Raft groups.
- **Multi-Raft:** Each shard of data is its own Raft group.
- **Efficiency:** Parallelizes consensus and allows for massive scalability.

### Layer 3: Control Layer (Placement Driver - jettra-pd)
The brain of the cluster.
- **Metadata Management:** Stores the location of all shards.
- **Scheduling:** Automaticaly rebalances data and Raft leaders based on load.
- **Health Monitoring:** Detects node failures and triggers recovery.

### Layer 4: Engine Layer (jettra-engine)
Multi-model support built on top of the consensus and storage layers.
- **Supported Models:** Document, Column, Key-Value, Graph, Vector, Time-Series, Geographics, Object.

## 2. Communication Flow
1. **Client (jettra-driver-java):** Connects to the PD to find the leader of the Raft group responsible for the target data.
2. **Reactive Protocol:** Uses gRPC and Smallrye Mutiny for non-blocking communication.
3. **Writes:** Always directed to the Leader node of the specific Raft group.
4. **Reads:** Can be directed to the leader or followers (with consistency level options).

## 3. Cloud Native Design
- **Observability:** Integrated with Quarkus for metrics (Micrometer), tracing (OpenTelemetry), and health checks.
- **Containerization:** Built to run on Kubernetes with efficient resource usage.
- **Elasticity:** Scales horizontally by adding more nodes and redistributing Raft groups.
