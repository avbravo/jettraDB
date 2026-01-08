# Column Engine: Analytics at Scale

El motor columnar de JettraDB está optimizado para cargas de trabajo analíticas (OLAP). Almacena los datos por columnas en lugar de por filas, lo que permite escaneos extremadamente rápidos y un uso eficiente de la caché de la CPU.

## Especificaciones Técnicas
- **Clase Principal**: `io.jettra.engine.column.ColumnEngine`
- **Optimizaciones**: Proyección de columnas (sólo se leen los campos necesarios) y agregaciones vectorizadas.
- **Consumo**: Minimiza el I/O al ignorar columnas que no forman parte de la consulta.

## Operaciones de Alta Performance

### 1. Inserción de Filas
Aunque el almacenamiento es columnar, la API permite la inserción de filas completas de forma atómica.

```java
Map<String, Object> telemetry = Map.of(
    "temp", 22.5,
    "humidity", 60,
    "site", "Madrid-01"
);
engine.insert(telemetry).subscribe().with(v -> {});
```

### 2. Agregaciones Vectorizadas (SUM)
Ideal para dashboards financieros o de monitoreo IoT.

```java
engine.sum("temp")
      .subscribe().with(avg -> System.out.println("Temperatura Total: " + avg));
```

### 3. Proyección de Columnas (Select)
Recupera sólo los datos necesarios para reducir el tráfico de red y memoria.

```java
engine.project(List.of("site", "temp"))
      .subscribe().with(results -> results.forEach(System.out::println));
```

## Arquitectura Interna
Los datos se organizan en bloques columnares en memoria (`StorageMap`) y se persisten de forma asíncrona mediante el motor LSM de `jettra-store`, asegurando durabilidad sin sacrificar la velocidad de análisis.
