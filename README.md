# Facebook Prospector V2

MVP Android preparado para compilar sin Android Studio mediante Gradle/CI.

## Funciones V2
- Grupos de Facebook con URL independiente de la búsqueda de PC.
- Múltiples búsquedas por grupo.
- Palabras clave y filtro guardados.
- Preparación de búsqueda: copia keyword al portapapeles y abre el grupo.
- Registro de última exploración.
- Estados: SIN EXPLORAR / hace X min / h / d.
- Historial.
- Respuestas rápidas editables en el código base.
- Variables `{nombre}` y `{necesidad}` como formato de respuesta.
- Tema claro/oscuro.
- Persistencia local preparada mediante DataStore en la siguiente iteración.
- GitHub Actions para generar APK sin Android Studio.

## Compilar sin Android Studio
1. Crea un repositorio en GitHub.
2. Sube todo el contenido de esta carpeta.
3. GitHub Actions ejecutará `.github/workflows/android.yml`.
4. En Actions abre la ejecución terminada.
5. Descarga el artefacto `facebook-prospector-debug-apk`.
6. Transfiere el APK al teléfono e instálalo.

También se puede compilar con `./gradlew assembleDebug` si tienes Java 17 y el SDK Android instalado.

## Nota sobre Facebook
La aplicación NO intenta forzar parámetros de una URL de escritorio dentro de la app oficial de Facebook. Conserva la intención de búsqueda (keyword/filtro), copia la keyword y abre el grupo. Facebook decide qué deep links y filtros admite.
