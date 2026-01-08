# Object Engine

High-performance binary object storage.

## Configuration
- **Module:** `jettra-engine-object`
- **Class:** `io.jettra.engine.object.ObjectEngine`

## Features
- **BLOB Support:** Optimized for streaming large files.
- **Bucket Organization:** Hierarchical storage structure.
- **Cloud-Native Compatibility:** Designed to behave like S3-ready storage.

## Usage Example (Java)

```java
@Inject ObjectEngine objects;

byte[] fileBytes = // ... read from disk
objects.stash("uploads", "image.jpg", fileBytes)
    .subscribe().with(v -> System.out.println("Stored!"));

objects.retrieve("uploads", "image.jpg")
    .subscribe().with(opt -> opt.ifPresent(bytes -> System.out.println("Size: " + bytes.length)));
```
