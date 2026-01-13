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

import io.jettra.driver.JettraReactiveClient;
import io.jettra.driver.NodeInfo;
import java.util.List;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Datos de conexión
        String pdAddress = "localhost:8081"; 
        String username = "admin";
        String password = "adminadmin"; 

        LOG.info("Iniciando JettraDB Java SE Example...");

        // 0. Inicializar cliente sin token inicial
        JettraReactiveClient client = new JettraReactiveClient(pdAddress);

        try {
            // 0.1 Realizar Login programático para obtener el token JWT
            LOG.info("Autenticando usuario: " + username);
            String token = client.login(username, password).await().indefinitely();
            LOG.info("Login exitoso. Token obtenido y almacenado en el cliente.");

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

            // 5. Inspección de recursos
            List<String> dbs = client.listDatabases().await().indefinitely();
            LOG.info("Bases de datos actuales: " + dbs);

            List<String> cols = client.listCollections(dbName).await().indefinitely();
            LOG.info("Colecciones en " + dbName + ": " + cols);

            LOG.info("Ejemplo completado con éxito!");

        } catch (Exception e) {
            LOG.severe("Ocurrió un error: " + e.getMessage());
        }
    }
}
```

## Ejecución

Desde el directorio `jettra-example-se`:

```bash
mvn clean compile exec:java -Dexec.mainClass="io.jettra.example.Main"
```

## Notas Técnicas

- **Mutiny**: El driver utiliza `SmallRye Mutiny`. Usamos `.await().indefinitely()` para convertir el flujo reactivo en síncrono para este ejemplo sencillo. En aplicaciones reales, se recomienda el uso de `subscribe()` o encadenamiento de tareas asíncronas.
- **Seguridad**: El método `client.login()` actualiza internamente la instancia del cliente con el token JWT obtenido, por lo que las llamadas subsiguientes estarán autorizadas automáticamente.
