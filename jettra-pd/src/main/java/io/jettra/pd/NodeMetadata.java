package io.jettra.pd;

@io.quarkus.runtime.annotations.RegisterForReflection
public record NodeMetadata(
                @com.fasterxml.jackson.annotation.JsonProperty("id") String id,
                @com.fasterxml.jackson.annotation.JsonProperty("address") String address,
                @com.fasterxml.jackson.annotation.JsonProperty("role") String role,
                @com.fasterxml.jackson.annotation.JsonProperty("status") String status,
                @com.fasterxml.jackson.annotation.JsonProperty("raftRole") String raftRole,
                @com.fasterxml.jackson.annotation.JsonProperty("lastSeen") long lastSeen,
                @com.fasterxml.jackson.annotation.JsonProperty("cpuUsage") double cpuUsage,
                @com.fasterxml.jackson.annotation.JsonProperty("memoryUsage") long memoryUsage,
                @com.fasterxml.jackson.annotation.JsonProperty("memoryMax") long memoryMax,
                @com.fasterxml.jackson.annotation.JsonProperty("diskUsage") long diskUsage,
                @com.fasterxml.jackson.annotation.JsonProperty("diskMax") long diskMax) {
}
