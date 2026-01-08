# Global Auditing System

JettraDB includes a built-in auditing system to track all critical operations, ensuring transparency and security in distributed transactions.

## Overview
The `AuditService` provides a centralized or sharded log of all modifications. When a transaction is processed by the `TransactionCoordinator`, every state change (Prepare, Commit, Abort) is automatically logged.

## Features
- **Immutability**: Designed to be stored in specialized append-only Raft groups.
- **Traceability**: Every log entry includes a Transaction ID (`txId`), a unique Log ID, a high-resolution timestamp, and details of the participants.
- **Compliance**: Helps meet regulatory requirements for financial and sensitive industrial data.

## Process Workflow
1. **TX Begin**: Audit logs the initiation of the transaction.
2. **Phase 1 (Prepare)**: Logs the list of participants (Raft Group IDs) that have successfully locked the resources.
3. **Phase 2 (Commit/Abort)**: Logs the final outcome. In case of failure, it logs the reason for the rollback.

## Monitoring Audit Logs
Currently, audit logs are output to the standard log management system (e.g., Graylog, ELK, or CloudWatch via Quarkus Logging).

```bash
# View real-time audit logs in the console
docker logs -f jettra-tx
```

## Java API Integration
The `AuditService` is automatically called by the `TransactionCoordinator`. Developers can also manually log custom business actions:

```java
@Inject AuditService auditor;

// Log a custom security event
auditor.log("N/A", "SECURITY_ALERT_IP_BLOCKED", "IP: 192.168.1.50 Attempted unauthorized access")
       .subscribe().with(v -> {});
```
