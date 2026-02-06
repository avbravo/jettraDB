# JettraDB Interactive Shell


For specific engine details, see:
- [Geospatial Engine Guide](jettra-engine-geospatial.md)


## Navigation
To start the shell in interactive mode, navigate to the `jettra-shell` directory and run:

```bash
mvn clean package
```

Para ejecutar el shell

```bash
 java -jar target/jettra-shell-1.0.0-SNAPSHOT.jar   

```

**Note:** `jettra-shell` is a pure console application and does not open any network ports for its execution.

You will enter the JettraDB REPL where you can type commands directly.

## Basic Commands

### 1.# Connect to remote cluster
### Finding Connection Details
To know which IP and port to connect to, check your Docker configuration:

1. List running containers:
   ```bash
   docker ps
   ```
2. Look for the `jettra-web` container and its port mapping (e.g., `0.0.0.0:8081->8080/tcp`).
3. Connect using that address (e.g., `localhost:8081`).

```bash
connect http://localhost:8081
```

Once connected, you typically need to login:
```bash
login super-user -p
# Enter password when prompted

Password: adminadmin
```
Password: adminadmin


# Show Connection Info (View current PD address and Auth status)
```bash
connect info
# Output:
# Current Connection Info:
#   PD/Web Address: localhost:8081
#   Auth Token: None (Logged Out)
```
The first step is always connecting to the Placement Driver (PD).

```bash
connect localhost:9000
```

### 2. Authentication
Before performing operations, you must log in:

```bash
login super-user
# Password prompt will appear
```

### 3. Database Management
Manage your databases directly from the shell, specifying the storage type. All databases are Multi-Model containers:

```bash
# Create a new persistent Multi-Model database (Default)
db create sales_db

# Create a new Multi-Model database in memory
db create social_db --storage MEMORY

# Rename a database
db rename sales_db retail_db

# Options for --storage:
# STORE (Persistent), MEMORY (In-Memory)

# List all databases
db list # legacy
show dbs # improved

# Delete a database
db delete retail_db

# Show detailed information for a database (aliases: info, database info)
db info retail_db
```

### 4. Navigation & Context
JettraDB shell now supports context switching and detailed metadata inspection:

```bash
# List all available databases
show dbs

# Switch to a target database context
use retail_db

# Show collections in the current database
show collections

# Show detailed information for a database
info retail_db
```

### 5. Collection Management
Manage collections within your current database context, specifying the specialized engine:

```bash
# Add a new Document collection (Default)
collection add users

# Add a new Graph collection
collection add friends --engine Graph

# Add a new Vector collection for search
collection add product_vectors --engine Vector

# Rename a collection
collection rename users customers

# Delete a collection
collection delete logs
```

### 6. Querying Data (Legacy)
The `query` command provides low-level access to engine primitives:

```bash
query "INSERT DOCUMENT INTO users {name: 'John', age: 30}"
query "SET config 'max_connections' '500'"
```

### 7. Multi-Engine SQL Support 🚀
JettraDB now supports a unified SQL interface that automatically routes operations to the specialized engines:

```bash
# Document Engine
sql SELECT * FROM users_collection WHERE age > 21

# Graph Engine (Traversals internally)
sql SELECT * FROM friends_graph WHERE name = 'Alice'

# Vector Engine (Similarity search internally)
sql SELECT * FROM image_vector_index LIMIT 5

# Persistence
sql INSERT INTO users VALUES ('id_1', 'John Doe')
sql UPDATE analytics SET processed=true WHERE id='node_2'
sql DELETE FROM logs WHERE date < '2023-01-01'
```

### 8. MongoDB-style Support (REAL) 🍃
JettraDB entiende la sintaxis de MongoDB y ahora la traduce internamente a llamadas al **Document Engine** para una persistencia real con versionamiento.

```bash
# Inserción (Genera automáticamente jettraID y Versión 1)
# Nota: No se permiten documentos vacíos ni JSON vacíos '{}'.
mongo db.usuarios.insert({nombre: 'Alice', _tags: ['vip']})

# Consulta por ID (Simplificado)
mongo db.usuarios.find('node1/default#uuid123')

# Consulta por Filtro ID
mongo db.usuarios.find({id: 'node1/default#uuid123'})

# Actualización (Incrementa el número de versión)
mongo db.usuarios.update({id: 'node1/default#uuid123'}, {nombre: 'Alice Cooper'})

# Eliminación
mongo db.usuarios.remove({id: 'node1/default#uuid123'})

# Restaurar Versión
restore usuarios node1/default#uuid123 1
```

### 9. Cluster Administration & Monitoring ⭐
Monitor your cluster health and resource usage directly from the shell. This is critical for maintaining high availability and identifying performance bottlenecks.

```bash
# List all nodes with CPU and Memory consumption
node list

# Output Example:
# Node Resources Monitoring:
# --------------------------------------------------------------------------------------------------------------------------
# ID              | Address            | Role       | Raft Role  | Status   | CPU%   | Memory Usage    / Max Memory     
# --------------------------------------------------------------------------------------------------------------------------
# jettra-store-1  | 172.18.0.3:8080    | STORAGE    | LEADER     | ONLINE   | 4.5    | 156.2 MB        / 4096.0 MB      
# jettra-store-2  | 172.18.0.4:8080    | STORAGE    | FOLLOWER   | ONLINE   | 2.1    | 120.8 MB        / 4096.0 MB      
# jettra-store-3  | 172.18.0.5:8080    | STORAGE    | FOLLOWER   | ONLINE   | 1.8    | 115.5 MB        / 4096.0 MB      
# --------------------------------------------------------------------------------------------------------------------------
```
*   **CPU%**: Porcentaje de uso de CPU del proceso del nodo.
*   **Memory Usage**: Memoria RAM actualmente utilizada por la JVM del nodo.
*   **Status**: `ONLINE` o `OFFLINE` basado en los heartbeats recibidos por el Placement Driver.

