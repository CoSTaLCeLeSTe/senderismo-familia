# Senderismo Familia

App Android para elegir, planificar y preparar rutas de senderismo familiares por Sevilla y Cádiz, pensada para ir con un niño pequeño.

## Fase 1 (esta versión)

- Catálogo de 17 rutas en Sevilla, Cádiz y Huelva con filtros (provincia, desde Sevilla o El Puerto, aptas para carrito, recorrido máximo).
- Ficha de cada ruta: recorrido propuesto, duración a ritmo familiar, avisos, cómo llegar y ficha oficial.
- Planificador: día, hora de salida, coche y horario completo con aviso de siesta (14:30).
- Previsión del tiempo de la zona (Open-Meteo) con avisos de calor, lluvia, viento y frío.
- Lista de mochila según la estación.

Todavía no sincroniza entre los dos móviles: cada móvil guarda su propio plan. La sincronización con Firebase llega en la siguiente iteración.

## Instalar

1. En el móvil, abre la página **Releases** de este repositorio y entra en "Senderismo Familia (última versión)".
2. Descarga `senderismo-familia.apk`.
3. Ábrela y permite instalar apps de origen desconocido si Android lo pide.

Cada cambio en `main` compila una versión nueva automáticamente (GitHub Actions). Para actualizar, descarga e instala la nueva APK encima de la anterior.

## Compilación

- Kotlin + Jetpack Compose, minSdk 26, targetSdk 35.
- `./gradlew assembleDebug`
- La firma usa `keystore/senderismo.keystore` para que todas las versiones se puedan instalar encima de la anterior.
