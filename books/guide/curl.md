# Ejemplos de uso con cURL para JettraDB

Esta guía proporciona ejemplos de comandos `curl` para interactuar con la API de JettraDB, específicamente para la monitorización, autenticación y administración de bases de datos.

## Autenticación (Requerido)

Todas las peticiones a la API requieren un token JWT válido. Primero debes autenticarte para obtener el token que usarás en las cabeceras `Authorization`.

```bash
# 1. Login para obtener el token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"super-user","password":"adminadmin"}' | jq -r .token)

echo "Token: $TOKEN"
```

## Monitorización de Nodos y Recursos

Para obtener la lista de nodos registrados y su consumo de recursos actual (CPU y Memoria), consulta el endpoint `/api/monitor/nodes` en el puerto 8081.

```bash
curl -s http://localhost:8081/api/monitor/nodes \
  -H "Authorization: Bearer $TOKEN" | jq .
```

**Respuesta esperada (JSON):**

```json
[
  {
    "id": "jettra-store-1",
    "address": "172.18.0.3:8080",
    "role": "STORAGE",
    "status": "ONLINE",
    "raftRole": "LEADER",
    "lastSeen": 1709923456789,
    "cpuUsage": 15.4,
    "memoryUsage": 245678912,
    "memoryMax": 1073741824
  },
  {
    "id": "jettra-store-2",
    "address": "172.18.0.4:8080",
    "role": "STORAGE",
    "status": "ONLINE",
    "raftRole": "FOLLOWER",
    "lastSeen": 1709923456790,
    "cpuUsage": 8.2,
    "memoryUsage": 198234567,
    "memoryMax": 1073741824
  }
]
```


## Administración de Bases de Datos

Gestión de bases de datos indicando el nombre y el tipo de almacenamiento (`storage`). Por defecto, todas las bases de datos son **Multi-modelo**.
### Change Password
```bash
curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob",
    "oldPassword": "password123",
    "newPassword": "newPassword456"
  }'
```

### Listar Bases de Datos
```bash
curl -s http://localhost:8081/api/db \
  -H "Authorization: Bearer $TOKEN"
```

### Crear Base de Datos (Persistent Multi-Model)
```bash
curl -X POST http://localhost:8081/api/db \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "sales_db", "storage": "STORE"}'
```

### Crear Colección (Document)
```bash
curl -X POST http://localhost:8081/api/db/sales_db/collections/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"engine": "Document"}'
```

### Crear Base de Datos (In-Memory Multi-Model)
```bash
curl -X POST http://localhost:8081/api/db \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "user_graph", "storage": "MEMORY"}'
```

### Multi-Raft Groups Information (Nuevo) ⭐
To view information about the Multi-Raft groups in the cluster:

```bash
curl -s http://localhost:8081/api/internal/pd/groups \
  -H "Authorization: Bearer $TOKEN"
```

### Renombrar Base de Datos
```bash
curl -X PUT http://localhost:8081/api/db/sales_db \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "sales_v2"}'
```

### Eliminar Base de Datos
```bash
curl -X DELETE http://localhost:8081/api/db/sales_db \
  -H "Authorization: Bearer $TOKEN"
```

### Consultar Información de la Base de Datos
Obtiene los metadatos y la configuración de una base de datos específica.

```bash
curl -s http://localhost:8081/api/db/sales_db \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

## Gestión de Colecciones
Operaciones para gestionar colecciones dentro de una base de datos específica.

### Listar Colecciones
```bash
curl -s http://localhost:8081/api/db/sales_db/collections \
  -H "Authorization: Bearer $TOKEN"
```

### Añadir Colección
```bash
curl -X POST http://localhost:8081/api/db/sales_db/collections/users \
  -H "Authorization: Bearer $TOKEN"
```

### Renombrar Colección
```bash
curl -X PUT http://localhost:8081/api/db/sales_db/collections/users/customers \
  -H "Authorization: Bearer $TOKEN"
```

### Eliminar Colección
```bash
curl -X DELETE http://localhost:8081/api/db/sales_db/collections/customers \
  -H "Authorization: Bearer $TOKEN"
```

## Registro Manual de Nodos (Interno)

Para registrar manualmente un nodo con información de recursos inicial:

**Endpoint:** `POST http://localhost:8080/api/internal/pd/register`

