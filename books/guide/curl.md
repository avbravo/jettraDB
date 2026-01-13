# Ejemplos de uso con cURL para JettraDB

Esta guía proporciona ejemplos de comandos `curl` para interactuar con la API de JettraDB, específicamente para la monitorización, autenticación y administración de bases de datos.

## Autenticación (Requerido)

Todas las peticiones a la API requieren un token JWT válido. Primero debes autenticarte para obtener el token que usarás en las cabeceras `Authorization`.

```bash
# 1. Login para obtener el token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"adminadmin"}' | jq -r .token)

echo "Token: $TOKEN"
```

## Monitorización de Nodos y Recursos

Para obtener la lista de nodos registrados y su consumo de recursos actual (CPU y Memoria):

```bash
curl -s http://localhost:8081/api/monitor/nodes \
  -H "Authorization: Bearer $TOKEN"
```

**Respuesta esperada (JSON):**

```json
[
  {
    "id": "jettra-store-1",
    "address": "jettra-store-1:8080",
    "role": "STORAGE",
    "status": "ONLINE",
    "lastSeen": 1709923456789,
    "cpuUsage": 15.4,
    "memoryUsage": 245678912,
    "memoryMax": 1073741824
  },
  {
    "id": "jettra-store-2",
    "address": "jettra-store-2:8080",
    "role": "STORAGE",
    "status": "ONLINE",
    "lastSeen": 1709923456790,
    "cpuUsage": 8.2,
    "memoryUsage": 198234567,
    "memoryMax": 1073741824
  }
]
```

## Administración de Bases de Datos

Gestión de bases de datos indicando el nombre y el tipo de almacenamiento (`storage`). Por defecto, todas las bases de datos son **Multi-modelo**.

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

### 1. Crear un Rol
Define permisos para una base de datos específica o para todas (`_all`).

```bash
curl -X POST http://localhost:8081/api/web-auth/roles \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "lector_ventas",
    "database": "sales_db",
    "privileges": ["READ"]
  }'
```

### 2. Crear un Usuario
Asigna uno o varios roles al usuario.

```bash
curl -X POST http://localhost:8081/api/web-auth/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob",
    "password": "password123",
    "roles": ["lector_ventas", "reader_all"],
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
```
