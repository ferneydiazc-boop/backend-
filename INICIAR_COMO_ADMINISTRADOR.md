> Estado 17/11/2026: el backend ya está Live en https://acordes-del-barrio-api.onrender.com/. Los datos originales se migraron a Atlas. Sesiones persistentes con Bearer; no se usa X-User-Id. Consulta REVISION_DESARROLLO.md para el estado comprobado; este documento conserva contenido histórico y no acredita pruebas visuales ni validación del cliente.

> Actualización 17/11/2026: consulta REVISION_DESARROLLO.md y DESPLIEGUE.md para el estado comprobado. Las evidencias y cifras anteriores son históricas; no acreditan esta revisión. La autenticación vigente usa Bearer y las sesiones se guardan en MongoDB. No ejecutes instrucciones antiguas de borrado de bases.

# Corregir el acceso del administrador

El backend usa **`usuario` (singular)**. La colección `usuarios` (plural) contiene registros anteriores y no se utiliza para iniciar sesión.

## Una sola vez

1. En `backend`, crea `.env` a partir de `.env.example` si todavía no existe.
2. Cambia `DEMO_PASSWORD=CAMBIAR_POR_CLAVE_SEGURA` por **una contraseña nueva de al menos 8 caracteres**, que solo tú conozcas. No publiques ese archivo.
3. Comprueba que MongoDB esté encendido.
4. En PowerShell, desde `backend`, ejecuta:

   ```powershell
   npm install
   npm run reset-admin
   npm start
   ```

5. En Android, elige **Administrador** e inicia sesión con `encargado@acordes.local` y la contraseña de `DEMO_PASSWORD`.

**Importante:** `npm run reset-admin` modifica exclusivamente la cuenta `encargado@acordes.local` de la colección `usuario`. No borra reservas ni estudiantes. Ejecútalo otra vez si quieres cambiar la contraseña. No lo ejecutes en cada inicio.

## Estudiantes

Elige **Estudiante**, pulsa **Regístrate**, crea la cuenta y después inicia sesión con ese correo y contraseña. El servidor asigna el rol `estudiante`, aunque alguien intente enviar otro rol.

## Conexión Android

- Emulador: `http://10.0.2.2:3000` con el backend abierto en el mismo computador.
- Celular físico: IP local del computador, ambos conectados a la misma red; revisa firewall y configuración de URL de la app.

## Comprobaciones

- `http://localhost:3000/api/health` debe devolver `{"status":"ok"}`.
- El administrador debe poder ver usuarios y reservas ajenas y cancelarlas.
- El estudiante solo puede gestionar sus propias reservas.
- Si aparece 401, verifica que ejecutaste `npm run reset-admin` en la misma base de datos que usa `npm start`.

No se incluye APK compilado. Es necesario abrir `android` en Android Studio, sincronizar Gradle y ejecutar las pruebas en el dispositivo o emulador.
