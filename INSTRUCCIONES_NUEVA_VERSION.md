> Estado 17/11/2026: el backend ya está Live en https://acordes-del-barrio-api.onrender.com/. Los datos originales se migraron a Atlas. Sesiones persistentes con Bearer; no se usa X-User-Id. Consulta REVISION_DESARROLLO.md para el estado comprobado; este documento conserva contenido histórico y no acredita pruebas visuales ni validación del cliente.

> Actualización 17/11/2026: consulta REVISION_DESARROLLO.md y DESPLIEGUE.md para el estado comprobado. Las evidencias y cifras anteriores son históricas; no acreditan esta revisión. La autenticación vigente usa Bearer y las sesiones se guardan en MongoDB. No ejecutes instrucciones antiguas de borrado de bases.

# Entregable Final — Acordes del Barrio (versión mejorada)

## Mejoras incorporadas
- Registro público de estudiantes: nombre, correo y contraseña.
- Inicio de sesión por correo y contraseña, con contraseña almacenada mediante scrypt y sal.
- Los nuevos usuarios reciben SIEMPRE rol `estudiante` desde el servidor.
- El administrador puede crear instructores y encargados desde el panel administrativo existente.
- Nueva pantalla de acceso con tarjeta, jerarquía visual y enlace de registro.
- Configuración de URL del servidor accesible desde la pantalla de acceso.
- Se conserva el módulo existente de salas, instrumentos, reservas y administración.

## Cómo arrancar
1. Instala Node.js y MongoDB; en `backend` crea `.env` a partir de `.env.example` y configura DEMO_PASSWORD (mínimo 8 caracteres).
2. En `backend`, ejecuta `npm ci` y luego `npm start`.
3. Comprueba en el navegador del PC `http://localhost:3000/api/health`.
4. Abre la carpeta `android` en Android Studio, sincroniza Gradle y ejecuta.
5. En emulador usa `http://10.0.2.2:3000/`; en teléfono físico usa `http://IP_LOCAL_DEL_PC:3000/` con ambos dispositivos en la misma red y firewall configurado.
6. Selecciona `Regístrate`, crea una cuenta y luego inicia sesión.

## Cuentas y seguridad
- El registro solo permite estudiantes. Los roles privilegiados deben asignarse por un encargado autenticado.
- Los usuarios demo preexistentes dependen de la variable DEMO_PASSWORD; no publiques esta contraseña.
- Las sesiones actuales se almacenan en memoria y se pierden al reiniciar el backend; para producción se requiere almacenamiento persistente de sesiones, HTTPS y recuperación de contraseñas.

## Pendiente antes de entregar
- Compilar y probar el APK en Android Studio y dispositivo real.
- Ejecutar pruebas de integración y de autorización; validar escenarios de reservas simultáneas.
- Completar historias de usuario, evidencias, validación con cliente, manuales y entrega APK según la rúbrica.
