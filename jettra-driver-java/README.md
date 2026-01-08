# Jettra Java Driver

Official Java driver for JettraDB, featuring a modern Repository Pattern implementation.

## Features
*   **Reactive Core**: Built on SmallRye Mutiny.
*   **Repository Pattern**: Jakarta Data/NoSQL inspired API.
*   **Java Record Support**: Use immutable records as entities.
*   **Sequential IDs**: Automatic unique ID generation via `@Id(sequential = true)`.

## Quick Start
```java
public interface ProductRepository extends Repository<Product, Long> {
    @Save
    Product save(Product p);
    
    @Find
    Optional<Product> findById(Long id);
}
```

Refer to the [Repository Guide](../books/guide/repository.md) for more details.
