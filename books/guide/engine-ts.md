# Time-Series Engine

Optimized for high-frequency time-stamped data.

## Configuration
- **Module:** `jettra-engine-time-series`
- **Class:** `io.jettra.engine.ts.TimeSeriesEngine`

## Features
- **Range Queries:** Optimized for "last X hours" queries.
- **Efficient Storage:** Time-ordered indexing reduces seek times.
- **Automatic Compression:** Data is compressed as it ages.

## Usage Example (Java)

```java
@Inject TimeSeriesEngine ts;

// Log a metric
ts.insert("cpu_load", new DataPoint(Instant.now(), 45.3, Map.of("node", "srv-1")))
    .subscribe().with(v -> {});

// Query range
Instant start = Instant.now().minus(Duration.ofHours(1));
ts.queryRange("cpu_load", start, Instant.now())
    .subscribe().with(points -> System.out.println("Points found: " + points.size()));
```
