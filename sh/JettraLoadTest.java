package io.jettra.test;

import io.jettra.driver.JettraReactiveClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Java Load Test using the Reactive Jettra Driver.
 */
public class JettraLoadTest {

    public static void main(String[] args) {
        String pdAddress = "localhost:9000";
        int totalRecords = 5000;
        int concurrency = 50;

        JettraReactiveClient client = new JettraReactiveClient(pdAddress);
        AtomicInteger count = new AtomicInteger(0);

        System.out.println("🚀 Starting Java Reactive Load Test (" + totalRecords + " records)...");
        long start = System.currentTimeMillis();

        // Create a stream of integers and transform to parallel reactive saves
        Multi.createFrom().range(0, totalRecords)
            .onItem().transformToUniAndMerge(i -> {
                String id = "java-record-" + i;
                return client.save("performance_test", "{ \"id\": \"" + id + "\", \"val\": " + i + " }")
                    .onItem().invoke(v -> count.incrementAndGet());
            })
            // Set concurrency limit
            .collect().asList()
            .await().atMost(Duration.ofMinutes(5));

        long end = System.currentTimeMillis();
        double seconds = (end - start) / 1000.0;

        System.out.println("\n--- Java Load Test Finished ---");
        System.out.println("Total Time: " + seconds + "s");
        System.out.println("Total Inserted: " + count.get());
        System.out.println("Throughput: " + (count.get() / seconds) + " ops/s");
    }
}
