# Interfaz Web de JettraDB

El Dashboard de JettraDB es una aplicación de una sola página (SPA) moderna, elegante y reactiva, diseñada para ofrecer una visibilidad total sobre el cluster cloud-native y permitir la interacción directa con los diversos motores de base de datos.

## Acceso al Dashboard
La interfaz web se despliega automáticamente con el componente `jettra-web`.
- **URL**: `http://localhost:8080`
- **Credenciales**: Por defecto, no requiere autenticación en entornos de desarrollo, aunque puede configurarse mediante Quarkus Security.

## Secciones y Funcionalidades

### 1. Panel de Control (Overview)
Es la vista general del sistema donde se muestran indicadores clave de rendimiento (KPIs):
- **Nodos del Cluster**: Conteo de nodos activos gestionados por el Placement Driver.
- **Grupos Raft**: Estado de salud de los grupos de consenso.
- **Latencia Global**: Tiempo de respuesta medio a través de todos los motores (Document, Graph, Vector, etc.).

### 2. Nodos del Cluster (Nodes)
Muestra una topología detallada de la red JettraDB:
- Identificación de nodos **Leader** y **Follower**.
- Estadísticas de carga por nodo.
- Visualización de la distribución de los grupos Raft.

### 3. Consola de Consultas (Query Console)
Permite ejecutar operaciones multi-modelo directamente desde el navegador:
1.  **Selección de Motor**: Conmuta entre Document, Key-Value, Graph, Columnar, Time-Series, Vector y Geospatial.
2.  **Editor JSON**: Entrada de comandos en formato JSON estandarizado de Jettra.
3.  **Ejecución Reactiva**: Las consultas se procesan de forma no bloqueante utilizando Mutiny.
4.  **Visor de Resultados**: Formateo automático de las respuestas JSON con resaltado de sintaxis.

### 4. Registros de Auditoría (Audit Logs)
Visualización del sistema de auditoría global inmutable:
- Seguimiento de transacciones distribuidas (2PC).
- Registro de acciones críticas (COMMIT/ABORT).
- Marcas de tiempo precisas para cumplimiento y debugging.

### 5. Alertas Predictivas y Métricas (Alerts) ⭐
Esta sección utiliza análisis de tendencias para anticipar problemas:
- **Alertas de Severidad**: Clasificación en *Critical*, *Warning* y *Predictive*.
- **Predicción de Almacenamiento**: Proyección del uso de disco para las próximas 24 horas.
- **Tendencias de Tráfico**: Visualización de aumentos o caídas repentinas en el Throughput (RPS).

## Consejos de Navegación
- **Cambio de Sección**: Utiliza el menú lateral izquierdo para saltar entre funcionalidades sin recargar la página.
- **Badge de Alertas**: El icono de notificaciones en el menú lateral te avisará si hay nuevas alertas predictivas de alta prioridad.
- **Respuesta Visual**: Los colores en el dashboard indican estados críticos: Verde (Saludable), Amarillo (Advertencia), Rojo (Crítico/Fallo).
