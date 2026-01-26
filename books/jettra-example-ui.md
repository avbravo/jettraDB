# Jettra Example UI

**Jettra Example UI** es una aplicación de demostración que muestra cómo construir interfaces de usuario utilizando el framework **Jettra UI**. Esta aplicación se ejecuta como un servicio web contenedorizado y proporciona una interfaz visual para interactuar con JettraDB.

## Características

*   **Interfaz Web Contenedorizada**: Se ejecuta como un contenedor Docker independiente, integrado en la red `jettra-net`.
*   **Gestión de Sesiones**: Implementa un flujo de autenticación completo (Login/Logout).
*   **Diseño Responsivo**: Utiliza Tailwind CSS y componentes de Jettra UI para adaptarse a diferentes tamaños de pantalla.
*   **Interacciones Dinámicas**: Utiliza HTMX para actualizaciones parciales de la página sin recargas completas.
*   **Modo Oscuro/Claro**: Soporte integrado para cambio de temas.

## Arquitectura

La aplicación está construida utilizando Quarkus y Jettra UI.

*   **Backend**: Quarkus (Java) maneja la lógica del servidor y la comunicación con `jettra-pd`.
*   **Frontend**: Jettra UI genera el HTML en el servidor. HTMX maneja la interactividad en el cliente.
*   **Estilos**: Tailwind CSS (vía CDN en este ejemplo) y Flowbite.

## Ejecución

El servicio `jettra-example-ui` está configurado en `docker-compose.yaml` y se inicia automáticamente con el script de construcción.

*   **Puerto Externo**: `8082`
*   **Puerto Interno**: `8080`
*   **Dirección**: `http://localhost:8082/`

### Comandos Útiles

Para reconstruir y reiniciar solo este servicio:

```bash
docker-compose up -d --build --force-recreate jettra-example-ui
```

## Estructura del Código

*   `src/main/docker/Dockerfile.jvm`: Configuración del contenedor.
*   `src/main/java/io/jettra/example/ui`:
    *   `LoginResource.java`: Página de inicio de sesión.
    *   `DashboardResource.java`: Panel principal (protegido).
    *   `AuthResource.java`: Manejo de autenticación (API).
    *   `form/LoginForm.java`: Componente de formulario de login reutilizable.

## Uso

1.  Acceda a `http://localhost:8082/`.
2.  Inicie sesión con sus credenciales (por defecto en entorno de desarrollo).
3.  Explore el panel de control para ver estadísticas y navegar por las secciones.
