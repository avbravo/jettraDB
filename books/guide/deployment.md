# Cloud Native Deployment

JettraDB is designed for modern cloud environments. This guide explains how to deploy a full cluster using Docker and Kubernetes.

## Docker Compose Deployment

The root of the project contains a `docker-compose.yaml` file that sets up:
1.  **Placement Driver (PD)**: The central coordinator.
2.  **3 Storage Nodes**: Form the base for Multi-Raft groups.
3.  **Web Dashboard**: Visual interface for management.

### Prerequisites
- Docker and Docker Compose installed.
- Maven (to build the artifacts).

### Step 1: Build the Project
Before running Docker, you must build the JAR files:
```bash
mvn clean package -DskipTests
```

### Step 2: Launch the Cluster
```bash
docker-compose up --build
```

### Step 3: Access the UI
Open your browser at `http://localhost:8081` to see the JettraDB Dashboard.

## Scaling the Cluster
To add more storage capacity or increase Raft group parallelism, you can scale the store service:

```bash
docker-compose up -d --scale jettra-store=5
```

## Kubernetes (Conceptual)
In a production environment, JettraDB components should be deployed as:
- **PD**: Deployment with 3 replicas (using its own Raft for high availability).
- **Store**: StatefulSet to ensure consistent identity and persistent volumes for LSM SSTables.
- **Engines**: Can be deployed as sidecars or standalone Microservices depending on the volume.

## Resource Optimization
- **Memory**: Tuning the LSM MemTable size via `JETTRA_STORE_MEMTABLE_SIZE`.
- **Network**: Using gRPC (HTTP/2) for inter-node communication minimizes latency in cloud VPCs.
