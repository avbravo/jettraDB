package io.jettra.driver;

public record NodeInfo(
        String id,
        String address,
        String role,
        String status,
        long lastSeen,
        double cpuUsage,
        long memoryUsage,
        long memoryMax) {
}