**Control de Nodos:**
*   `node list`: Muestra CPU/Memoria y Raft Role de cada nodo.
*   `node stop <node-id>`: Apaga de forma segura el nodo especificado.
*   `node <node-id> stop`: Sintaxis alternativa para el apagado.

- `database list`: List all multi-model databases.

```bash
# List all nodes in the cluster (SQL legacy)
query "SHOW NODES"

# List Raft Groups
query "SHOW GROUPS"
```

### 10. User & Role Management ⭐
Manage users and their permissions across the cluster. 

> [!IMPORTANT]
> User and Role management commands are restricted to users with the **admin** role.

```bash
# Create a new role for a specific database
role create reader_db1 db1 READ

# Create a new role with multiple privileges
role create writer_db1 db1 READ,WRITE

# List all existing roles
role list

# Create a new user and assign roles
# Note: Roles must be comma-separated
user create bob password123 reader_db1,writer_db2

# Edit an existing user (e.g., change roles or password)
user edit bob newpassword456 reader_db1,admin_all

# List all users
user list

# Delete a user
user delete bob
```

**Tipos de Roles Permitidos:**
JettraDB implementa una jerarquía estricta de 4 roles principales:
- `super-user`: Rol único del usuario `admin`. Acceso total global. Inmutable.
- `admin`: Administrador de base de datos (CREATE, DROP, GRANT).
- `read`: Solo lectura (SELECT/GET).
- `read-write`: Lectura y Escritura (INSERT, UPDATE, DELETE), sin capacidades administrativas.

### 11. Sequential Keys (Sequences) 🔑

Manage persistent counters for generating sequential IDs at the database level. Sequences are cluster-wide but can be filtered by database context.

```bash
# Context Awareness: Use 'use' to target a database for sequence creation and listing
use sales_db

# Create a sequence (auto-associates with 'sales_db')
sequence create user_id_seq --start 1000 --inc 1

# Get next/current value
sequence next user_id_seq
sequence current user_id_seq

# Management
sequence list                 # Lists sequences for the current database context
sequence list --database db2  # Override database filtering
sequence reset user_id_seq 0
sequence delete user_id_seq
```

#### Associating Sequences with Collections
While sequences are database-level objects, they are primarily used to provide unique IDs for Document collections. 

**Best Practice:** Use a naming convention that includes the collection name (e.g., `orders_seq` for an `orders` collection).

**Example Workflow:**
1. Generate the next sequential ID:
   ```bash
   sequence next user_id_seq
   # Output: 1001
   ```
2. Insert a document using that ID:
   ```bash
   mongo db.users.insert({id: 1001, name: 'John Doe'})
   ```

   **Note on Association**: JettraDB sequences are standalone objects. "Associating" them with a collection is a logical convention maintained by your application logic (e.g., always using `orders_seq` for the `orders` collection). The database does not enforce this relationship.

### 12. Resolve References (Direct Memory Access) 🚀

JettraDB allows you to automatically resolve references between documents without using JOINs. When a field contains a `jettraID`, JettraDB can fetch the full object in a single operation.

```bash
# Using SQL with resolution
sql SELECT * FROM users --resolve-refs

# Output Example:
# {
#   "id": "node1/def#uuid1",
#   "name": "Jane",
#   "profile": {
#      "jettraID": "node1/def#profile1",
#      "bio": "Software Engineer"
#   }
# }


# Using MongoDB syntax with resolution
mongo db.users.find('node1/default#u123') --resolve-refs
```

This feature uses the internal `idToCollection` index to perform direct memory access to the referenced objects, making it extremely efficient for normalized data models.

### 13. Integrated Help System 📖

## Advanced Features

### Auto-completion
The shell supports Tab-completion for commands and collection names.

### Reactivity
The shell is reactive. If a leader changes during your session, the shell automatically reconnects to the new leader via the PD metadata.

## Scripting Mode
You can pass a file with commands to the shell for automation:

## Monitoring via API (CURL)

You can also monitor the cluster status using standard HTTP tools like `curl`. This is useful for integration with external monitoring systems.

### 1. List Nodes
```bash
curl -H "Authorization: Bearer <your-token>" \
     http://localhost:8081/api/monitor/nodes
```

### 2. List Raft Groups
```bash
curl -H "Authorization: Bearer <your-token>" \
     http://localhost:8081/api/monitor/groups
```bash
curl -X GET http://localhost:8080/api/monitor/groups -H "Authorization: Bearer <token>"
```

### Stopping a Node
To gracefully shut down a node via API Proxy (Recommended):

```bash
curl -X POST http://localhost:8081/api/monitor/nodes/<node-id>/stop -H "Authorization: Bearer <token>"
```

Or directly via the new root endpoint on any node:

```bash
curl -X POST http://localhost:<port>/stop -H "Authorization: Bearer <token>"
```

Or directly via PD (legacy):

```bash
curl -X POST http://localhost:8080/api/internal/pd/nodes/<node-id>/stop -H "Authorization: Bearer <token>"
```

### 3. Health Check
```bash
curl http://localhost:8080/api/internal/pd/health
```
