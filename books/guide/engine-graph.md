# Graph Engine: Native Relationships

El motor de grafos de JettraDB permite modelar y consultar relaciones complejas de forma nativa. A diferencia de las bases de datos relacionales, el Graph Engine utiliza punteros directos entre nodos, eliminando la necesidad de costosos `JOINs`.

## Especificaciones Técnicas
- **Clase Principal**: `io.jettra.engine.graph.GraphEngine`
- **Modelo**: Labeled Property Graph (LPG).
- **Algoritmos**: Traversal BFS distribuido con soporte para Multi-Raft.

## Operaciones de Alta Performance

### 1. Creación de Vértices y Aristas
Define nodos y las conexiones que los unen de forma reactiva.

```java
// Vértice
engine.addVertex(new Vertex("v1", "Person", Map.of("name", "Alice"))).subscribe().with(v -> {});

// Arista (Relación)
engine.addEdge(new Edge("v1", "v2", "FOLLOWS")).subscribe().with(e -> {});
```

### 2. Recorridos de K-Pasos (Traversals)
Encuentra conexiones a profundidades específicas de forma eficiente.

```java
// Busca conexiones hasta nivel 3 desde el nodo "v1"
engine.traverse("v1", 3)
      .subscribe().with(node -> System.out.println("Relacionado: " + node.id()));
```

## Casos de Uso
- **Redes Sociales**: Detección de círculos de amigos o influenciadores.
- **Detección de Fraude**: Identificación de patrones circulares en transacciones financieras.
- **Sistemas de Recomendación**: Recomendaciones basadas en "quien compró esto también compró...".

## Persistencia
Las aristas se almacenan en una lista de adyacencia optimizada localmente y se replican a través del grupo Raft para garantizar que el grafo sea consistente en todo el cluster.