```bash
curl -X POST http://localhost:8080/api/internal/pd/register \
  -H "Content-Type: application/json" \
  -d '{
    "id": "manual-node-1",
    "address": "192.168.1.50:8080",
    "role": "STORAGE",
    "status": "ONLINE",
    "lastSeen": 0,
    "cpuUsage": 0.0,
    "memoryUsage": 0,
    "memoryMax": 0
  }'
```

### Index Management 🔍

#### Create Index
```bash
curl -X POST http://localhost:8080/api/internal/pd/databases/myDB/collections/myCollection/indexes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "field": "name",
    "type": "text"
  }'
```

#### List Indexes
```bash
curl -G http://localhost:8080/api/internal/pd/databases/myDB/collections/myCollection/indexes \
  -H "Authorization: Bearer $TOKEN"
```

#### Delete Index
```bash
curl -X DELETE http://localhost:8080/api/internal/pd/databases/myDB/collections/myCollection/indexes/myCollection_name_text \
  -H "Authorization: Bearer $TOKEN"
```

### Aggregations (Analytics) 📊

You can perform aggregations using SQL or the internal analytics engine endpoints.

```bash
# Example: Sum of amount
curl -X POST http://localhost:8081/api/v1/sql \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sql": "SELECT SUM(amount) FROM sales_db.orders"}'
```
Wait, the user asked for Mongo-like aggregation support. The shell translates it. Here we can document how to use the SQL endpoint or similar.
Actually, let's add a "Mongo-like Operations via cURL" section below.

## Monitoring Node Resources

## Monitoring Node Resources

You can monitor the resource usage (CPU, Memory) of all registered nodes:

```bash
# 1. Login to get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin", "password":"adminadmin"}' | jq -r .token)

# 2. Get Node Metrics (Request to Web Dashboard on port 8081)
curl -s http://localhost:8081/api/monitor/nodes \
  -H "Authorization: Bearer $TOKEN" | jq .
```
Response:
```json
[
  {
    "id": "jettra-store-1",
    "address": "jettra-store-1:8080",
    "role": "STORAGE",top
    "status": "ONLINE",
    "lastSeen": 1704321000,
    "cpuUsage": 12.5,
    "memoryUsage": 104857600,
    "memoryMax": 4294967296
  }
]
```

> [!IMPORTANT]
> La detención de nodos está restringida a usuarios con rol **admin**.

## Detener un Nodo

Para detener un nodo de forma segura, puedes usar el proxy de monitorización (puerto 8081) o directamente el PD (puerto 8080).

### Vía Proxy (Recomendado)
```bash
curl -X POST http://localhost:8081/api/monitor/nodes/jettra-store-3/stop \
  -H "Authorization: Bearer $TOKEN"
```

### Vía Placement Driver (Directo)
```bash
curl -X POST http://localhost:8080/api/internal/pd/nodes/jettra-store-3/stop \
  -H "Authorization: Bearer $TOKEN"
```

### Invocación Directa al Nodo (Nuevo) ⭐
Ahora cada nodo (PD, Store, Web) expone un endpoint `/stop` directo en la raíz para facilitar su detención.

```bash
# Detener Placement Driver
curl -X POST http://localhost:8080/stop -H "Authorization: Bearer $TOKEN"

# Detener Jettra Store
curl -X POST http://localhost:8082/stop -H "Authorization: Bearer $TOKEN"

# Detener Jettra Web
curl -X POST http://localhost:8081/stop -H "Authorization: Bearer $TOKEN"
```

## Gestión de Usuarios y Roles ⭐

> [!IMPORTANT]
> La gestión de usuarios y roles está restringida exclusivamente a usuarios con rol **admin**.

### 1. Crear un Rol
Define permisos para una base de datos específica o para todas (`_all`).
**Nota:** Se recomienda seguir la convención de nombres: `<tipo>_<base_de_datos>` (ej: `read_sales_db`, `read-write_logs`).

```bash
curl -X POST http://localhost:8081/api/web-auth/roles \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "read_sales_db",
    "database": "sales_db",
    "privileges": ["READ"]
  }'
```

