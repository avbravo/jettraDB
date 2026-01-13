package io.jettra.example;

import io.jettra.driver.JettraReactiveClient;
import io.jettra.driver.NodeInfo;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        String pdAddress = "localhost:8081"; 
        String username = "admin";
        String password = "adminadmin"; // Reemplazar con su contraseña real

        LOG.info("Iniciando JettraDB Java SE Example...");

        // 0. Inicializar cliente sin token y realizar Login
        JettraReactiveClient client = new JettraReactiveClient(pdAddress);

        try {
            LOG.info("Autenticando usuario: " + username);
            String token = client.login(username, password).await().indefinitely();
            LOG.info("Login exitoso. Token obtenido.");

            // 1. Monitoreo del Cluster
            LOG.info("Listando nodos del cluster...");
            List<NodeInfo> nodes = client.listNodes().await().indefinitely();
            if (nodes.isEmpty()) {
                LOG.warning("No se encontraron nodos activos.");
            } else {
                for (NodeInfo node : nodes) {
                    System.out.printf(" - Nodo ID: %s, Rol: %s, Estado: %s, CPU: %.1f%%%n", 
                                      node.id(), node.role(), node.status(), node.cpuUsage());
                }
            }

            // 2. Creación de Base de Datos
            String dbName = "demo_se_db";
            LOG.info("Creando base de datos: " + dbName);
            client.createDatabase(dbName, "STORE").await().indefinitely();

            // 3. Creación de Colección con motor Document
            String docCol = "clientes";
            LOG.info("Añadiendo colección con motor Document: " + docCol);
            client.addCollection(dbName, docCol, "Document").await().indefinitely();

            // 4. Creación de Colección con motor Graph
            String graphCol = "seguidores";
            LOG.info("Añadiendo colección con motor Graph: " + graphCol);
            client.addCollection(dbName, graphCol, "Graph").await().indefinitely();

            // 5. Gestión de Seguridad (Roles y Usuarios)
            LOG.info("Configurando seguridad...");
            
            // 5.1 Crear un rol específico para la base de datos demo
            String roleName = "lector_demo";
            LOG.info("Creando rol: " + roleName);
            client.createRole(roleName, dbName, Set.of("READ")).await().indefinitely();

            // 5.2 Crear un nuevo usuario con este rol
            String newUsername = "developer_se";
            LOG.info("Creando nuevo usuario: " + newUsername);
            client.createUser(newUsername, "secret123", Set.of(roleName)).await().indefinitely();

            // 5.3 Editar el usuario (por ejemplo, añadir rol admin global)
            LOG.info("Actualizando roles del usuario...");
            client.updateUser(newUsername, null, Set.of(roleName, "admin")).await().indefinitely();

            // 6. Listar recursos para verificar
            List<String> dbs = client.listDatabases().await().indefinitely();
            LOG.info("Bases de datos actuales: " + dbs);

            List<String> users = client.listUsers().await().indefinitely();
            LOG.info("Usuarios registrados: " + users);

            List<String> cols = client.listCollections(dbName).await().indefinitely();
            LOG.info("Colecciones en " + dbName + ": " + cols);

            LOG.info("Ejemplo completado con éxito!");

        } catch (Exception e) {
            LOG.severe("Ocurrió un error durante la ejecución: " + e.getMessage());
            // No imprimimos el stacktrace completo para mantener la salida limpia en el ejemplo
        }
    }
}
