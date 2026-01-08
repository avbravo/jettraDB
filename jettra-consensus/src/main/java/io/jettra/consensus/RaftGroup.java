package io.jettra.consensus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import io.jettra.consensus.proto.LogEntry;

public class RaftGroup {
    private final long groupId;
    private final AtomicLong currentTerm = new AtomicLong(0);
    private volatile String votedFor = null;
    private final List<LogEntry> log = new ArrayList<>();
    private volatile RaftState state = RaftState.FOLLOWER;
    private long commitIndex = 0;
    private long lastApplied = 0;
    private String leaderId = null;

    public RaftGroup(long groupId) {
        this.groupId = groupId;
    }

    public long getGroupId() {
        return groupId;
    }

    public RaftState getState() {
        return state;
    }

    public synchronized void setState(RaftState state) {
        this.state = state;
    }

    public long getCurrentTerm() {
        return currentTerm.get();
    }

    public synchronized boolean requestVote(long term, String candidateId, long lastLogIndex, long lastLogTerm) {
        if (term < currentTerm.get()) {
            return false;
        }
        if (term > currentTerm.get()) {
            currentTerm.set(term);
            votedFor = null;
            state = RaftState.FOLLOWER;
        }
        if (votedFor == null || votedFor.equals(candidateId)) {
            // Check log consistency (simplified)
            votedFor = candidateId;
            return true;
        }
        return false;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }
}
