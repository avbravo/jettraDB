# Guía de Optimización de JettraDB

Esta guía detalla las políticas y reglas de optimización implementadas en los proyectos de JettraDB, enfocadas en minimizar la latencia y maximizar la eficiencia de memoria utilizando las características más avanzadas de la JVM (Java 25).

## Configuración de la JVM

Para entornos de producción y pruebas de alto rendimiento, utilizamos una configuración optimizada del Garbage Collector y la gestión de memoria.

### Banderas de Optimización ("Best Practices")

La configuración estándar recomendada para los nodos de JettraDB (Store y PD) es:

```bash
java -Xmx8g -Xms8g \
     -XX:+UseZGC \
     -XX:+ZGenerational \
     -XX:+UseCompactObjectHeaders \
     -jar quarkus-run.jar
```

### Explicación de las Banderas

1.  **`-XX:+UseZGC`**: Activa el Z Garbage Collector. ZGC está diseñado para tiempos de pausa extremadamente bajos (sub-milisegundos), lo cual es crítico para una base de datos distribuida como JettraDB para evitar *hiccups* en el consenso Raft y las lecturas/escrituras.
2.  **`-XX:+ZGenerational`**: Habilita el modo generacional de ZGC. La mayoría de los objetos mueren jóvenes. Al separar el heap en generaciones, ZGC puede recolectar objetos efímeros mucho más eficientemente, reduciendo el overhead de CPU y mejorando el throughput.
3.  **`-XX:+UseCompactObjectHeaders`**: Una optimización clave de las versiones modernas de Java (Project Lilliput). Reduce el tamaño de la cabecera de los objetos instanciados en el heap. En aplicaciones con millones de objetos pequeños (como nodos de documentos o entradas de índice), esto reduce significativamente el consumo de memoria y mejora la localidad de caché.
4.  **` AlwaysPreTouch` (Deshabilitado por defecto)**: *Ver sección de "Optimizaciones Futuras"*.

> **Nota Crítica sobre Versiones de Java:**
> La opción `-XX:+UseCompactObjectHeaders` es una característica experimental/avanzada (Project Lilliput) que requiere versiones muy recientes de la JVM (JDK 24/25+). **Actualmente está activa en la configuración por defecto**, por lo que es necesario asegurar que el entorno de ejecución (Local y Docker) utilice una versión de Java compatible.

## Configuración en los Proyectos (Estado Actual)

### 1. Desarrollo y Test (`application.properties`)

```properties
quarkus.jvm.args=-XX:+UseZGC -XX:+ZGenerational -XX:+UseCompactObjectHeaders
```

### 2. Contenedores Docker (`docker-compose.yaml`)

```yaml
environment:
  - JAVA_OPTS=-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager -XX:+UseZGC -XX:+ZGenerational -XX:+UseCompactObjectHeaders
```

## Optimizaciones Futuras y Producción Avanzada

### Uso de `-XX:+AlwaysPreTouch`

Actualmente, **hemos deshabilitado** esta bandera en la configuración de desarrollo y Docker por defecto debido a que incrementa notablemente el tiempo de inicio de los contenedores y servicios (la JVM debe poner a cero toda la memoria asignada antes de arrancar).

**¿Cuándo habilitarla?**
Se recomienda encarecidamente habilitarla **solo en entornos de Producción reales** (no en desarrollo local ni CI rápido) donde la estabilidad de latencia a largo plazo es más importante que un reinicio rápido.

**Beneficios:**
1.  **Elimina "Hiccups":** Evita que el sistema operativo pause la base de datos aleatoriamente para asignar páginas de memoria física bajo carga.
2.  **Validación de RAM:** Fuerza un fallo inmediato al arranque si no hay RAM suficiente, evitando muertes súbitas (OOM Killer) posteriores.

**Cómo habilitarla:**
Añade la bandera `-XX:+AlwaysPreTouch` a la variable `JAVA_OPTS` o `quarkus.jvm.args`.

*Ejemplo para Producción:*
```bash
java -Xmx32g -Xms32g -XX:+UseZGC -XX:+ZGenerational -XX:+AlwaysPreTouch -jar quarkus-run.jar
```

## Recomendaciones de Hardware

*   **Memoria:** Se recomienda un mínimo de **8GB** de Heap (`-Xmx8g`) para nodos de almacenamiento en producción para aprovechar al máximo ZGC Generacional.
*   **CPU:** ZGC se beneficia de múltiples núcleos para los hilos de marcado concurrente.
