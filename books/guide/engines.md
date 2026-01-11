# Multi-Model Engine

JettraDB is a truly multi-model database engine. Unlike other databases that add "wrappers", JettraDB's engines are optimized for each data model while sharing the same underlying Multi-Raft and LSM-Store layers. 

**Note:** Starting from version 1.0, JettraDB databases are **Multi-Model by default**. You don't need to specify an engine during database creation; the database will automatically support all the models listed below integrated into a single instance.

## Supported Engines

### 1. Document Engine
- **Module:** `jettra-engine-document`
- **Format:** JSON/BSON
- **Features:** Rich indexing, sub-document queries.

### 2. Column Engine (OLAP)
- **Module:** `jettra-engine-column`
- **Use Case:** Analytics, big data.
- **Optimization:** Columnar storage for fast aggregations.

### 3. Key-Value Engine
- **Module:** `jettra-engine-key-value`
- **Use Case:** Caching, session management.
- **Latency:** Sub-millisecond.

### 4. Graph Engine
- **Module:** `jettra-engine-graph`
- **Format:** Labeled Property Graph (LPG).
- **Optimization:** Native vertex/edge storage with fast traversals.
- **Algorithms:** Built-in BFS (Breadth-First Search) and DFS for relationship traversal. Optimized for deep path queries.

### 5. Vector Engine (AI)
- **Module:** `jettra-engine-vector`
- **Use Case:** LLM context, similarity search, recommendation systems.
- **Algorithms:** Optimized Cosine Similarity search. Designed for high-dimensional data (1536+ dimensions).
- **Indexing:** Supports tiered indexing for low-latency retrieval.

### 6. Time-Series Engine
- **Module:** `jettra-engine-time-series`
- **Use Case:** IoT, monitoring.
- **Optimization:** Compression for time-series data.

### 7. Geographic Engine
- **Module:** `jettra-engine-geographics`
- **Use Case:** GIS, location-based services.
- **Indexes:** S2, R-Tree.

### 8. Object Engine
- **Module:** `jettra-engine-object`
- **Use Case:** Cloud-native object storage.
- **API:** Compatible with major object storage protocols.
