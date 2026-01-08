# Ejemplos de uso con cURL para JettraDB

Esta guía proporciona ejemplos de comandos `curl` para interactuar con la API de JettraDB, específicamente para la monitorización y el registro de nodos.

## Autenticación (Requerido)

Todas las peticiones a la API requieren un token JWT válido. Primero debes autenticarte:

```bash
# 1. Login para obtener el token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"adminadmin"}' | jq -r .token)

echo "Token: $TOKEN"
```

## Monitorización de Nodos

Para obtener la lista de nodos registrados (requiere Auth):

```bash
curl -v http://localhost:8081/api/monitor/nodes \
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
    "lastSeen": 1709923456789
  },
  {
    "id": "jettra-store-2",
    "address": "jettra-store-2:8080",
    "role": "STORAGE",
    "status": "ONLINE",
    "lastSeen": 1709923456790
  }
]
```

## Registro Manual de Nodos (Interno)

Aunque los nodos de almacenamiento se registran automáticamente al iniciarse, puedes registrar o actualizar manualmente un nodo en el `jettra-pd` (Placement Driver) para pruebas o depuración.

**Endpoint:** `POST http://localhost:8080/api/internal/pd/register` (Puerto del contenedor `jettra-pd`)

```bash
curl -X POST http://localhost:8080/api/internal/pd/register \
  -H "Content-Type: application/json" \
  -d '{
    "id": "manual-node-1",
    "address": "192.168.1.50:8080",
    "role": "STORAGE",
    "status": "ONLINE",
    "lastSeen": 0
  }'
```

## Verificar Nodos en el PD (Interno)

Puedes consultar directamente al Placement Driver para ver qué nodos tiene registrados en su memoria:

```bash
curl http://localhost:8080/api/internal/pd/nodes \
  -H "Authorization: Bearer $TOKEN"
```

## Administración de Bases de Datos

Gestión de bases de datos mediante la API del Placement Driver (PD):

### Listar Bases de Datos
```bash
curl http://localhost:8080/api/internal/pd/databases \
  -H "Authorization: Bearer $TOKEN"
```

### Crear Base de Datos
```bash
curl -X POST http://localhost:8080/api/internal/pd/databases \
  -H "Authorization: Bearer $TOKEN" \
  -d "my_new_db"
```

### Eliminar Base de Datos
```bash
curl -X DELETE http://localhost:8080/api/internal/pd/databases/my_new_db \
  -H "Authorization: Bearer $TOKEN"
```
