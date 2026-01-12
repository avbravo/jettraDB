# JettraDB

JettraDB is a modern, high-performance, distributed multi-model database engine supporting Document, Graph, Vector, and Key-Value data models.

## Architecture

JettraDB follows a distributed architecture with specialized nodes:

*   **Placement Driver (PD)**: Manages cluster metadata and node coordination.
*   **Storage Engine**: Handles low-level data persistence across multiple models.
*   **Consensus (Raft)**: Ensures high availability and data consistency.
*   **Jettra Web**: Management console and REST interface.

## Modules

*   `jettra-pd`: Placement Driver for cluster orchestration.
*   `jettra-engine`: Core multi-model storage engine.
*   `jettra-store`: Data store service implementation.
*   `jettra-web`: Web-based management interface.
*   `jettra-shell`: Interactive CLI for database operations.
*   `jettra-driver-java`: Official Java driver with Repository architecture support.
*   `jettra-consensus`: Raft-based consensus implementation.
*   `jettra-tx`: Distributed transaction manager.

## Quick Start
To build and run the entire JettraDB cluster using Docker:

```bash
# Recommendation: Use the provided automated script
bash buildandrundockercompose.sh

# Or manually:
mvn clean package -DskipTests
docker-compose up -d --build
```

Access the dashboard at: `http://localhost:8081`

## Documentation

Comprehensive documentation can be found in the `books/guide` directory:
- [Getting Started](books/guide/getting-started.md)
- [Architecture](books/guide/architecture.md)
- [Docker Deployment](books/guide/docker.md)
- [Java Driver & Repository](books/guide/repository.md)

## License

MIT License
