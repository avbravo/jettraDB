# Interfaz Web de JettraDB

El Dashboard de JettraDB es una aplicación de una sola página (SPA) moderna, elegante y reactiva, diseñada para ofrecer una visibilidad total sobre el cluster cloud-native y permitir la interacción directa con los diversos motores de base de datos.

## Acceso al Dashboard
La interfaz web se despliega automáticamente con el componente `jettra-web`.
- **URL**: `http://localhost:8081` (Puerto por defecto del componente web)
- **Credenciales**: **Requerido**. El acceso está protegido por JWT.
  - Usuario por defecto: `admin`
  - Contraseña por defecto: `adminadmin` (Cambio obligatorio al primer inicio).

## Secciones y Funcionalidades

### 1. Panel de Control (Overview)
Es la vista general del sistema donde se muestran indicadores clave de rendimiento (KPIs):
- **Nodos del Cluster**: Conteo de nodos activos gestionados por el Placement Driver.
- **Grupos Raft**: Estado de salud de los grupos de consenso y elección de líderes.

### 2. Nodos del Cluster (Nodes)
Muestra una topología detallada de la red JettraDB en tiempo real:
- **Descubrimiento Automático**: Los nodos se registran automáticamente con el Placement Driver al iniciarse.
- **Estado en Vivo**: Indicadores visuales (Verde/Rojo) para el estado ONLINE/OFFLINE de cada nodo.
- **Roles**: Identificación de nodos **Storage** y su dirección de red.
- **Monitorización de Recursos** ⭐: Al dar clic en el botón de búsqueda/lupa del nodo, se abre un diálogo modal que muestra:
    - **Uso de CPU**: Porcentaje de carga del procesador del nodo.
    - **Uso de Memoria**: Memoria RAM consumida vs Memoria RAM disponible.
    - **Latencia de Señal**: Tiempo transcurrido desde el último latido (heartbeat).

### 3. Administración de Bases de Datos (Database Management)
Gestión completa del ciclo de vida de las bases de datos:
- **Explorador de Datos (Sidebar Tree)** ⭐: Un nuevo árbol interactivo en el menú izquierdo que permite:
    - **Navegación Visual**: Listado en tiempo real de todas las bases de datos.
    - **Creación Rápida**: Botón "+" directamente en el encabezado del árbol para abrir el formulario de creación.
    - **Estructura Multi-modelo**: Cada base de datos muestra sub-nodos para sus motores integrados (**Document, Column, Graph, Vector, Object, Files**).
    - **Barras de Opciones Rápidas** ⭐: Al hacer clic en un tipo de motor (ej: Document), se despliega una barra con botones para:
        - **Añadir**: Insertar nuevos documentos, registros, vértices, etc.
        - **Índices**: Gestionar índices específicos para ese modelo de datos.
        - **Reglas**: Configurar reglas de validación y seguridad.
- **Crear**: Provisiona nuevas bases de datos lógicas instantáneamente. Se debe seleccionar:
    - **Nombre**: Identificador único de la base de datos.
    - **Multi-modelo**: Permite crear dentro de ellas tipos Document, Column, Graph, Vector, Object y File de manera integrada.
    - **Tipo de Almacenamiento (Storage)**:
        - **Persistent (Store)**: Los datos se guardan en disco (jettra-store).
        - **In-Memory**: Los datos residen exclusivamente en RAM para baja latencia.
- **Listar**: Visualiza todas las bases de datos registradas indicando su **Motor** y tipo de **Almacenamiento**.
- **Eliminar**: Borra bases de datos y todos sus datos asociados tras confirmación.

### 4. Consola de Consultas (Query Console)
Permite ejecutar operaciones multi-modelo directamente desde el navegador:
1.  **Selección de Motor**: Conmuta entre Document, Key-Value, Graph, Columnar, Time-Series, Vector y Geospatial.
2.  **Editor JSON**: Entrada de comandos en formato JSON estandarizado de Jettra.
3.  **Ejecución Reactiva**: Las consultas se procesan de forma no bloqueante.

## Procedimiento de Autenticación en la Web

1. Al acceder a la URL, el sistema redirigirá automáticamente a `login.html`.
2. Introduce tus credenciales (`admin` / `adminadmin`).
3. El sistema generará un token JWT que se almacenará en el `localStorage` del navegador.
4. El token se enviará automáticamente en la cabecera `Authorization: Bearer <token>` en cada petición a la API.
5. Si el token expira o es inválido, serás redirigido nuevamente al Login.
