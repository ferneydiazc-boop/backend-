> Estado 17/11/2026: el backend ya está Live en https://acordes-del-barrio-api.onrender.com/. Los datos originales se migraron a Atlas. Sesiones persistentes con Bearer; no se usa X-User-Id. Consulta REVISION_DESARROLLO.md para el estado comprobado; este documento conserva contenido histórico y no acredita pruebas visuales ni validación del cliente.

> Actualización 17/11/2026: consulta REVISION_DESARROLLO.md y DESPLIEGUE.md para el estado comprobado. Las evidencias y cifras anteriores son históricas; no acreditan esta revisión. La autenticación vigente usa Bearer y las sesiones se guardan en MongoDB. No ejecutes instrucciones antiguas de borrado de bases.

# ENTREGABLE FINAL — Acordes del Barrio

## Cambios realizados sobre el ZIP original
- Se reemplazó la selección de usuarios sin contraseña por login con correo y contraseña.
- El servidor valida un token de sesión y el rol en cada solicitud; no acepta `X-User-Id` como autenticación.
- Contraseñas con hash scrypt y sal aleatoria; no se entregan hashes en la API.
- Roles: encargado (administración), instructor (reservas propias), estudiante (reservas propias).
- El encargado dispone de un panel para crear usuarios, cambiar roles, activar/desactivar usuarios, crear y desactivar recursos.
- Los usuarios no administradores solo pueden consultar sus propias reservas, incluso si omiten el filtro `userId`.
- El nombre visible de la app se cambió a «Entregable Final».

## Arranque del backend
1. Instalar Node.js 22.13+ y MongoDB.
2. En `backend`, copiar `.env.example` a `.env` y cambiar `DEMO_PASSWORD` por una clave segura de al menos 8 caracteres.
3. Ejecutar `npm ci` y `npm start`.
4. Primer inicio con base vacía: `encargado@acordes.local`, `carlos@acordes.local`, `ana@acordes.local`, `instructor@acordes.local`, todos con la contraseña definida en `DEMO_PASSWORD`.
5. Si ya existían usuarios sin contraseña, el inicio asigna `DEMO_PASSWORD` a esos usuarios (migración demo). Cambiar sus contraseñas después.
6. En Android, ajustar «Servidor» a `http://10.0.2.2:3000/` para emulador o a la IP del computador en la misma Wi-Fi para teléfono físico.

## Abrir Android
En Android Studio: Open > carpeta `android` > Sync Gradle > Run. Para generar APK: Build > Build Bundle(s) / APK(s) > Build APK(s). El APK **no está incluido**: no se pudo compilar ni verificar en este entorno.

## Advertencias
- Las sesiones se guardan en memoria del servidor y caducan a las 8 horas; al reiniciar el backend hay que iniciar sesión otra vez.
- El tráfico HTTP local no está cifrado; usar HTTPS para despliegue real.
- Los tests originales usan el sistema de cabecera anterior y necesitan actualizarse antes de usarse como evidencia de aprobación.
- La interfaz del instructor comparte las pantallas de reserva del estudiante; el rol está diferenciado en el backend, pero no hay módulo específico de clases/instructor.
- La documentación y el código requieren prueba final en Android Studio y teléfono físico.
