# JettraDB Docker Deployment Guide

This guide explains how to deploy the JettraDB stack using Docker and Docker Compose. JettraDB is designed for distributed environments, and containerization simplifies the orchestration of its various components.

## Core Components

The JettraDB architecture consists of three main service types:

| Component | Role | Description |
|-----------|------|-------------|
| **Jettra PD** | Placement Driver | The "brain" of the cluster. Manages metadata, node registration, and Raft group leadership. |
| **Jettra Store** | Storage Node | Data nodes that store the actual data and participate in Multi-Raft replication. |
| **Jettra Web** | Dashboard | A web-based interface for monitoring cluster health, nodes, and Raft groups. |

## Docker Compose Walkthrough

The following `docker-compose.yaml` defines a minimal cluster with one PD, one Web Dashboard, and three Storage Nodes.

```yaml
version: '3.8'

services:
  # Placement Driver - The Cluster Brain
  jettra-pd:
    build:
      context: ./jettra-pd
      dockerfile: src/main/docker/Dockerfile.jvm
    container_name: jettra-pd
    ports:
      - "9000:9000" # gRPC Port
      - "8080:8080" # REST API Port
    environment:
      - QUARKUS_HTTP_PORT=8080
      - JETTRA_PD_ADDR=0.0.0.0:9000
    networks:
      - jettra-net

  # Web Dashboard
  jettra-web:
    build:
      context: ./jettra-web
      dockerfile: src/main/docker/Dockerfile.jvm
    container_name: jettra-web
    ports:
      - "8081:8080" # Host 8081 -> Container 8080
    depends_on:
      - jettra-pd
    environment:
      - QUARKUS_HTTP_PORT=8080
      - JETTRA_PD_URL=http://jettra-pd:8080
    networks:
      - jettra-net

  # Storage Nodes (Scale as needed)
  jettra-store-1:
    build:
      context: ./jettra-store
      dockerfile: src/main/docker/Dockerfile.jvm
    container_name: jettra-store-1
    depends_on:
      - jettra-pd
    environment:
      - QUARKUS_HTTP_PORT=8080
      - JETTRA_PD_ADDR=jettra-pd:9000
      - JETTRA_NODE_ID=jettra-store-1
    networks:
      - jettra-net

  # ... jettra-store-2 and jettra-store-3 follow the same pattern
```

### Network Configuration
All components communicate over an internal bridge network named `jettra-net`. This ensures that storage nodes can reach the Placement Driver using its container name (`jettra-pd`).

## Custom Deployment Examples

### Example 1: Changing External Ports
If port `8081` or `9000` is already in use on your host, you can remap them in the `ports` section:

```yaml
  jettra-web:
    # ...
    ports:
      - "9090:8080" # Dashboard now at http://localhost:9090
  
  jettra-pd:
    # ...
    ports:
      - "10000:9000" # PD gRPC at port 10000
      - "8082:8080"  # PD REST API at port 8082
```

### Example 2: Fixed IP Assignment
For production-like scenarios where you want nodes to have static internal IPs:

```yaml
services:
  jettra-pd:
    # ...
    networks:
      jettra-net:
        ipv4_address: 172.20.0.10

  jettra-store-1:
    # ...
    environment:
      - JETTRA_PD_ADDR=172.20.0.10:9000
    networks:
      jettra-net:
        ipv4_address: 172.20.0.11

networks:
  jettra-net:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

### Example 3: Running Multiple Web Dashboards
You can run multiple instances of the web dashboard pointing to the same cluster:

```yaml
  jettra-web-viewer:
    image: jettradb/jettra-web:latest
    ports:
      - "8082:8080"
    environment:
      - JETTRA_PD_URL=http://jettra-pd:8080
    networks:
      - jettra-net
```

## Troubleshooting

- **Connection Errors**: Ensure all nodes are on the same Docker network.
- **Port Conflicts**: Use `docker ps` to verify no other services are using the assigned ports.
- **Logs**: Use `docker logs -f <container_name>` to debug startup issues.
