package io.jettra.consensus;

import io.jettra.consensus.proto.*;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@GrpcService
public class MultiRaftService implements RaftService {
    private static final Logger LOG = Logger.getLogger(MultiRaftService.class);

    @Inject
    MultiRaftManager raftManager;

    @Override
    public Uni<AppendEntriesResponse> appendEntries(AppendEntriesRequest request) {
        RaftGroup group = raftManager.getOrCreateGroup(request.getGroupId());
        
        // Basic Raft validation
        boolean success = true;
        if (request.getTerm() < group.getCurrentTerm()) {
            success = false;
        } else {
            group.setLeaderId(request.getLeaderId());
        }

        return Uni.createFrom().item(AppendEntriesResponse.newBuilder()
                .setTerm(group.getCurrentTerm())
                .setSuccess(success)
                .build());
    }

    @Override
    public Uni<RequestVoteResponse> requestVote(RequestVoteRequest request) {
        RaftGroup group = raftManager.getOrCreateGroup(request.getGroupId());
        boolean granted = group.requestVote(
                request.getTerm(), 
                request.getCandidateId(), 
                request.getLastLogIndex(), 
                request.getLastLogTerm());

        return Uni.createFrom().item(RequestVoteResponse.newBuilder()
                .setTerm(group.getCurrentTerm())
                .setVoteGranted(granted)
                .build());
    }

    @Override
    public Uni<ProposeResponse> propose(ProposeRequest request) {
        RaftGroup group = raftManager.getOrCreateGroup(request.getGroupId());
        if (group.getState() != RaftState.LEADER) {
            return Uni.createFrom().item(ProposeResponse.newBuilder()
                    .setSuccess(false)
                    .setLeaderId(group.getLeaderId() != null ? group.getLeaderId() : "")
                    .setErrorMessage("Not the leader")
                    .build());
        }
        
        // Log the proposal and replicate (async logic would go here)
        LOG.infof("Proposing data to group %d", request.getGroupId());
        
        return Uni.createFrom().item(ProposeResponse.newBuilder()
                .setSuccess(true)
                .build());
    }
}
