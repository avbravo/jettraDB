# Monitoreo y Alertas Predictivas

JettraDB no solo monitoriza el estado actual del cluster, sino que utiliza algoritmos de tendencia para predecir posibles fallos o cuellos de botella antes de que ocurran.

## Monitoreo de Recursos en Tiempo Real ⭐
El sistema ahora permite una inspección profunda y granular de cada componente del cluster. A través del Dashboard Web, los administradores pueden visualizar el consumo exacto de recursos:

1.  **Navegación**: Dirígete a la sección **Nodes** en el menú lateral.
2.  **Inspección**: Haz clic en el botón **🔍 View Resources** dentro de la tarjeta de cualquier nodo (Storage, Memory, etc.).
3.  **Métricas Detalladas**:
    -   **CPU Usage**: Visualización mediante barra de progreso del porcentaje de carga de CPU actual del proceso.
    -   **RAM Usage**: Consumo de memoria RAM en Megabytes vs el límite máximo configurado.
    -   **Last Heartbeat**: Monitoreo de la frescura de la señal del nodo para detectar "zombie nodes".

## Centro de Alertas
En la interfaz web, la sección **Alertas & Métricas** centraliza todas las notificaciones críticas. El sistema clasifica las alertas en tres niveles de severidad:

### Niveles de Severidad
- 🔴 **CRITICAL (Alta)**: Requiere acción inmediata (ej. Nodo con >85% de disco). El sistema podría comenzar a rechazar escrituras pronto.
- 🟡 **WARNING (Media)**: Desviación detectada (ej. Latencia de replicación Raft >100ms).
- 🔵 **PREDICTIVE (Predictiva)**: Basada en tendencias de carga. Te avisa con antelación si el CPU o la Memoria excederán los umbrales en las próximas horas.

## Métricas Clave
El dashboard visualiza tendencias de salud del cluster:
1.  **Predicted Disk Usage**: Proyección del uso de almacenamiento para las próximas 24-48 horas basada en el ritmo de ingestión actual.
2.  **Throughput Trend**: Comparativa del rendimiento (RPS - Requests Per Second) respecto a la última hora.

## Cómo Responder a una Alerta
- **Alerta de Almacenamiento**: Considera añadir nuevos nodos de almacenamiento al cluster usando Docker Compose y deja que el Placement Driver reequilibre los datos.
- **Alerta de Latencia**: Revisa la conectividad de red entre los nodos del grupo Raft afectado.
- **Alerta Predictiva de CPU**: Es el momento ideal para escalar horizontalmente la capa de motores (Engines).

### 🐚 Vía Shell
Ejecuta el comando `node list` para ver una tabla comparativa de recursos en tiempo real. Este comando extrae datos directamente del Placement Driver, mostrando la carga de CPU y el consumo de memoria JVM de cada nodo.

```bash
node list
```

**Ejemplo de Salida:**
```text
Node Resources Monitoring:
--------------------------------------------------------------------------------------------------------------------------
ID              | Address            | Role       | Raft Role  | Status   | CPU%   | Memory Usage    / Max Memory     
--------------------------------------------------------------------------------------------------------------------------
jettra-store-1  | 172.18.0.3:8080    | STORAGE    | LEADER     | ONLINE   | 4.5    | 156.2 MB        / 4096.0 MB      
jettra-store-2  | 172.18.0.4:8080    | STORAGE    | FOLLOWER   | ONLINE   | 2.1    | 120.8 MB        / 4096.0 MB      
jettra-store-3  | 172.18.0.5:8080    | STORAGE    | FOLLOWER   | ONLINE   | 1.8    | 115.5 MB        / 4096.0 MB      
--------------------------------------------------------------------------------------------------------------------------
```

### 🌐 Vía cURL (API REST)
Consulta el endpoint de monitorización unificado en el puerto del Dashboard Web (8081). Este endpoint devuelve un JSON con las métricas crudas de todos los nodos.

```bash
# 1. Obtener Token
TOKEN=$(curl -s -X POST http://localhost:8081/api/web-auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"super-user","password":"adminadmin"}' | jq -r .token)

# 2. Consultar Recursos
curl -s http://localhost:8081/api/monitor/nodes \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### ☕ Vía Java Driver
Utiliza el método `listNodes()` de `JettraClient` para obtener objetos `NodeInfo` que contienen todas las métricas de recursos.

```java
JettraReactiveClient client = new JettraReactiveClient("localhost:8081", token);

client.listNodes().subscribe().with(nodes -> {
    for (NodeInfo node : nodes) {
        System.out.printf("Node: %s | CPU: %.1f%% | Mem: %.1f MB / %.1f MB\n",
            node.id(), 
            node.cpuUsage(), 
            node.memoryUsage() / 1024.0 / 1024.0, 
            node.memoryMax() / 1024.0 / 1024.0);
    }
});
```

## Ejemplo: Escalamiento Dinámico
Si detectas que los nodos de almacenamiento están llegando a su límite de recursos, puedes escalar horizontalmente el cluster:

```bash
docker-compose up -d --scale jettra-store=5
```

## Configuración de Umbrales
Los umbrales de alerta se pueden configurar en el archivo `application.properties` o `config.json` del Placement Driver (PD), permitiendo personalizar la sensibilidad del sistema predictivo según el entorno (Dev, Stage, Prod).

