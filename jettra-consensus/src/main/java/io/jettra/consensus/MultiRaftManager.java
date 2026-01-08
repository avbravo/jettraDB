package io.jettra.consensus;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MultiRaftManager {
    private static final Logger LOG = Logger.getLogger(MultiRaftManager.class);
    
    private final Map<Long, RaftGroup> groups = new ConcurrentHashMap<>();

    public RaftGroup getOrCreateGroup(long groupId) {
        return groups.computeIfAbsent(groupId, id -> {
            LOG.infof("Creating new Raft group: %d", id);
            return new RaftGroup(id);
        });
    }

    public Map<Long, RaftGroup> getGroups() {
        return groups;
    }
}
