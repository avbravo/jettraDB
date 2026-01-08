# Geographics Engine

Support for geospatial data and spatial queries.

## Configuration
- **Module:** `jettra-engine-geographics`
- **Class:** `io.jettra.engine.geo.GeoEngine`

## Features
- **Spatial Indexing:** Fast lookup of coordinates.
- **Radius Search:** Find entities within X kilometers of a location.
- **Polygon Support:** (Upcoming) Intersect and Containment checks.

## Usage Example (Java)

```java
@Inject GeoEngine geo;

// Add a point of interest
geo.addPoint(new Point(40.4168, -3.7038, "Madrid Center"))
    .subscribe().with(v -> {});

// Search nearby
geo.findNearby(40.42, -3.71, 5.0) // 5km radius
    .subscribe().with(matches -> {
        matches.forEach(p -> System.out.println("Located: " + p.metadata()));
    });
```
