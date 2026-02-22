# Rutas MapLibre

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-7F52FF?style=for-the-badge&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-2024.09-4285F4?style=for-the-badge&logo=jetpackcompose)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-orange?style=for-the-badge)

Aplicación Android nativa de alto rendimiento para la grabación y gestión de rutas geográficas, diseñada con una arquitectura moderna y orientada a la privacidad del usuario.

## Funcionalidades Principales

La aplicación ofrece un conjunto de herramientas robustas para el seguimiento de actividades al aire libre:

- **Grabación de Rutas en Tiempo Real:** Seguimiento GPS con cálculo de distancia, duración y velocidad media.
- **Gestión Avanzada de Waypoints:** Creación de puntos de interés con descripciones y captura de fotografías, con posibilidad de edición post-grabación.
- **Intervalo de Grabación Personalizable:** Ajuste de la frecuencia de captura de puntos (1-30s) para balancear precisión y consumo de batería.
- **Importación y Exportación GPX:** Carga de rutas desde plataformas como WikiLoc y exportación de trayectos propios para interoperabilidad.
- **Compartición de Rutas:** Integración nativa con el sistema de Android para enviar archivos GPX a otras aplicaciones o contactos.

## Decisiones de Diseño y Arquitectura

El proyecto se fundamenta en decisiones técnicas orientadas a la mantenibilidad, rendimiento y escalabilidad.

- **Arquitectura MVVM:** Se implementó el patrón Model-View-ViewModel para separar la lógica de negocio de la interfaz de usuario. El `MapViewModel` orquesta las interacciones, gestionando el estado y la comunicación con la base de datos, mientras que la UI (Compose) simplemente reacciona a los cambios de estado.

- **UI Declarativa con Jetpack Compose:** La interfaz se construye con Jetpack Compose y Material 3, lo que permite un desarrollo rápido y un código de UI más limpio y predecible. El estado se gestiona mediante `StateFlow`, asegurando que la UI siempre refleje la fuente única de verdad.

- **Persistencia Local con Room:** Se eligió Room por su abstracción sobre SQLite, que facilita la creación de una base de datos local robusta, con consultas verificadas en tiempo de compilación y un sistema de migración claro.

- **SDK de MapLibre:** Se optó por MapLibre por su naturaleza de código abierto y su alto rendimiento en el renderizado de mapas vectoriales, ofreciendo una alternativa potente a servicios de mapas privativos.

## Stack Tecnológico

- **Lenguaje:** Kotlin 1.9.23
- **Interfaz de Usuario:** Jetpack Compose (BOM 2024.09.00), Material 3
- **Arquitectura:** MVVM, StateFlow, Coroutines, ViewModel
- **Navegación:** Navigation Compose
- **Base de Datos:** Room 2.6.1
- **Mapas:** MapLibre GL SDK 10.3.1
- **Localización:** Google Play Services Location 21.2.0

## Posibles Mejoras Futuras

- **Clasificación por Actividad:** Añadir una entidad en la base de datos para etiquetar rutas (senderismo, ciclismo, etc.) y filtrar por ellas.
- **Perfil de Altitud:** Integrar datos de elevación en los puntos de ruta para generar y visualizar gráficos de altitud.
- **Mapas Offline:** Permitir la descarga de regiones de mapa para una navegación completa sin conexión.
- **Sincronización en la Nube:** Ofrecer un respaldo opcional de las rutas en un servicio como Firebase.

## Guía de Instalación y Contribución

### Build Local

1.  **Clonar:** `git clone https://github.com/isaac/rutas-maplibre.git`
2.  **API Key:** Crea un archivo `secrets.properties` en la raíz del proyecto y añade tu clave de MapTiler:
    ```properties
    MAPTILER_API_KEY="TU_API_KEY"
    ```
3.  **Compilar:** Abre el proyecto en Android Studio y ejecuta `./gradlew assembleDebug`.

### Contribuir

Las contribuciones son bienvenidas. Por favor, siga el flujo de trabajo estándar de Fork & Pull Request.

1.  Haz un **Fork** del repositorio.
2.  Crea una nueva **rama** para tu funcionalidad (`git checkout -b feature/MiMejora`).
3.  Haz **commit** de tus cambios.
4.  Envía un **Pull Request** para su revisión.
