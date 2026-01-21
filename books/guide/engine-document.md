# Document Engine: Optimized Persistence & Indexing

El motor de documentos de JettraDB está diseñado para manejar esquemas flexibles con un rendimiento de lectura cercano a la memoria gracias a su caché integrada y su sistema de índices optimizado.

## Especificaciones Técnicas
- **Clase Principal**: `io.jettra.engine.document.DocumentEngine`
- **Consumo de Memoria**: Eficiente mediante el uso de `ConcurrentHashMap` para MemTables y gestión de punteros.
- **Escalabilidad**: Particionado automático mediante Multi-Raft Groups.

## Especificaciones Avanzados
- **jettraID**: Identificador único que incluye la dirección física del bucket (`nodeId/bucketName#uuid`). Las referencias entre documentos utilizan esta dirección física.
- **Versiones**: Gestión automática de cambios. Cada actualización archiva la versión anterior permitiendo trazabilidad total.
- **Enriquecimiento JSON**: Soporte nativo para etiquetas (tags) dentro del campo `_tags` para búsquedas rápidas.
- **Documentos Embebidos y Referenciados**: Soporta nativamente objetos anidados y referencias cruzadas mediante etiquetas y `jettraID`.

## Operaciones de Alta Performance

### 1. Inserción con Versionamiento
Las escrituras generan automáticamente una nueva versión si el documento ya existe.

```java
// El motor asigna un jettraID basado en el bucket físico
String jettraId = engine.generateJettraId("node1/bucketA");
engine.save("users", jettraId, "{\"name\":\"Alice\",\"_tags\":[\"vip\"]}");
```

### 2. Resolución de Referencias
Permite obtener documentos vinculados mediante su `jettraID` físico.

```java
engine.resolveReference("orders", "node1/bucketB#order123")
      .subscribe().with(order -> LOG.info("Pedido recuperado"));
```

### 3. Consultas por Etiquetas
Búsqueda optimizada sobre el JSON enriquecido.

```java
engine.findByTag("users", "vip")
      .subscribe().with(doc -> System.out.println("VIP: " + doc));
```

## Optimización Multi-Model
Al estar integrado en `jettra-engine`, los documentos pueden contener referencias (`jettraID`) a objetos en otros motores, manteniendo la consistencia de ubicación incluso si las direcciones IP cambian, ya que los buckets permanecen constantes.


## Integración y Acceso

El motor de documentos se expone a través de múltiples interfaces en el stack JettraDB.

### 1. API REST (Store API)
Los nodos de almacenamiento exponen endpoints específicos para el motor de documentos:
- `POST /api/v1/document/{collection}`: Guardar/Upsert.
- `GET /api/v1/document/{collection}/{jettraId}`: Recuperar por ID.
- `GET /api/v1/document/{collection}/{jettraId}/versions`: Historial de versiones.
- `GET /api/v1/document/{collection}/search/tag?tag={val}`: Búsqueda por tag.

### 2. Jettra Shell (MongoDB Integration)
Puedes usar comandos estilo MongoDB que se traducen automáticamente a operaciones del Document Engine:
```bash
mongo db.usuarios.insert({nombre: "Alice", _tags: ["beta"]})
mongo db.usuarios.find("node1/main#uuid123")
```

### 3. Jettra Web Dashboard
El explorador de documentos permite:
- **Navegación Visual**: Selección de colecciones desde el árbol multi-modelo.
- **Inserción de Documentos**: Interfaz de edición JSON con soporte para etiquetas automáticas.
- **Monitoreo de Ubicación**: Visualización del `jettraID` para verificar la ubicación física (node/bucket) de los datos.

## Ejemplo de Documento Enriquecido
Internamente, el motor añade metadatos de control para garantizar consistencia y auditoría:

```json
{
  "nombre": "Alice Cooper",
  "empresa": "JettraCorp",
  "_tags": ["vip"],
  "jettraID": "node1/clientes-premium#389252...",
  "_version": 2,
  "_lastModified": 1705786400231
}
```
