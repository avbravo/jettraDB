# Document Engine: Optimized Persistence & Indexing

El motor de documentos de JettraDB está diseñado para manejar esquemas flexibles con un rendimiento de lectura cercano a la memoria gracias a su caché integrada y su sistema de índices optimizado.

## Especificaciones Técnicas
- **Clase Principal**: `io.jettra.engine.document.DocumentEngine`
- **Consumo de Memoria**: Eficiente mediante el uso de `ConcurrentHashMap` para MemTables y gestión de punteros.
- **Escalabilidad**: Particionado automático mediante Multi-Raft Groups.

## Operaciones de Alta Performance

### 1. Inserción Atómica
Las escrituras se validan primero contra el líder del grupo Raft correspondiente al hash del ID del documento.

```java
engine.save("users", "u101", "{\"name\":\"Alice\",\"status\":\"OK\"}")
      .subscribe().with(v -> LOG.info("Insertado"));
```

### 2. Búsqueda por ID (O(1))
El motor intenta recuperar el documento de la caché MemTable local antes de consultar el almacenamiento LSM persistente.

```java
engine.findById("users", "u101")
      .onItem().transform(json -> parse(json))
      .subscribe().with(user -> System.out.println(user.name));
```

### 3. Consultas Indexadas (Simuladas)
El motor soporta la creación de índices secundarios para evitar el escaneo completo de la colección (*Full Collection Scan*).

```java
// Busca todos los usuarios con status "OK" usando el índice de campo
engine.findByField("users", "status", "OK")
      .subscribe().with(doc -> System.out.println("Encontrado: " + doc));
```

## Optimización Multi-Model
Al estar integrado en `jettra-engine`, los documentos pueden contener referencias (`RIDs`) a objetos en otros motores (ej. un ID de un vértice en el Graph Engine o una serie temporal), permitiendo consultas híbridas cloud-native.

## Ejemplo Completo de Flujo Reactivo

```java
public Uni<List<String>> processActiveUsers() {
    return engine.findByField("users", "status", "OK")
                 .collect().asList();
}
```
