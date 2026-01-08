package io.jettra.tx;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.UUID;
import org.jboss.logging.Logger;

/**
 * Immutable Audit Service for JettraDB Transactions.
 */
@ApplicationScoped
public class AuditService {
    private static final Logger LOG = Logger.getLogger(AuditService.class);

    public record AuditLog(String id, String txId, String action, Instant timestamp, String details) {}

    public Uni<Void> log(String txId, String action, String details) {
        AuditLog entry = new AuditLog(
            UUID.randomUUID().toString(),
            txId,
            action,
            Instant.now(),
            details
        );
        
        // In a real system, this would write to a specialized immutable Raft group
        // or an Append-Only File (AOF).
        LOG.infof("AUDIT [%s] TX: %s | Action: %s | Details: %s", 
            entry.timestamp(), entry.txId(), entry.action(), entry.details());
            
        return Uni.createFrom().voidItem();
    }
}
