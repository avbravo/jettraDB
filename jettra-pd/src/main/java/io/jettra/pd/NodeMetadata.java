package io.jettra.pd;

public record NodeMetadata(
                String id,
                String address,
                String role,
                String status,
                String raftRole,
                long lastSeen,
                double cpuUsage,
                long memoryUsage,
                long memoryMax) {
}