### 2. Crear un Usuario
Asigna uno o varios roles al usuario.
JettraDB soporta 4 tipos de roles principales:
1. **super-user**: Solo para `admin`.
2. **admin**: Administrador de DB.
3. **read**: Solo lectura.
4. **read-write**: Lectura y Escritura.

```bash
curl -X POST http://localhost:8081/api/web-auth/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob",
    "password": "password123",
    "roles": ["read_sales_db", "read_all"],
    "forcePasswordChange": false
  }'
```

### 3. Editar un Usuario
Actualiza los roles o la contraseña de un usuario existente.

```bash
curl -X PUT http://localhost:8081/api/web-auth/users/bob \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob",
    "password": "newpassword456",
    "roles": ["admin_all"]
  }'
```

### 4. Listar Usuarios y Roles
```bash
# Listar todos los usuarios
curl -s http://localhost:8081/api/web-auth/users -H "Authorization: Bearer $TOKEN"

# Listar todos los roles
curl -s http://localhost:8081/api/web-auth/roles -H "Authorization: Bearer $TOKEN"
```

## Ejemplo Completo: Base de Datos y Colección (Document)
Este ejemplo muestra el flujo completo para crear una base de datos persistente y una colección usando el motor **Document** vía cURL.

```bash
# 1. Crear la base de datos (Persistent Multi-Model)
curl -X POST http://localhost:8081/api/db \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "mi_base_datos", "storage": "STORE"}'

# 2. Crear la colección con el motor Document
curl -X POST http://localhost:8081/api/db/mi_base_datos/collections/usuarios \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"engine": "Document"}'

## Operaciones de Documentos (Document Engine) ⭐

Una vez creada la colección con motor `Document`, puedes interactuar directamente con los documentos.

### 1. Guardar un Documento (Save / Upsert)
Si no se proporciona `jettraID`, el sistema lo generará automáticamente.
Nota: No se permiten documentos vacíos ni JSON vacíos `{}`.

```bash
# Se requiere hablar directamente con un nodo STORAGE (ej: puerto 8082)
curl -X POST http://localhost:8082/api/v1/document/usuarios \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Juan", "email": "juan@example.com", "_tags": ["premium"]}'
```

### 2. Recuperar un Documento por ID
```bash
# Reemplazar {jettraID} con el ID generado (ej: node1/default#uuid)
curl -s http://localhost:8082/api/v1/document/usuarios/{jettraID} \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Ver Historial de Versiones
Para ver todas las versiones guardadas de un documento:

```bash
curl -s http://localhost:8082/api/v1/document/usuarios/{jettraID}/versions \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Búsqueda por Etiquetas (Tags)
```bash
curl -s "http://localhost:8082/api/v1/document/usuarios/search/tag?tag=premium" \
  -H "Authorization: Bearer $TOKEN"
```

  -H "Authorization: Bearer $TOKEN"
```

## Mongo-like Operations (New) 🍃

Perform operations using MongoDB-like syntax. Note that currently `curl` interacts with the underlying HTTP APIs, so we map these operations to the Document API.

### insertOne / insertMany
To insert documents, perform a POST to the document endpoint.
```bash
# insertOne
curl -X POST http://localhost:8082/api/v1/document/usuarios \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice"}'

# insertMany (Batch insert is supported by sending a JSON Array)
curl -X POST http://localhost:8082/api/v1/document/usuarios/batch \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '[{"name": "Bob"}, {"name": "Charlie"}]'
```

### replaceOne / replaceMany
To replace (update) documents.
```bash
# replaceOne (Update by ID)
curl -X POST http://localhost:8082/api/v1/document/usuarios/{id} \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice Updated"}'
```

### deleteOne / deleteMany
```bash
# deleteOne (Delete by ID)
curl -X DELETE http://localhost:8082/api/v1/document/usuarios/{id} \
  -H "Authorization: Bearer $TOKEN"
```

### Aggregations (Mongo-Style) 📊
JettraDB supports complex aggregation pipelines via the Document API.

```bash
# General Aggregation Pipeline
curl -X POST http://localhost:8082/api/v1/document/usuarios/aggregate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '[
     {"$match": {"age": {"$gt": 20}}},
     {"$group": {"_id": "$city", "total": {"$sum": "$age"}}}
  ]'
```

