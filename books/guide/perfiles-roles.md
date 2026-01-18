# Gestión de Perfiles y Roles en JettraDB

JettraDB implementa un sistema robusto de Control de Acceso Basado en Roles (RBAC) que distingue entre privilegios a nivel de aplicación (**Perfiles**) y privilegios a nivel de base de datos (**Roles**).

## 1. Perfiles de Aplicación (Globales)

Los perfiles determinan qué acciones puede realizar un usuario en la interfaz administrativa (Web, Shell, cURL, Driver) y su visibilidad global de los recursos del cluster.

| Perfil | Descripción | Restricciones de UI |
| :--- | :--- | :--- |
| **super-user** | Acceso total y absoluto al sistema. Ve todos los nodos, grupos y todas las bases de datos creadas en el sistema sin excepción. | Ninguna |
| **management** | Acceso administrativo global. Puede gestionar usuarios y configuraciones generales del cluster. | **No puede detener (Stop)** nodos del cluster. |
| **end-user** | Perfil estándar para usuarios finales. Solo tiene visibilidad de las bases de datos donde se le asigne un rol específico o las que él mismo cree. | No puede gestionar usuarios globales ni detener nodos. |

> **Seguridad Crítica:** El usuario `super-user` es una cuenta de sistema protegida. No puede ser eliminado ni su perfil puede ser alterado a través de las APIs estándar para prevenir bloqueos accidentales.

## 2. Roles a Nivel de Base de Datos

Cada base de datos creada en JettraDB tiene su propio conjunto de roles. Los roles siguen la convención de nomenclatura `{tipo-rol}_{nombre-db}`.

### Tipos de Roles Disponibles:

1.  **super-user**: Permiso total dentro de la base de datos.
    - Se asigna automáticamente al usuario `super-user` en cada base de datos nueva.
    - No puede ser removido de esta base de datos.
2.  **admin**: Permite administrar la base de datos, incluyendo la gestión de permisos para otros usuarios en esa base de datos específica.
    - El **creador** de una base de datos recibe automáticamente este rol para dicha base de datos.
3.  **read-write**: Permite realizar operaciones de lectura y escritura (Insert, Update, Delete) sobre los datos.
4.  **read**: Solo permite realizar consultas (Select/Find) de datos.
5.  **denied**: Deniega explícitamente el acceso. Una base de datos con este rol para un usuario será invisible para él en la interfaz.

## 3. Comportamiento del Sistema

### Creación de Bases de Datos
Todos los usuarios, independientemente de su perfil, tienen permiso para **crear nuevas bases de datos**. Al crear una base de datos:
1.  Se registra la base de datos en el Placement Driver.
2.  Se crea el rol `super-user_{db}` y se asigna al usuario `super-user`.
3.  Se crea el rol `admin_{db}` y se asigna al usuario que realizó la creación.

### Visibilidad en el Dashboard
- El usuario con perfil `super-user` visualiza el árbol completo de bases de datos del sistema.
- Los usuarios con perfiles `management` o `end-user` solo visualizan aquellas bases de datos en las que poseen un rol asignado (que no sea `denied`).

### Gestión de Usuarios
La administración de usuarios y la asignación de perfiles globales está reservada para los perfiles `super-user` y `management`. Sin embargo, un `end-user` con rol `admin` en una base de datos específica puede gestionar los permisos de otros usuarios dentro de los límites de esa base de datos.
