> Estado 17/11/2026: el backend ya está Live en https://acordes-del-barrio-api.onrender.com/. Los datos originales se migraron a Atlas. Sesiones persistentes con Bearer; no se usa X-User-Id. Consulta REVISION_DESARROLLO.md para el estado comprobado; este documento conserva contenido histórico y no acredita pruebas visuales ni validación del cliente.

> Actualización 17/11/2026: consulta REVISION_DESARROLLO.md y DESPLIEGUE.md para el estado comprobado. Las evidencias y cifras anteriores son históricas; no acreditan esta revisión. La autenticación vigente usa Bearer y las sesiones se guardan en MongoDB. No ejecutes instrucciones antiguas de borrado de bases.

# Roles y pantallas — Entregable Final

## Acceso
- En la pantalla de ingreso selecciona **Estudiante** o **Administrador**.
- El registro público crea únicamente cuentas de **estudiante**.
- El servidor valida el rol auténtico de la cuenta; elegir Administrador no concede permisos.
- La cuenta fija de administración es `encargado@acordes.local` con contraseña definida por `DEMO_PASSWORD` al crearla. Si la cuenta ya existía, conserva su contraseña previa.
- El administrador entra directamente a su panel; el estudiante entra a la lista de recursos.

## Panel del administrador
- Resumen de usuarios, recursos y reservas activas.
- Lista de **todas** las reservas con nombre del usuario y recurso.
- Cancelación de cualquier reserva activa con confirmación.
- Crear usuarios estudiantes/instructores, cambiar su estado o rol, gestionar salas e instrumentos.
- El servidor protege las operaciones administrativas.

## Estudiantes
- Registro con nombre, correo y contraseña.
- Inicio de sesión, exploración de recursos, creación y cancelación de **sus propias** reservas.
- No tienen acceso al panel de administración ni a las reservas de otras personas.

## Inicio del backend
1. Copia `backend/.env.example` a `backend/.env` si no existe y configura `DEMO_PASSWORD`.
2. Inicia MongoDB y luego ejecuta `npm install` y `npm start` en `backend`.
3. En Android Studio abre la carpeta `android`.
4. Emulador: `http://10.0.2.2:3000/`; celular físico: `http://IP_LOCAL_PC:3000/`.

## Nota de validación
Se verificó la sintaxis JavaScript; la compilación Android y las pruebas integrales deben realizarse en Android Studio y con MongoDB activo antes de presentar el APK.
