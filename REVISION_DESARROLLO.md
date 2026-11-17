# Acordes del Barrio — despliegue y verificación

Fecha: 17/11/2026 (America/Bogota). Este informe sustituye el estado pendiente de los informes anteriores.

## Resultado

- API pública: https://acordes-del-barrio-api.onrender.com/
- Salud comprobada: https://acordes-del-barrio-api.onrender.com/api/health — HTTP 200, {"status":"ok"}.
- Render: servicio acordes-del-barrio-api, plan Free, región Oregon, una instancia, Auto-Deploy Off.
- Servicio: srv-dbuau90n74is73ddtbmg.
- Despliegue confirmado Live: dep-dbuavv0m7kps73bh3e1g, a las 14:51 del 17/11/2026.
- Código publicado: https://github.com/ferneydiazc-boop/backend-/commit/7d9e972add15f812fb6ec900b6a1c8985720ccd8
- CI de ese commit: https://github.com/ferneydiazc-boop/backend-/actions/runs/44158916999 — success.
- APK 1.1 (versionCode 2) compilado correctamente, con la URL HTTPS incorporada.

## Datos y seguridad

Se copió acordes_del_barrio del PC al clúster gratuito Atlas Cluster0, con autorización explícita: 4 usuarios, 6 recursos, 1 reserva y 3 documentos de contadores. Se verificó igualdad de cada documento después de copiar. Se conservaron los originales locales; no se borró ni sobrescribió una base existente en Atlas. No se migraron sesiones locales.

MONGODB_URI se configuró como secreto en Render, sin publicarla en Git ni incluirla en los entregables. NODE_ENV=production, NODE_VERSION=24.14.0, DB_NAME=acordes_del_barrio y SEED_DEMO=false. PORT provisto por Render: 10000; servidor escucha en 0.0.0.0. No se activa la semilla demo en producción.

Atlas autoriza la IP local preexistente y las redes de salida de Render 74.220.48.0/24 y 74.220.56.0/24, aprobadas expresamente. No se abrió 0.0.0.0/0. Son redes compartidas por otros servicios de Render; sigue siendo obligatoria la autenticación. El usuario de conexión se redujo de atlasAdmin a readWrite únicamente sobre acordes_del_barrio.

## Pruebas

| Comprobación | Resultado |
|---|---|
| Integración local de API y MongoDB aislado | 19/19 PASS |
| CI remoto del backend publicado | success |
| Compilación en Render | success; auditoría npm con 0 vulnerabilidades reportadas |
| Despliegue tras habilitar redes Atlas | Live |
| HTTPS, consulta de salud y MongoDB real | 200 / status ok |
| Funciones en API pública | 16/16 PASS |
| Compilación Android 1.1 | BUILD SUCCESSFUL |
| URL incorporada a BuildConfig | HTTPS de Render verificada |
| Interfaz en emulador/dispositivo | Pendiente: el emulador no arrancó por aceleración de hardware no disponible |
| Lint Android | Pendiente: el entorno impidió escribir el informe en la revisión inicial |

Las 16 pruebas públicas comprobaron login del administrador con su contraseña existente, cuentas originales, registro de estudiantes forzado por servidor, acceso administrativo rechazado para estudiantes, contraseña incorrecta, creación de un recurso temporal, reservas simultáneas (201/409), disponibilidad, aislamiento de reservas, cancelación, revocación de sesión al desactivar cuenta y logout. Se crearon dos cuentas de prueba, un recurso y dos reservas de prueba: las cuentas quedaron desactivadas, el recurso inactivo y las reservas canceladas. No se eliminaron documentos ni se alteraron usuarios o reservas originales.

El primer arranque en Render falló al conectar mientras faltaban sus redes en Atlas. La compilación había pasado. Tras aprobar y habilitar las redes se repitió el despliegue y quedó Live. La migración desde el PC requirió resolver registros DNS SRV con servidores públicos dentro del proceso de migración; no se cambió el DNS de Windows.

## Cambios de Android

Debug y release usan por defecto https://acordes-del-barrio-api.onrender.com/. Se mantuvo el servidor local para desarrollo mediante la pantalla Configurar servidor o:

    gradlew.bat assembleDebug -PACORDES_DEBUG_API_URL=http://10.0.2.2:3000/

La URL release puede reemplazarse con -PACORDES_API_URL=https://OTRO-HOST-VERIFICADO/. Release exige HTTPS; debug permite HTTP local. La preferencia del antiguo emulador por defecto migra una sola vez al backend público; una selección local posterior se conserva. Los servidores personalizados se conservan. El tiempo de lectura se aumentó a 90 segundos para permitir el arranque de la instancia gratuita.

Se conservan los módulos de recursos, reservas, roles y administración. La aplicación sigue siendo Android Kotlin/Compose con Retrofit, repositorio y ViewModels. Tokens opacos con expiración de ocho horas guardados como hash en MongoDB, índice TTL y revocación desde logout o modificación de usuario.

## Límites y entrega académica

- No se afirma que la interfaz Android funcione en dispositivo: falta instalar el APK y probar login, registro, recursos y reservas por HTTPS en un teléfono real. El intento con el AVD existente falló por aceleración no disponible; no se modificó Hyper-V ni la seguridad de Windows.
- El APK entregado está firmado para depuración; no se generó una clave release ni se publicó en una tienda.
- La exclusión de reservas sigue siendo local a un proceso. Mantener una instancia. No se garantiza exclusión entre procesos solapados durante futuros despliegues.
- El servicio gratuito puede dormir y demorar su primera respuesta. No se activaron planes de pago.
- No hay recuperación de contraseña ni limitación de intentos implementadas; son mejoras pendientes.
- El HTML de la rúbrica no está dentro del proyecto original. Faltan evidencias de tablero de HU cerrado y compartido, funcionamiento por sprint y validación formal del cliente. La sustentación debe realizarla el equipo. No se inventaron esas evidencias.
- No se borraron archivos previos de GitHub: README.md y backend.zip originales se conservaron. El código del backend está ahora descomprimido en backend/ y con render.yaml en la raíz. El proyecto local completo conserva Android y toda su documentación.

## Evidencias entregadas

pruebas-render.json: 16 comprobaciones de la API pública.
migracion-atlas.json: conteos e igualdad de la migración autorizada.
compilacion-android-render.txt: compilación del APK actualizado.
emulador-limitacion.txt: bloqueo real de aceleración.

Las evidencias antiguas en backend/evidencia son históricas. Este informe no convierte una compilación ni pruebas de API en una prueba de interfaz de Android.