#### Analytical Shortcuts (via SQL)
For high-level analytic functions, use the SQL endpoint:
```bash
curl -X POST http://localhost:8081/api/v1/sql \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sql": "SELECT AVG(age) FROM usuarios"}'
```

## Query Builder (Manual Guide) 🛠️
To construct a complex query via curl without an interactive tool:

1. **Identify Entpoint**: Decide if you are querying Documents (GET /api/v1/document) or using SQL (POST /api/v1/sql).
2. **Build JSON**:
   - For SQL: `{"sql": "SELECT * FROM col WHERE field='value'"}`
   - For Document Search: Use query params `?tag=label` or `?id=...`
3. **Execute**:
   ```bash
   curl -X POST http://localhost:8081/api/v1/sql -d '{"sql": "..."}' ...
   ```

## Consultas SQL (Nuevo) ⭐

JettraDB ahora soporta un subconjunto del lenguaje SQL para operar sobre los diversos motores. Las consultas se envían al Placement Driver (puerto 8081).

### Ejecutar una consulta SQL
Soporta `SELECT`, `INSERT`, `UPDATE` y `DELETE`.

```bash
# 1. SELECT (Obtener todos los documentos de una colección)
curl -X POST http://localhost:8081/api/v1/sql \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sql": "SELECT * FROM sales_db.orders"}'

# 2. INSERT (Insertar un nuevo documento)
curl -X POST http://localhost:8081/api/v1/sql \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sql": "INSERT INTO sales_db.orders VALUES ('order123', 'Laptop', 1200)"}'

# 3. UPDATE (Actualizar un documento por su ID)
curl -X POST http://localhost:8081/api/v1/sql \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sql": "UPDATE sales_db.orders SET precio=1300 WHERE id='order123'"}'

# 4. DELETE (Eliminar un documento por su ID)
curl -X POST http://localhost:8081/api/v1/sql \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sql": "DELETE FROM sales_db.orders WHERE id='order123'"}'
```

## Llaves Secuenciales (Sequences) ⭐

Permite crear y gestionar contadores persistentes en el cluster.

```bash
# 1. Crear secuencia
curl -X POST http://localhost:8081/api/v1/sequence \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "test_seq", "database": "db1", "startValue": 100, "increment": 1}'

# 2. Obtener siguiente valor
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/v1/sequence/test_seq/next

# 3. Obtener valor actual
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/v1/sequence/test_seq/current

# 4. Reiniciar secuencia
curl -X POST http://localhost:8081/api/v1/sequence/test_seq/reset \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"newValue": 500}'

# 5. Eliminar secuencia
curl -X DELETE http://localhost:8081/api/v1/sequence/test_seq -H "Authorization: Bearer $TOKEN"
```
## Resolución de Referencias (Resolve References) ⭐

Esta característica permite que JettraDB resuelva automáticamente las referencias entre documentos basadas en `jettraID` en una sola operación de lectura. Utiliza **acceso directo a la memoria** donde se encuentra el registro referenciado, evitando el uso de JOINS costosos y haciendo la búsqueda mucho más eficiente. Devolviendo el objeto completo en lugar de solo el ID.

### 1. Vía API de Documentos
Agrega el parámetro `resolveRefs=true` a la URL de consulta.

```bash
# Obtener un documento resolviendo sus referencias internas
curl -s "http://localhost:8082/api/v1/document/usuarios/{jettraID}?resolveRefs=true" \
  -H "Authorization: Bearer $TOKEN"

# Listar documentos con resolución
curl -s "http://localhost:8082/api/v1/document/usuarios?resolveRefs=true" \
  -H "Authorization: Bearer $TOKEN"

# Respuesta esperada (Ejemplo):
# {
#   "id": "node1/def#uuid1",
#   "name": "Aris",
#   "pais": {
#     "jettraID": "node1/def#uuid2",
#     "name": "Panama",
#     "codigo": "PA"
#   }
# }

```

### 2. Vía API SQL
Agrega el campo `resolveRefs: true` en el cuerpo del JSON.

```bash
curl -X POST http://localhost:8081/api/v1/sql \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT * FROM sales_db.orders",
    "resolveRefs": true
  }'
```

---
Este manual se actualiza periódicamente con las nuevas funciones del núcleo de JettraDB.
