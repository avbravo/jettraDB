# Interfaz Web de JettraDB

El Dashboard de JettraDB es una aplicación de una sola página (SPA) moderna, elegante y reactiva, diseñada para ofrecer una visibilidad total sobre el cluster cloud-native y permitir la interacción directa con los diversos motores de base de datos.

## Acceso al Dashboard
La interfaz web se despliega automáticamente con el componente `jettra-web`.
- **URL**: `http://localhost:8081` (Puerto por defecto del componente web)
- **Credenciales**: **Requerido**. El acceso está protegido por JWT.
  - Usuario por defecto: `super-user`
  - Contraseña por defecto: `superuser`

## Jettra Web Vaadin ⭐
JettraDB ahora incluye un panel de administración moderno construido con **Vaadin**, ofreciendo una una experiencia de usuario enriquecida y componentes interactivos avanzados.

- **URL**: `http://localhost:8082`
- **Tecnología**: Java + Vaadin 24 + Quarkus.
- **Funcionalidades**:
    - **Explorador de Base de Datos**: Vista de árbol para navegar por bases de datos y colecciones.
    - **Monitoreo**: Gráficos y tablas para visualizar el estado del cluster.
    - **Administración**: Gestión de configuraciones y seguridad.

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
    - **Detener Nodo** 🛑: Botón para enviar una petición remota de parada al nodo (vía PD o directamente al endpoint `/stop`), lo que lo marcará como OFFLINE de forma segura.

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
- **Crear**: Provisiona nuevas bases de datos lógicas instantáneamente.
    - **Nombre**: Identificador único de la base de datos.
    - **Contenedor Multi-modelo**: Todas las bases de datos creadas son contenedores multi-modelo por definición, permitiendo alojar colecciones de diferentes tipos (Document, Graph, Vector, etc.).
    - **Tipo de Almacenamiento (Storage)**:
        - **Persistent (Store)**: Los datos se guardan en disco (jettra-store).
        - **In-Memory**: Los datos residen exclusivamente en RAM para baja latencia.
- **Listar**: Visualiza todas las bases de datos registradas indicando su **Motor** y tipo de **Almacenamiento**.
- **Eliminar**: Borra bases de datos y todos sus datos asociados tras confirmación.
- **Gestión de Colecciones (Document Explorer)** ⭐: Dentro del árbol de la base de datos, en cada sección de motor (ej: **Document, Graph, Vector**), ahora puede:
    - **Añadir Colección**: Al dar clic en `+`, se abrirá un modal para ingresar el nombre y confirmar el **Motor (Engine)** especializado. Por defecto se sugiere el motor de la sección desde la que se invoca.
    - **Refrescar**: Sincronizar la lista de colecciones de ese motor específico.
    - **Renombrar/Eliminar**: Opciones rápidas para el ciclo de vida de la colección.
- **Gestión de Secuencias (Sequences Subtree)** ⭐: Un nuevo nodo "🔑 Sequences" aparece bajo cada base de datos.
    - **Gestión Visual**: Permite crear, listar, incrementar y borrar secuencias asociadas a esa base de datos específica sin necesidad de comandos.

### 4. Seguridad y Gestión de Usuarios ⭐
Control centralizado de acceso y roles para todo el cluster:
- **Gestión de Usuarios**: Formulario avanzado para crear y editar cuentas de usuario.
- **Asignación de Roles por Base de Datos** ⭐: El formulario de usuario lista todas las bases de datos disponibles y permite seleccionar un rol específico para cada una (ej: `bob` es `reader` en `db1` pero `writer-reader` en `db2`).
- **Edición de Usuarios**: Permite cambiar la contraseña y los roles de usuarios existentes de forma visual.
- **Roles Predefinidos**:
    - `super-user`: Rol exclusivo del usuario `admin` (built-in). Tiene control total absoluto y no puede ser eliminado ni modificado.
    - `admin`: Administrador de base de datos. Puede gestionar usuarios, crear/editar/eliminar sus bases de datos asignadas. 
    - `read`: Acceso de solo lectura.
    - `read-write`: Acceso de lectura y escritura (sin permisos administrativos).

### 5. Consola de Consultas SQL (Nuevo) ⭐
Permite ejecutar sentencias SQL directamente desde el navegador:
1.  **Editor SQL**: Un área de texto con resaltado (vía fuente monoespaciada) para ingresar comandos `SELECT`, `INSERT`, `UPDATE` o `DELETE`.
2.  **Ejecución Unificada**: Las consultas se envían al Placement Driver, el cual las enruta automáticamente al motor correspondiente (Document, Graph, Vector, etc.) basándose en la base de datos y colección especificada.
3.  **Visualizador de Resultados**: Muestra el resultado de la ejecución en formato JSON estructurado, con indicadores de éxito o error en tiempo real.

### 7. Llaves Secuenciales (Sequences) ⭐
Interfaz visual para la gestión de contadores:
- **Listado de Secuencias**: Vista de tabla con el valor actual, incremento y base de datos de cada secuencia.
- **Creación**: Formulario para provisionar nuevas secuencias con valores iniciales personalizados.
- **Interacción**: Botón "NEXT" para incrementar manualmente y visualizar el cambio de estado en tiempo real.
- **Eliminación**: Capacidad para borrar secuencias persistentes del sistema.

### 8. Resolución de Referencias (Resolve References) ⭐

El Dashboard incluye una opción global para la resolución automática de referencias entre documentos:
- **Casilla "Resolve Refs"**: Disponible en la Consola SQL y en el Explorador de Documentos.
- **Funcionamiento**: Al marcar esta casilla, las consultas que devuelvan documentos con campos que contengan un `jettraID` válido mostrarán el objeto referenciado completo en lugar de solo el ID.
- **Eficiencia**: Utiliza acceso directo a la memoria del cluster para evitar JOINS tradicionales, acelerando la visualización de datos normalizados.

## Procedimiento de Autenticación en la Web

1. Al acceder a la URL, el sistema redirigirá automáticamente a `login.html`.
2. Introduce tus credenciales (`super-user` / `superuser`).
3. El sistema generará un token JWT que se almacenará en el `localStorage` del navegador.
4. El token se enviará automáticamente en la cabecera `Authorization: Bearer <token>` en cada petición a la API.
5. Si el token expira o es inválido, serás redirigido nuevamente al Login.
