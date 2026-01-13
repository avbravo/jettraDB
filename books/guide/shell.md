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
connect 192.168.1.50:8081
```

Once connected, you typically need to login:
```bash
login admin -p
# Enter password when prompted
```

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
login admin
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

### 8. MongoDB-style Support 🍃
JettraDB also understands MongoDB syntax for developers coming from document-oriented backgrounds.

```bash
# Querying
mongo db.users.find({age: {$gt: 21}})

# Inserting
mongo db.products.insert({name: 'Laptop', price: 1200})

# Updating
mongo db.inventory.update({id: '123'}, {$set: {stock: 10}})

# Aggregations
mongo db.orders.aggregate([{$group: {_id: '$status', total: {$sum: '$amount'}}}])
```

### 9. Cluster Administration & Monitoring ⭐
Monitor your cluster health and resource usage directly from the shell:

```bash
# List all nodes with CPU and Memory consumption
node list

# Output Example:
# ID              | Address            | Role       | Status   | CPU%   | Memory Usage    / Max Memory
# jettra-store-1  | jettra-store-1:8080| STORAGE    | ONLINE   | 4.2    | 120.5 MB        / 4096.0 MB
```
- `node list`: Shows CPU/Memory and Raft Role of each node.
- `node stop <node-id>`: Gracefully shuts down the specified node (e.g., `node stop jettra-store-2`).
- `node <node-id> stop`: Alternative syntax for shutdown (e.g., `node jettra-store-2 stop`).
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

**Allowed Role Types:**
While you can create custom roles with any name, the system optimized for the following predefined types:
- `admin`: Full access (ADMIN, READ, WRITE).
- `reader`: Read-only access.
- `writer-reader`: Read and Write access.
- `guest`: Read-only access (anonymous/restricted).

### 11. Integrated Help System 📖

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
