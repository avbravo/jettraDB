package io.jettra.engine.core;

import io.jettra.store.ObjectStorage;
import io.jettra.consensus.MultiRaftManager;
import io.jettra.consensus.RaftGroup;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractEngine {
    
    @Inject
    protected ObjectStorage storage;
    
    @Inject
    protected MultiRaftManager raftManager;

    protected Uni<Void> writeData(long groupId, String key, String value) {
        RaftGroup group = raftManager.getOrCreateGroup(groupId);
        
        // Multi-Raft Logic: If this node is not the leader of the group, 
        // it must reject the write or forward it to the leader.
        if (group.getState() != io.jettra.consensus.RaftState.LEADER) {
            // Simplified: we assume success for the demo or auto-promote if solo
            if (group.getLeaderId() == null) {
                group.setState(io.jettra.consensus.RaftState.LEADER);
            } else {
                 return Uni.createFrom().failure(new RuntimeException("Not the leader for Group " + groupId + ". Leader is: " + group.getLeaderId()));
            }
        }
        
        return storage.put(key, value.getBytes(StandardCharsets.UTF_8));
    }

    protected Uni<String> readData(String key) {
        return storage.get(key)
                .onItem().ifNotNull().transform(bytes -> new String(bytes, StandardCharsets.UTF_8))
                .onItem().ifNull().continueWith("");
    }
}
