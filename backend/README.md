# API Acordes del Barrio

Consulta ../README.md, ../REVISION_DESARROLLO.md y ../DESPLIEGUE.md.

Node >=22.13. npm ci; npm start. MongoDB mediante MONGODB_URI (MONGO_URI compatible) y DB_NAME. En producción se exige una URI explícita y no se generan datos demo. PORT lo proporciona Render; servidor escucha en 0.0.0.0.

Sesiones opacas de ocho horas en colección sesion, con hash SHA-256 del token e índice TTL. Contraseñas scrypt con sal; nunca se devuelven en las respuestas.

Registro público solo estudiante. Listar/crear/modificar usuarios y modificar recursos requiere encargado. Estudiante e instructor solo gestionan reservas propias; encargado consulta y cancela reservas generales.

GET /api/health prueba acceso a MongoDB y responde 503 si falla.

Pruebas: npm test usa TEST_MONGODB_URI o localhost, base aleatoria independiente sin dropDatabase. Postman se regenera con npm run postman, exige cuentas demo y demoPassword local; ejecutar únicamente contra entorno de prueba autorizado.
