sequenceDiagram
    participant Cliente
    participant ServidorAIConsultaHelidon
    participant HelidonFramework
    participant AI_Engine

    Cliente->>ServidorAIConsultaHelidon: POST /consulta (Prompt)
    ServidorAIConsultaHelidon->>HelidonFramework: Validar Request
    HelidonFramework->>AI_Engine: Procesar con Modelo
    AI_Engine-->>HelidonFramework: Respuesta Generada
    HelidonFramework-->>ServidorAIConsultaHelidon: Formatear JSON
    ServidorAIConsultaHelidon-->>Cliente: 200 OK (Respuesta)