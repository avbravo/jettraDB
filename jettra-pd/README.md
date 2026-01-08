# Jettra PD (Placement Driver)

Jettra PD is the management component of JettraDB. It is responsible for:

*   **Cluster Metadata**: Storing information about nodes, databases, and collections.
*   **Leader Election**: Coordinating leader election for storage groups.
*   **Load Balancing**: Guiding data placement and rebalancing across the cluster.
*   **TDM (Timestamp Oracle)**: Providing monotonically increasing timestamps for distributed transactions.

## Technologies
- Quarkus
- gRPC / REST
- Raft Consensus
