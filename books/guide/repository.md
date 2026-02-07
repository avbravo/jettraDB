# JettraDB Repository Pattern

JettraDB provides a developer-friendly Repository Pattern inspired by **Jakarta NoSQL**, **Jakarta Data**, and **Jakarta Query**. This pattern abstracts the low-level client operations and allows you to work with **Java Records** or POJOs using annotations.

## Annotations

### Entity Mapping
- `@Entity(collection = "...")`: Marks a class/record as a JettraDB entity.
- `@Id(sequential = true)`: Marks the primary key. If `sequential` is true, JettraDB will handle automatic ID generation.
- `@Column(name = "...")`: Maps a field/component to a specific database field.

### Repository Methods (Jakarta Style)
- `@Save`: Upserts the entity.
- `@Update`: Explicit update operation.
- `@Delete`: Deletes the record.
- `@Find`: Finds a record (usually by ID or complex query).
- `@FindAll`: Retrieves all records in the collection.

## Using Java Records as Entities

Java Records are the preferred way to define entities in JettraDB due to their immutability and conciseness.

```java
import io.jettra.driver.annotations.Column;
import io.jettra.driver.annotations.Entity;
import io.jettra.driver.annotations.Id;

@Entity(collection = "products")
public record Product(
    @Id(sequential = true)
    Long id,
    
    @Column(name = "product_name")
    String name,
    
    Double price
) {}
```

## Fluent Repository Interface

You can define repository interfaces that follow the Jakarta Data specification.

```java
import io.jettra.driver.repository.Repository;
import io.jettra.driver.annotations.*;
import io.smallrye.mutiny.Uni;
import java.util.List;

public interface ProductRepository extends Repository<Product, Long> {
    
    @Save
    Product save(Product product);
    
    @Find
    Optional<Product> findById(Long id);
    
    @FindAll
    List<Product> findAll();
    
    @Delete
    void deleteById(Long id);
}
```

### Aggregations in Repositories ⭐

The repository pattern now supports high-level analytic functions:

```java
var productRepo = new JettraRepositoryImpl<>(client, Product.class);

// Count documents
Uni<Long> total = productRepo.count();

// Sum of a field
Uni<Double> totalSales = productRepo.sum("price");

// Average value
Uni<Double> avgPrice = productRepo.avg("price");
```

## Base Implementation: `JettraRepository`

The `JettraRepository<T, K>` class provides a base implementation that uses reflection to handle common CRUD operations automatically, even for Java Records.

### Example Usage

```java
JettraClient client = ...;
var productRepo = new JettraRepository<>(client, Product.class);

// Saving a Record (ID will be generated if sequential=true)
Product newProduct = new Product(null, "Laptop", 1200.0);
productRepo.save(newProduct).subscribe().with(p -> System.out.println("Saved ID: " + p.id()));
```

## Summary of Features
- **Reactive by Default**: All operations return `Uni` or `Multi`.
- **Automatic ID Generation**: Enabled via `@Id(sequential = true)`.
- **Multi-Engine Support**: Repositories work seamlessly across Document, Key-Value, and Graph engines.
- **Jakarta Standards**: Compatible with Jakarta EE dependency injection and programming models.
