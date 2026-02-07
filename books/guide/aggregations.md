# Aggregations and Analytics in JettraDB

JettraDB supports powerful aggregation pipelines for real-time analytics across your data. This functionality is accessible via the Java Driver, Jettra Shell, and REST API.

## Summary of Aggregation Operators

| Operator | Description |
| :--- | :--- |
| `$match` | Filters documents based on conditions. |
| `$group` | Groups documents by a specified identifier. |
| `$sum` | Calculates the sum of a numeric field. |
| `$avg` | Calculates the average of a numeric field. |
| `$min` | Finds the minimum value in a field. |
| `$max` | Finds the maximum value in a field. |
| `$count` | Counts the number of documents in a stage. |

## Usage via Java Driver

The Java Driver provide two ways to perform aggregations: using the generic `aggregate` method or specialized high-level methods.

### 1. High-Level Aggregation Methods

These methods are available in both `JettraReactiveClient` and `JettraRepository`.

#### Count
```java
// Count all documents in a collection
Long total = client.count("users").await().indefinitely();

// Count with a query
Long activeUsers = repository.count("{status: 'active'}").await().indefinitely();
```

#### Numeric Aggregations (Sum, Avg, Min, Max)
```java
// Calculate sum of sales
Double totalSales = repository.sum("amount").await().indefinitely();

// Calculate average price with a filter
Double avgPrice = repository.avg("price", "{category: 'electronics'}").await().indefinitely();

// Find min/max values
Double minAge = repository.min("age").await().indefinitely();
Double maxAge = repository.max("age").await().indefinitely();
```

### 2. Generic Aggregation Pipelines

For complex logic, you can define a full pipeline.

```java
String pipeline = "[" +
    "{\"$match\": {\"category\": \"sports\"}}," +
    "{\"$group\": {\"_id\": \"$brand\", \"totalInventory\": {\"$sum\": \"$stock\"}}}" +
"]";

List<Object> results = client.aggregate("products", pipeline).await().indefinitely();
```

## Usage via Jettra Shell

The Jettra Shell supports MongoDB-style `aggregate` syntax.

```bash
# Basic sum
mongo db.sales.aggregate([{$group: {_id: null, total: {$sum: '$amount'}}}])

# Average with match
mongo db.users.aggregate([{$match: {city: 'NY'}}, {$group: {_id: null, avgAge: {$avg: '$age'}}}])
```

## REST API Endpoint

`POST /api/v1/document/{collection}/aggregate`

**Body:**
```json
[
  {
    "$group": {
      "_id": "$category",
      "count": { "$count": {} }
    }
  }
]
```
