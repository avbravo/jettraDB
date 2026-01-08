package io.jettra.pd;

import java.util.List;

public record RaftGroupMetadata(
    long groupId,
    String leaderId,
    List<String> peers
) {}
