# JettraDB Java Driver Guide

The JettraDB Java Driver allows seamless integration with JettraDB clusters using reactive programming principles (Mutiny).

## Maven Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.jettra</groupId>
    <artifactId>jettra-driver-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Usage

### 1. Initialization
Initialize the client with the Placement Driver (PD) address and your Authentication Token.

```java
import io.jettra.driver.JettraReactiveClient;

// ...
String pdAddress = "localhost:8081"; 
String authToken = "eyJh... (your JWT token)"; // Obtain via Auth API

JettraReactiveClient client = new JettraReactiveClient(pdAddress, authToken);
```

### 2. Authentication Flow
To obtain a token programmatically, you can use the Auth API:

```java
// Example: Obtaining token via curl or dedicated AuthClient (if available)
// Use this token for all JettraReactiveClient operations.
```

### 3. Database Management
Create and manage multi-model databases, specifying the **Engine** and **Storage** style.

```java
// Create a new persistent Multi-Model database
client.createDatabase("sales_db", "STORE").await().indefinitely();

// Create a new in-memory Multi-Model database 
client.createDatabase("social_net", "MEMORY").await().indefinitely();

// Rename a database
client.renameDatabase("sales_db", "sales_v2").await().indefinitely();

// List databases
List<String> dbs = client.listDatabases().await().indefinitely();
System.out.println("Databases: " + dbs);

// Delete a database
client.deleteDatabase("sales_v2").await().indefinitely();

// Get detailed database metadata (returns JSON String)
String info = client.getDatabaseInfo("sales_v2").await().indefinitely();
System.out.println("Metadata: " + info);
```

### 4. Collection Management
Manage collections within a database.

```java
// List collections in a database
List<String> collections = client.listCollections("retail_db").await().indefinitely();

// Add a new collection (Engine: Document)
client.addCollection("retail_db", "orders", "Document").await().indefinitely();

// Add a new Graph collection
client.addCollection("retail_db", "friends", "Graph").await().indefinitely();

// Add a new Vector collection
client.addCollection("retail_db", "embeddings", "Vector").await().indefinitely();

// Rename a collection
client.renameCollection("retail_db", "orders", "customer_orders").await().indefinitely();

// Remove a collection
client.removeCollection("retail_db", "customer_orders").await().indefinitely();
```

### 5. Cluster Monitoring ⭐
Monitor node resource consumption (CPU, Memory) directly via the driver.

```java
import io.jettra.driver.NodeInfo;

// 1. List all cluster nodes
Uni<List<NodeInfo>> nodesUni = client.listNodes();
List<NodeInfo> nodes = nodesUni.await().indefinitely();

// 2. Iterate and display resource metrics
for (NodeInfo node : nodes) {
    double memUsedMB = node.memoryUsage() / 1024.0 / 1024.0;
    double memMaxMB = node.memoryMax() / 1024.0 / 1024.0;
    
    System.out.println("Node ID: " + node.id());
    System.out.println(" - Role: " + node.role() + " (" + node.raftRole() + ")");
    System.out.println(" - Status: " + node.status());
    System.out.println(" - CPU Usage: " + String.format("%.1f %%", node.cpuUsage()));
    System.out.println(" - Memory: " + String.format("%.1f / %.1f MB", memUsedMB, memMaxMB));
    System.out.println("-----------------------------------");
}

// 3. Stop a specific node (Sends a remote stop request via PD)
// NOTE: Only allowed for users with 'admin' role.
client.stopNode("jettra-store-2").await().indefinitely();

// Check Connection Info
System.out.println(client.connectionInfo());
```


### 6. User & Role Management ⭐
Manage cluster access control. 

> [!IMPORTANT]
> These methods are restricted to users with the **admin** role.

```java
// Create a role for a specific database
// Create a role for a specific database using standard prefixes
client.createRole("read_db1", "db1", Set.of("READ"))
      .await().indefinitely();

// Create a new user with specific roles
client.createUser("bob", "password123", Set.of("read_db1", "admin_all"))
      .await().indefinitely();

// Update an existing user (Edit roles/password)
client.updateUser("bob", "newpassword456", Set.of("read-write_all"))
      .await().indefinitely();

// List all users
List<String> usernames = client.listUsers().await().indefinitely();

// Delete a user
client.deleteUser("bob").await().indefinitely();
```

### 7. Document Operations (Document Engine) ⭐

Operaciones específicas para el motor de documentos, incluyendo versionamiento y gestión de IDs físicos.

