# Acordes del Barrio

Aplicación Android para consultar y reservar salas de ensayo e instrumentos musicales. Incluye registro, inicio de sesión, roles de estudiante, instructor y administrador, disponibilidad, control de solapes, reservas y panel administrativo.

**Autor:** Ferney Arturo Díaz Cerquera. **Versión Android:** 1.1 (versionCode 2). **Entrega:** 17 de noviembre de 2026.

## Contenido del repositorio completo

| Ruta | Contenido |
|---|---|
| [android/](android/) | Código Kotlin, pantallas Compose, navegación, modelos, Retrofit, repositorio, ViewModels, recursos y Gradle Wrapper |
| [backend/](backend/) | API Express, driver MongoDB, configuración, pruebas, colección Postman y evidencias |
| [docs/Manual_de_Usuario_Acordes_del_Barrio.pdf](docs/Manual_de_Usuario_Acordes_del_Barrio.pdf) | Manual de usuario en PDF con instalación y tareas de cada rol |
| [MANUAL_TECNICO.md](MANUAL_TECNICO.md) | Arquitectura, autenticación, endpoints y configuración |
| [DESPLIEGUE.md](DESPLIEGUE.md) | Configuración de Render y MongoDB Atlas |
| [REVISION_DESARROLLO.md](REVISION_DESARROLLO.md) | Cambios, migración, pruebas y límites de la revisión técnica |
| [render.yaml](render.yaml) | Configuración reproducible de Render sin valores secretos |
| [.github/workflows/backend.yml](.github/workflows/backend.yml) | Pruebas automáticas con MongoDB aislado |

El archivo `backend.zip` se conserva como material anterior; el código vigente es el de las carpetas anteriores. El APK se entrega por separado en Moodle. No hace falta extraer ese ZIP para compilar.

## Backend desplegado

- URL base: https://acordes-del-barrio-api.onrender.com/
- Salud: https://acordes-del-barrio-api.onrender.com/api/health
- Base de datos: MongoDB Atlas, base `acordes_del_barrio`.
- Render Free, una instancia y Auto-Deploy desactivado. El primer acceso tras inactividad puede tardar.

La raíz `/` es una API sin página de inicio; puede devolver `Ruta no encontrada`. La comprobación válida es `/api/health`. Las rutas privadas requieren `Authorization: Bearer TOKEN`.

## Ejecutar Android

Abrir `android/` en Android Studio con SDK 34 y JDK 17 o compatible. Se incluye Gradle Wrapper 8.7; AGP 8.5.2 y Kotlin 1.9.24.

```powershell
cd android
.\gradlew.bat assembleDebug
```

El APK resultante usa por defecto la API HTTPS de Render. Para utilizar el backend local en el emulador:

```powershell
.\gradlew.bat assembleDebug -PACORDES_DEBUG_API_URL=http://10.0.2.2:3000/
```

También puede configurarse el servidor desde la pantalla de acceso. La URL base se define en `android/app/build.gradle.kts`; `ApiClient.kt` utiliza `BuildConfig.API_URL`. Release solo admite HTTPS y permite una URL alternativa mediante `ACORDES_API_URL`. Las claves de firma privadas no forman parte del repositorio.

## Ejecutar backend local

Node.js 22.13 o posterior. La versión usada en Render es 24.14.0. Configurar un `.env` privado desde `backend/.env.example` con `MONGODB_URI` y `DB_NAME`.

```powershell
cd backend
npm ci
npm start
```

En producción, `MONGODB_URI` se guarda como secreto en Render; `NODE_ENV=production`, `SEED_DEMO=false` y `PORT` lo proporciona la plataforma. El servidor escucha en `0.0.0.0`. La cuenta de Atlas tiene `readWrite` limitado a la base de la aplicación. No se publican contraseñas, sesiones ni datos de usuarios.

Para crear datos de demostración locales se requiere activar explícitamente `SEED_DEMO=true` y una `DEMO_PASSWORD` propia. No ejecutar restablecimientos de administrador como parte de un arranque habitual.

## Organización y seguridad

Flujo Android: Pantalla → ViewModel → Repository → Retrofit → API → MongoDB. El registro público impone el rol estudiante. El administrador gestiona usuarios y recursos; el estudiante y el instructor solo gestionan reservas propias. Las contraseñas usan scrypt con sal y las sesiones opacas se almacenan hasheadas en MongoDB con expiración e índice TTL. Logout y modificaciones de usuario revocan sesiones.

Los solapes se rechazan con 409; horarios contiguos pueden reservarse. El bloqueo actual opera dentro de un proceso: mantener una instancia y revisar concurrencia distribuida antes de escalar.

## Verificación de la entrega

- 19 pruebas de integración locales; CI del backend aprobado.
- 16 comprobaciones contra la API pública: login, registro, roles, disponibilidad, conflictos, cancelación y sesiones.
- APK 1.1 compilado y firma de depuración verificada.
- El autor confirmó el 17 de noviembre que instaló la versión 1.1 en su teléfono, inició sesión y utilizó reservas. Es una confirmación del usuario, adicional a las pruebas automatizadas; no se presenta como una observación directa del agente.
- El emulador del entorno no arrancó por falta de aceleración disponible. Lint quedó pendiente por un problema de escritura del informe. Recuperación de contraseña y limitación de intentos siguen pendientes.

Las evidencias antiguas en `backend/evidencia` son históricas; los JSON de Render corresponden a esta revisión. El manual explica únicamente las funciones disponibles en la interfaz.

## Entrega en Moodle

Entregar el APK 1.1, el manual PDF y el enlace a este repositorio completo. Las contraseñas de evaluación deben comunicarse de forma privada. La entrega no incluye ni inventa un acta de validación con el cliente o evidencia de tablero académico.
