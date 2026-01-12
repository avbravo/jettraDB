# JettraDB Geospatial Engine

The **JettraDB Geospatial Engine** provides specialized support for storing, indexing, and querying geospatial data. It is designed to handle location-based services, efficient spatial queries, and GeoJSON data structures within the multi-model ecosystem of JettraDB.

## Features

- **GeoJSON Support**: Store Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon, and GeometryCollection.
- **Spatial Indexing**: Utilizes QuadTrees (and planned R-Trees) for efficient spatial lookups.
- **Spatial Queries**:
    - `NEAR`: Find points within a certain radius.
    - `WITHIN`: Find geometries entirely inside a polygon.
    - `INTERSECTS`: Find geometries that intersect with a given shape.

## Usage in Jettra Shell

You can interact with the Geospatial Engine using the unified `sql` interface or direct engine commands.

### Creating a Geospatial Database

```bash
# Create a dedicated geospatial database
db create city_maps --engine Geospatial --storage STORE
```

### Storing Data (GeoJSON)

```sql
-- Insert a location (Point)
INSERT INTO landmarks VALUES ('statue_liberty', {
    "type": "Point",
    "coordinates": [-74.0445, 40.6892],
    "properties": {"name": "Statue of Liberty"}
});

-- Insert a zone (Polygon)
INSERT INTO zones VALUES ('central_park', {
    "type": "Polygon",
    "coordinates": [[
        [-73.981, 40.768],
        [-73.958, 40.800],
        [-73.949, 40.796],
        [-73.973, 40.764],
        [-73.981, 40.768]
    ]]
});
```

### Querying Data

#### Find Nearby Locations

```bash
# Find landmarks within 5km of a point (lat, lon)
sql SELECT * FROM landmarks WHERE NEAR(-74.006, 40.7128, 5000)
```

#### Find Points Within a Zone

```bash
# Find all landmarks inside Central Park
sql SELECT * FROM landmarks WHERE WITHIN('central_park')
```

## Java Driver Example

```java
import io.jettra.driver.JettraDriver;
import io.jettra.driver.JettraClient;

public class MapsExample {
    public static void main(String[] args) {
        JettraClient client = JettraDriver.connect("localhost:8081");
        
        // Store a location
        String json = """
            {
                "type": "Point",
                "coordinates": [-122.4194, 37.7749],
                "name": "San Francisco"
            }
        """;
        client.database("maps_db").collection("cities").insert(json);
        
        System.out.println("City stored.");
    }
}
```
