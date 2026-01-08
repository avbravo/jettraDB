# Jettra Shell

An interactive Command Line Interface (CLI) to manage JettraDB and execute queries.

## Usage
Connect to a Jettra cluster:
```bash
java -jar jettra-shell.jar --server http://localhost:9000
```

## Commands
*   `use <database>`
*   `find <collection> { ... }`
*   `insert <collection> { ... }`
*   `delete <collection> <id>`
