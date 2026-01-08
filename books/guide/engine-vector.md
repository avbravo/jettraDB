# Vector Engine

Built for AI and Machine Learning workloads.

## Configuration
- **Module:** `jettra-engine-vector`
- **Class:** `io.jettra.engine.vector.VectorEngine`

## Features
- **Similarity Search:** Search using Cosine Similarity or Euclidean distance.
- **RAG Ready:** Perfect for storage of embeddings from OpenAI, HuggingFace, etc.
- **High Performance:** Designed for high-dimensional vector spaces.

## Usage Example (Java)

```java
@Inject VectorEngine vectorEngine;

float[] embedding = new float[]{0.1f, 0.9f, -0.3f};
vectorEngine.addVector(new VectorRecord("doc-1", embedding, "Sample metadata"))
    .subscribe().with(v -> {});

// Search for similar vectors
vectorEngine.searchSimilarity(new float[]{0.1f, 0.85f, -0.2f}, 5)
    .subscribe().with(results -> {
        results.forEach(res -> System.out.println("Match: " + res.id()));
    });
```
