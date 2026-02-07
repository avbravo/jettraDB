# JettraDB Java SE Example (`jettra-example-se`)

Este proyecto es un ejemplo práctico de cómo utilizar el **JettraDB Java Driver** en una aplicación Java SE estándar para realizar operaciones de administración y monitoreo.

## Estructura del Proyecto

```text
jettra-example-se/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── io/jettra/example/
                └── Main.java
```

## Requisitos

- Java 21 o superior.
- Maven 3.8 o superior.
- Un cluster de JettraDB funcionando.

## Datos de Conexión

Para este ejemplo, se asumen los siguientes parámetros de conexión por defecto:

| Parámetro | Valor por defecto | Descripción |
| :--- | :--- | :--- |
| **PD/Proxy Address** | `localhost:8081` | Dirección del Web Dashboard o Placement Driver. |
| **Usuario** | `admin` | Usuario administrador por defecto. |
| **Contraseña** | `adminadmin` | Contraseña inicial (se recomienda cambiarla). |

## Configuración (`pom.xml`)

```xml
<dependency>
    <groupId>io.jettra</groupId>
    <artifactId>jettra-driver-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Ejemplo Completo (`Main.java`)

A continuación se presenta el código fuente completo del ejemplo. Este código incluye el flujo de **autenticación automática (Login)**, monitoreo del cluster y creación de recursos multi-modelo.

```java
package io.jettra.example;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jettra.driver.JettraReactiveClient;
import io.jettra.driver.NodeInfo;
import io.jettra.driver.Query;
import io.jettra.driver.annotation.Column;
import io.jettra.driver.annotation.Embedded;
import io.jettra.driver.annotation.Entity;
import io.jettra.driver.annotation.Id;
import io.jettra.driver.annotation.Referenced;
import io.jettra.driver.repository.JettraRepository;
import io.jettra.driver.repository.JettraRepositoryImpl;

public class Main {
    @Entity(collection = "users")
    public record UserProfile(
        @Id String id,
        @Column String name,
        @Column int age,
        @Column List<String> tags
    ) {}

    // 1. Embedded Example
    public record Address(
        String street,
        String city,
        String zipCode
    ) {}

    @Entity(collection = "customers")
    public record Customer(
        @Id String id,
        String name,
        @Embedded Address address
    ) {}

    // 2. Referenced Example
    @Entity(collection = "projects")
    public record Project(
        @Id String id,
        String projectName,
        double budget
    ) {}

    @Entity(collection = "employees")
    public record Employee(
        @Id String id,
        String name,
        @Referenced(collection = "projects") String projectId
    ) {}

    static {
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
    }
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        String pdAddress = "localhost:8081";

