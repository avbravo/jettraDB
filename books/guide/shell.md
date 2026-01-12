# JettraDB Interactive Shell

The `jettra-shell` provides a command-line interface to interact with JettraDB clusters.

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
```bash
connect 192.168.1.50:8081
```

# Show Connection Info
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
Manage your databases directly from the shell, specifying the engine and storage type:

```bash
# Create a new persistent Document database (Default)
db create sales_db

# Create a new Graph database in memory
db create social_graph --engine Graph --storage MEMORY

# Options for --engine:
# Document, Column, Key-Value, Graph, Vector, Object, File

# Options for --storage:
# STORE (Persistent), MEMORY (In-Memory)

# List all databases
db list

# Delete a database
db delete sales_db
```

### 4. Querying Data (Legacy)
The `query` command provides low-level access to engine primitives:

```bash
query "INSERT DOCUMENT INTO users {name: 'John', age: 30}"
query "SET config 'max_connections' '500'"
```

### 5. Multi-Engine SQL Support 🚀
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

### 6. MongoDB-style Support 🍃
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

### 7. Cluster Administration & Monitoring ⭐
Monitor your cluster health and resource usage directly from the shell:

```bash
# List all nodes with CPU and Memory consumption
node list

# Output Example:
# ID              | Address            | Role       | Status   | CPU%   | Memory Usage    / Max Memory
# jettra-store-1  | jettra-store-1:8080| STORAGE    | ONLINE   | 4.2    | 120.5 MB        / 4096.0 MB
```

```bash
# List all nodes in the cluster (SQL legacy)
query "SHOW NODES"

# List Raft Groups
query "SHOW GROUPS"
```

### 8. Integrated Help System 📖
Each language support has its own detailed help and examples:

```bash
# General help
jettra> help

# Specialized help for SQL
jettra> sql --help

# Specialized help for MongoDB
jettra> mongo --help
```

## Advanced Features

### Auto-completion
The shell supports Tab-completion for commands and collection names.

### Reactivity
The shell is reactive. If a leader changes during your session, the shell automatically reconnects to the new leader via the PD metadata.

## Scripting Mode
You can pass a file with commands to the shell for automation:

```bash
./jettra-shell --file setup_db.jettra
```
