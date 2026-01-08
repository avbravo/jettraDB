# JettraDB Interactive Shell

The `jettra-shell` provides a command-line interface to interact with JettraDB clusters.

## Navigation
To start the shell, navigate to the `jettra-shell` directory and run:

```bash
mvn quarkus:dev
```

## Basic Commands

### 1. Connecting to a Cluster
The first step is always connecting to the Placement Driver (PD).

```bash
connect localhost:9000
```

### 2. Querying Data
You can execute SQL-like queries for the different engines.

```bash
# Insert into Document Engine
query "INSERT DOCUMENT INTO users {name: 'John', age: 30}"

# Query the Key-Value Engine
query "SET config 'max_connections' '500'"
query "GET config 'max_connections'"

# Analyze with Column Engine
query "AGGREGATE SUM(sales) FROM transactions"
```

### 3. Cluster Administration

```bash
# List all nodes in the cluster
query "SHOW NODES"

# List Raft Groups
query "SHOW GROUPS"
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