```java
// 1. Generar un jettraID basado en la ubicación física (bucket)
// Formato: nodeId/bucketName#uuid
String jettraId = client.generateJettraId("node1/main-bucket").await().indefinitely();

// 2. Guardar un documento (JSON String o POJO)
// Si el documento ya existe, se crea una nueva versión automáticamente.
String json = "{\"nombre\": \"Alice\", \"_tags\": [\"vip\", \"2024\"]}";
client.save("usuarios", jettraId, json).await().indefinitely();

// 3. Recuperar la versión actual por jettraID
Object doc = client.findById("usuarios", jettraId).await().indefinitely();
System.out.println("Documento: " + doc);

// 4. Listar historial de versiones
List<String> versiones = client.getDocumentVersions("usuarios", jettraId).await().indefinitely();
System.out.println("Versiones: " + versiones);

// 5. Resolver una referencia (Document Linking)
// Útil para navegar entre documentos vinculados físicamente.
Object related = client.resolveReference("pedidos", "node2/orders#ref123").await().indefinitely();

// 6. Restaurar una versión
client.restoreVersion("usuarios", jettraId, "1").await().indefinitely();

// 7. Eliminar un documento
client.delete("usuarios", jettraId).await().indefinitely();
```

### 8. Consultas SQL (Nuevo) ⭐

Soporta la ejecución de sentencias SQL (`SELECT`, `INSERT`, `UPDATE`, `DELETE`) directamente. Las consultas son procesadas por el Placement Driver y enrutadas al motor correspondiente.

```java
// Ejecutar una consulta SQL
String result = client.executeSql("SELECT * FROM sales_db.orders").await().indefinitely();
System.out.println("Resultados: " + result);

// Insertar vía SQL
client.executeSql("INSERT INTO sales_db.orders VALUES ('order123', 'Laptop', 1200)").await().indefinitely();

// Actualizar vía SQL
client.executeSql("UPDATE sales_db.orders SET precio=1300 WHERE id='order123'").await().indefinitely();

// Eliminar vía SQL
client.executeSql("DELETE FROM sales_db.orders WHERE id='order123'").await().indefinitely();
```

### 9. Llaves Secuenciales (Sequences) ⭐

Gestión de contadores persistentes y secuenciales.

```java
// 1. Crear secuencia
client.createSequence("user_id_seq", "sales_db", 1000, 1).await().indefinitely();

// 2. Obtener siguiente valor
long nextId = client.nextSequenceValue("user_id_seq").await().indefinitely();

// 3. Obtener valor actual
long currentId = client.currentSequenceValue("user_id_seq").await().indefinitely();

// 4. Listar todas las secuencias de una base de datos
List<String> names = client.listSequences("sales_db").await().indefinitely();

// 5. Reiniciar secuencia
client.resetSequence("user_id_seq", 2000).await().indefinitely();

// 6. Eliminar secuencia
client.deleteSequence("user_id_seq").await().indefinitely();
```

### 10. Resolución de Referencias (Resolve References) ⭐

Permite obtener objetos referenciados completos de manera automática.

```java
// 1. Usando findById con resolución activa
// Devuelve el documento con todos sus JettraID internos resueltos a objetos completos
String jsonResult = (String) client.findById("usuarios", "node1/def#uuid1", true).await().indefinitely();

// 2. Usando SQL con resolución activa 
String sqlResult = client.executeSql("SELECT * FROM users", true).await().indefinitely();
```

---
The driver uses `Mutiny` (Uni/Multi) for non-blocking I/O.

## Ejemplo Completo: Base de Datos y Colección (Document)
Este ejemplo muestra el flujo completo para crear una base de datos persistente y una colección usando el motor **Document**.

```java
import io.jettra.driver.JettraReactiveClient;
import java.util.List;

public class DocumentExample {
    public static void main(String[] args) {
        String pdAddress = "localhost:8081";
        String token = "YOUR_TOKEN";
        
        JettraReactiveClient client = new JettraReactiveClient(pdAddress, token);

        // 1. Crear una base de datos persistente Multi-Modelo
        client.createDatabase("mi_base_datos", "STORE")
              .await().indefinitely();

        // 2. Añadir una colección con engine = "Document"
        client.addCollection("mi_base_datos", "usuarios", "Document")
              .await().indefinitely();

        System.out.println("Base de datos y Colección (Document) creadas con éxito!");
    }
}
```
