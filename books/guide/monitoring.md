# Monitoreo y Alertas Predictivas

JettraDB no solo monitoriza el estado actual del cluster, sino que utiliza algoritmos de tendencia para predecir posibles fallos o cuellos de botella antes de que ocurran.

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

```bash
# Ejemplo: Escalar nodos de almacenamiento si recibes una alerta crítica de disco
docker-compose up -d --scale jettra-store=5
```

## Configuración de Umbrales
Los umbrales de alerta se pueden configurar en el archivo `config.json` del Placement Driver (PD), permitiendo personalizar la sensibilidad del sistema predictivo según el entorno (Dev, Stage, Prod).