        try {
            LOG.info("Iniciando JettraDB Java SE Example...");

            // 1. Autenticación y obtención del Token
            LOG.info("Iniciando sesión para obtener el token...");
            JettraReactiveClient client = new JettraReactiveClient(pdAddress);
            client.login("super-user", "adminadmin").await().indefinitely();
            LOG.info("Login exitoso. Token autoconfigurado en el cliente.");

            // 2. Monitoreo del Cluster
            LOG.info("Consultando estado de los nodos y recursos...");
            List<NodeInfo> nodes = client.listNodes().await().indefinitely();
            System.out.println(
                    "\n---------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-15s | %-10s | %-10s | %-8s | %-6s | %-12s | %-12s\n", "ID", "Role", "Raft Role",
                    "Status", "CPU%", "Mem Usage", "Mem Max");
            System.out.println(
                    "---------------------------------------------------------------------------------------------------------------------------");
            for (NodeInfo node : nodes) {
                double memUsedMb = node.memoryUsage() / (1024.0 * 1024.0);
                double memMaxMb = node.memoryMax() / (1024.0 * 1024.0);
                System.out.printf("%-15s | %-10s | %-10s | %-8s | %-6.1f | %-10.1f MB | %-10.1f MB\n",
                        node.id(), node.role(), node.raftRole(), node.status(), node.cpuUsage(), memUsedMb, memMaxMb);
            }
            System.out.println(
                    "---------------------------------------------------------------------------------------------------------------------------\n");

            // Check Connection Info
            System.out.println("Connection Info: " + client.connectionInfo());

            // 2. Gestión de Bases de Datos
            String dbName = "shop";
            LOG.info("Creando base de datos '{}'...", dbName);
            client.createDatabase(dbName, "Persistent").await().indefinitely();

            // 3. Gestión de Colecciones
            String colName = "users";
            LOG.info("Añadiendo colección '{}' a la base de datos '{}'...", colName, dbName);
            client.addCollection(dbName, colName, "Document").await().indefinitely();

            // 4. Real Document Engine Operations
            LOG.info("Probando operaciones REALES del Document Engine...");

            // Generar un JettraID
            String jettraId = client.generateJettraId("main-bucket").await().indefinitely();
            LOG.info("Generated JettraID: {}", jettraId);

            // Guardar un documento
            String jsonDoc = "{\"name\": \"Bob\", \"age\": 30, \"_tags\": [\"active\", \"premium\"]}";
            client.save(colName, jettraId, jsonDoc).await().indefinitely();
            LOG.info("Document saved successfully!");

            // Buscar por ID
            Object doc = client.findById(colName, jettraId).await().indefinitely();
            LOG.info("Retrieved Document: {}", doc);

            // Actualizar documento (esto crea una nueva versión)
            String updatedJson = "{\"name\": \"Bob Hudson\", \"age\": 31, \"_tags\": [\"active\", \"premium\"]}";
            client.save(colName, jettraId, updatedJson).await().indefinitely();
            LOG.info("Document updated (New version created).");

            // Listar versiones
            List<String> versions = client.getDocumentVersions(colName, jettraId).await().indefinitely();
            LOG.info("Document Versions: {}", versions);

            // 5. Configuración de Seguridad
            String testUser = "bob";
            LOG.info("Creando usuario '{}'...", testUser);
            client.createUser(testUser, "password123", "bob@example.com", Set.of()).await().indefinitely();

            LOG.info("Configurando roles para el usuario '{}'...", testUser);
            client.createRole("reader-role", dbName, Set.of("READ")).await().indefinitely();
            client.updateUser(testUser, "password123", "bob@example.com", Set.of("reader-role")).await().indefinitely();

            // 6. Listar Información
            List<String> dbs = client.listDatabases().await().indefinitely();
            LOG.info("Bases de datos actuales: {}", dbs);

            LOG.info("Consultando metadatos de la base de datos: {}", dbName);
            String dbInfo = client.getDatabaseInfo(dbName).await().indefinitely();
            LOG.info("Metadata: {}", dbInfo);

            List<String> users = client.listUsers().await().indefinitely();
            LOG.info("Usuarios registrados: {}", users);

            List<String> cols = client.listCollections(dbName).await().indefinitely();
            LOG.info("Colecciones en {}: {}", dbName, cols);

            // 7. Multi-Engine SQL Support
            LOG.info("Probando consultas SQL unificadas...");

            // SELECT
            String sqlSelect = String.format("SELECT * FROM %s.%s", dbName, colName);
            String selectResult = client.executeSql(sqlSelect).await().indefinitely();
            LOG.info("SQL SELECT Result: {}", selectResult);

            // INSERT
            String sqlInsert = String.format("INSERT INTO %s.%s VALUES ('sql_id_1', 'SQL User', 25)", dbName, colName);
            client.executeSql(sqlInsert).await().indefinitely();
            LOG.info("SQL INSERT completed.");

            // UPDATE
            String sqlUpdate = String.format("UPDATE %s.%s SET age=26 WHERE id='sql_id_1'", dbName, colName);
            client.executeSql(sqlUpdate).await().indefinitely();
            LOG.info("SQL UPDATE completed.");

            // DELETE
            String sqlDelete = String.format("DELETE FROM %s.%s WHERE id='sql_id_1'", dbName, colName);
            client.executeSql(sqlDelete).await().indefinitely();
            LOG.info("SQL DELETE completed.");

            // 8. Resolve References Example
            LOG.info("Probando Resolve References (Direct Memory Access)...");
            // Assuming we have a linked document scenario
            // Insert linked doc
            String parentId = "parent_doc";
            String childId = client.generateJettraId("main-bucket").await().indefinitely();
            client.save(colName, childId, "{\"name\": \"Child\", \"type\": \"sub\"}").await().indefinitely();
            client.save(colName, parentId, String.format("{\"name\": \"Parent\", \"child\": \"%s\"}", childId)).await()
                    .indefinitely();

            // Fetch with resolution via Driver
            Object resolvedDoc = client.findById(colName, parentId, true).await().indefinitely();
            LOG.info("Resolved Document via Driver: {}", resolvedDoc);

            // Fetch with resolution via SQL
            String sqlResolved = client
                    .executeSql(String.format("SELECT * FROM %s.%s WHERE id='%s'", dbName, colName, parentId), true)
                    .await().indefinitely();
            LOG.info("Resolved Document via SQL: {}", sqlResolved);

            // 9. Sequential Keys (Sequences) Support ⭐
            LOG.info("Probando soporte de llaves secuenciales (Sequences)...");
            String seqName = "order_id_seq";

            // Crear una secuencia
            LOG.info("Creando secuencia: {}", seqName);
            client.createSequence(seqName, dbName, 100, 1).await().indefinitely();

            // Obtener el valor actual y el siguiente
            long currentVal = client.currentSequenceValue(seqName).await().indefinitely();
            LOG.info("Valor actual de la secuencia: {}", currentVal);

            long nextVal = client.nextSequenceValue(seqName).await().indefinitely();
            LOG.info("Siguiente valor de la secuencia: {}", nextVal);

            // Listar secuencias
            List<String> sequences = client.listSequences(dbName).await().indefinitely();
            LOG.info("Secuencias en {}: {}", dbName, sequences);

            // Reiniciar y eliminar
            client.resetSequence(seqName, 500).await().indefinitely();
            LOG.info("Secuencia reiniciada a 500. Nuevo valor siguiente: {}",
                    client.nextSequenceValue(seqName).await().indefinitely());

            client.deleteSequence(seqName).await().indefinitely();
            LOG.info("Secuencia eliminada.");

            // 10. Repository Pattern & Query Builder ⭐
            LOG.info("Probando JettraRepository y Query Builder...");
            JettraRepository<UserProfile, String> userRepo = new JettraRepositoryImpl<>(client, UserProfile.class);
            
            UserProfile profile = new UserProfile("repo_user_1", "Repository User", 29, List.of("dev", "java"));
            userRepo.save(profile).await().indefinitely();
            LOG.info("Profile saved via Repository!");

            // Query Builder
            Query query = Query.find()
                .from("users")
                .field("age").gt(25)
                .field("name").ne("Bob");
            
            LOG.info("Built Query JSON: {}", query.build());
            List<UserProfile> foundUsers = userRepo.findAll().await().indefinitely();
            LOG.info("Found {} users in repository.", foundUsers.size());

            // 11. Aggregations & Analytics ⭐
            LOG.info("Probando Agregaciones...");
            long count = userRepo.count().await().indefinitely();
            LOG.info("Total usuarios: {}", count);

            double avgAge = userRepo.avg("age").await().indefinitely();
            LOG.info("Edad promedio: {}", avgAge);

            String aggPipeline = "[{\"$match\": {\"age\": {\"$gt\": 20}}}, {\"$group\": {\"_id\": null, \"total\": {\"$sum\": \"$age\"}}}]";
            List<Object> aggResults = client.aggregate(colName, aggPipeline).await().indefinitely();
            LOG.info("Resultado Agregación Genérica: {}", aggResults);

            // 12. Clases Embebidas y Referenciadas ⭐
            LOG.info("Probando Clases Embebidas y Referenciadas...");
            
            // a. Embedded: El objeto se guarda dentro del mismo documento
            Address addr = new Address("Main St 123", "New York", "10001");
            Customer cust = new Customer("cust1", "Alice Smith", addr);
            new JettraRepositoryImpl<>(client, Customer.class).save(cust).await().indefinitely();

            // b. Referenced: Se guarda solo el ID y se puede resolver luego
            Project proj = new Project("proj_alpha", "Project Alpha", 50000);
            new JettraRepositoryImpl<>(client, Project.class).save(proj).await().indefinitely();
            
            Employee emp = new Employee("emp1", "John Doe", "proj_alpha");
            new JettraRepositoryImpl<>(client, Employee.class).save(emp).await().indefinitely();

            // Recuperar con resolución automática de referencias
            Object resolved = client.findById("employees", "emp1", true).await().indefinitely();
            LOG.info("Empleado con proyecto resuelto: " + resolved);

            LOG.info("Ejemplo completado con éxito!");

        } catch (Exception e) {
            LOG.error("Ocurrió un error durante la ejecución: {}", e.getMessage());
        }
    }
}
```

## Ejecución

Desde el directorio `jettra-example-se`:

```bash
mvn clean compile exec:java -Dexec.mainClass="io.jettra.example.Main"
```

## Características Documentadas

El archivo `Main.java` está organizado en secciones que demuestran las capacidades clave del driver:

1.  **Autenticación**: Uso de `client.login()` para obtener y configurar automáticamente el token JWT.
2.  **Monitoreo del Cluster**: Consulta de `NodeInfo` para obtener métricas de CPU, memoria y estado de Raft.
3.  **Gestión de Bases de Datos**: Creación de bases de datos persistentes.
4.  **Gestión de Colecciones**: Creación dinámica de colecciones para diferentes motores (Document, Graph, etc.).
5.  **Operaciones de Document Engine**: Generación de `JettraID`, guardado de JSON, búsqueda por ID y manejo de versiones (histórico de cambios).
6.  **Configuración de Seguridad**: Creación de usuarios y roles con permisos específicos (READ, WRITE, etc.).
7.  **Consultas SQL Unificadas**: Ejecución de comandos `SELECT`, `INSERT`, `UPDATE` y `DELETE` sobre el motor de documentos.
8.  **Resolve References (DMA)**: Recuperación de documentos relacionados en una sola llamada sin necesidad de joins, utilizando Direct Memory Access.
9.  **Secuencias (Sequences)**: Soporte para llaves secuenciales autoincrementales centralizadas.
10. **Repositorios (Repository Pattern)**: Uso de la interfaz `JettraRepository` para operaciones CRUD tipadas usando Java Records.
11. **Agregaciones & Analytics**: Métodos para `count`, `avg`, `sum` y ejecución de pipelines de agregación complejos.
12. **Modelos Embebidos y Referenciados**: Uso de las anotaciones `@Embedded` y `@Referenced` para definir relaciones entre entidades.

## Notas Técnicas

- **Mutiny**: El driver utiliza `SmallRye Mutiny`. Usamos `.await().indefinitely()` para convertir el flujo reactivo en síncrono para este ejemplo sencillo. En aplicaciones reales, se recomienda el uso de `subscribe()` o encadenamiento de tareas asíncronas.
- **Seguridad**: El método `client.login()` actualiza internamente la instancia del cliente con el token JWT obtenido, por lo que las llamadas subsiguientes estarán autorizadas automáticamente.
- **Logs**: El ejemplo utiliza SLF4J con JBoss LogManager para una salida limpia y profesional.

## Ejemplo de SQL Dedicado (`SqlExample.java`)

Para probar las funcionalidades de SQL específicamente, puede usar la clase `SqlExample`. Este ejemplo también demuestra la funcionalidad **Resolve References**, que permite acceder directamente a los objetos en memoria mediante su JettraID, evitando Joins costosos.

```java
package io.jettra.example;

import io.jettra.driver.JettraReactiveClient;

public class SqlExample {

    public static void main(String[] args) {
        String pdAddress = "localhost:8081";
        String token = System.getenv("JETTRA_TOKEN"); 
        
        if (token == null) {
            System.err.println("Please set JETTRA_TOKEN env variable.");
            System.exit(1);
        }

        JettraReactiveClient client = new JettraReactiveClient(pdAddress, token);

        System.out.println("=== SQL Example ===");

        // INSERT
        client.executeSql("INSERT INTO sales_db.products VALUES ('p1', 'Laptop', 1500)").await().indefinitely();

        // SELECT
        String inventory = client.executeSql("SELECT * FROM sales_db.products").await().indefinitely();
        System.out.println(inventory);
    }
}
```

Ejecución:

```bash
export JETTRA_TOKEN="<tu-token>"
mvn clean compile exec:java -Dexec.mainClass="io.jettra.example.SqlExample"
```

