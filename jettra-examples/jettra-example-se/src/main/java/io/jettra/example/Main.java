package io.jettra.example;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jettra.driver.JettraReactiveClient;
import io.jettra.driver.NodeInfo;

public class Main {
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

            // 8. Sequential Keys (Sequences) Support ⭐
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

            LOG.info("Ejemplo completado con éxito!");

        } catch (Exception e) {
            LOG.error("Ocurrió un error durante la ejecución: {}", e.getMessage());
        }
    }
}
