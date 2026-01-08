package io.jettra.tx;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.jboss.logging.Logger;

/**
 * Two-Phase Commit (2PC) Coordinator for Multi-Raft JettraDB with Auditing.
 */
@ApplicationScoped
public class TransactionCoordinator {
    private static final Logger LOG = Logger.getLogger(TransactionCoordinator.class);

    @Inject
    AuditService audit;

    public enum TransactionStatus { START, PREPARED, COMMITTED, ABORTED }
    
    public record Transaction(String txId, TransactionStatus status, List<Long> participants) {}

    private final Map<String, Transaction> activeTransactions = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public Uni<String> begin() {
        String txId = "tx-" + idGenerator.incrementAndGet();
        activeTransactions.put(txId, new Transaction(txId, TransactionStatus.START, new ArrayList<>()));
        return audit.log(txId, "BEGIN", "Transaction started")
                .replaceWith(txId);
    }

    /**
     * Phase 1: Prepare
     */
    public Uni<Boolean> prepare(String txId, List<Long> raftGroupIds) {
        Transaction tx = activeTransactions.get(txId);
        if (tx == null) return Uni.createFrom().item(false);

        return audit.log(txId, "PREPARE", "Participants: " + raftGroupIds)
                .onItem().transformToUni(v -> Uni.createFrom().item(true))
                .onItem().invoke(() -> {
                    activeTransactions.put(txId, new Transaction(txId, TransactionStatus.PREPARED, raftGroupIds));
                });
    }

    /**
     * Phase 2: Commit
     */
    public Uni<Void> commit(String txId) {
        Transaction tx = activeTransactions.get(txId);
        if (tx == null || tx.status() != TransactionStatus.PREPARED) {
            return Uni.createFrom().failure(new RuntimeException("TX not in PREPARED state"));
        }

        return audit.log(txId, "COMMIT", "Transaction committed successfully")
                .onItem().invoke(() -> {
                    activeTransactions.put(txId, new Transaction(txId, TransactionStatus.COMMITTED, tx.participants()));
                });
    }

    /**
     * Abort / Rollback
     */
    public Uni<Void> abort(String txId) {
        return audit.log(txId, "ABORT", "Transaction rolled back")
                .onItem().invoke(() -> {
                    activeTransactions.computeIfPresent(txId, (id, tx) -> 
                        new Transaction(id, TransactionStatus.ABORTED, tx.participants())
                    );
                });
    }
}
