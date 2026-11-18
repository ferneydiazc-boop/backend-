# Manual técnico — Acordes del Barrio

## Arquitectura
Android Kotlin + Jetpack Compose, MVVM, Retrofit/Gson y OkHttp. API Express 5 sobre Node 24.14.0. MongoDB Atlas. Flujo: Screen → ViewModel → Repository → Retrofit → HTTPS → Express → MongoDB.

Colecciones: usuario, recurso, reserva, counters y sesion. IDs numéricos del dominio y _id interno MongoDB. Índices únicos de ID/correo y TTL para sesiones. Contraseñas scrypt con sal; tokens opacos aleatorios, hash SHA-256 en MongoDB y expiración de ocho horas. Las contraseñas/hash no se devuelven por API.

Registro público fija rol estudiante. Login recibe correo, clave y tipoAcceso (estudiante o encargado); instructor usa acceso estudiante. Encargado administra usuarios/recursos y consulta/cancela reservas generales. Estudiantes e instructores gestionan reservas propias. Cambiar usuario revoca sus sesiones y logout elimina el token en servidor.

## API
POST /api/auth/register y /api/auth/login; POST /api/auth/logout y GET /api/auth/me.
GET/POST /api/users y PATCH /api/users/:id (encargado).
GET /api/resources, /api/resources/:id y /api/resources/:id/availability. POST/PUT/DELETE de recursos según ruta (encargado; DELETE lógico).
GET/POST /api/reservations, PUT /api/reservations/:id y PATCH /api/reservations/:id/cancel.
GET /api/health comprueba MongoDB. Peticiones privadas: Authorization: Bearer TOKEN.

Solape: inicio existente < fin nuevo y fin existente > inicio nuevo, para el mismo recurso, fecha y reserva activa. Franjas contiguas permitidas. Conflicto 409; validación/mantenimiento 400; autenticación 401; permisos 403; inexistente 404. Exclusión serial dentro del proceso; mantener una instancia.

## Ejecución y configuración
Backend público: https://acordes-del-barrio-api.onrender.com/. Variables secretas en Render; servidor usa PORT y 0.0.0.0. Desarrollo local: .env privado desde backend/.env.example; npm ci y npm start. La semilla exige SEED_DEMO=true fuera de producción y contraseña propia. No ejecutar reset-admin en cada inicio ni borrar bases para preparar una demo.

Android: Gradle 8.7 (wrapper incluido), AGP 8.5.2, Kotlin 1.9.24, SDK 34 y bytecode Java 17. APK 1.1 generado con JBR 21 instalado. Debug usa HTTPS público; alternativa -PACORDES_DEBUG_API_URL=http://10.0.2.2:3000/. Release solo HTTPS.

## Verificación
19 pruebas locales y CI; 16 pruebas contra API pública. Android compilado; interfaz pendiente en dispositivo. Lint pendiente por escritura del entorno. Consulta REVISION_DESARROLLO.md para resultados y límites exactos.
