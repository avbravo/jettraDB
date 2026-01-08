# Getting Started with JettraDB

JettraDB is a high-performance, multi-model distributed database designed for modern cloud-native applications. It features a Multi-Raft consensus algorithm across multiple node groups, providing high availability and strong consistency.

## Installation

### Maven
Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.jettra</groupId>
    <artifactId>jettra-driver-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Core Guides
- [Architecture & Design](architecture.md)
- [Multi-Raft Consensus](consensus.md)
- [Engines & Multi-Model Support](engines.md)
- [Java Reactive Driver](driver.md)
- [Vector and Graph Search](engines.md)
- [Distributed Transactions (2PC)](transactions.md)
- [Global Auditing System](auditing.md)
- [Repository Pattern](repository.md)
- [Predictive Alerts & Monitoring](monitoring.md)
- [Web Management Interface](web.md)
- [LSM Storage & Persistence](storage.md)
- [Usage Examples](examples.md)

## Starting with Docker
(Easiest)

```bash
docker-compose up -d
```

## Running Locally

To run JettraDB locally, you can use the provided startup scripts:

```bash
./sh/start-cluster.sh
```

## Dashboard
Once the cluster is running, the management dashboard is available at:
`http://localhost:8080`
