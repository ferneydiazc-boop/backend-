# API Acordes del Barrio

API pública verificada: https://acordes-del-barrio-api.onrender.com/
Health: GET /api/health → 200/status ok. Consulta ../DESPLIEGUE.md y ../REVISION_DESARROLLO.md para configuración, migración, pruebas y límites.

Node 24.14.0, Express 5 y MongoDB Atlas. Desarrollo: npm ci; .env privado desde .env.example; npm start. Producción usa MONGODB_URI secreta, DB_NAME, NODE_ENV=production, SEED_DEMO=false y PORT proporcionado por Render.

Registro público fija estudiante. Login devuelve token opaco; peticiones privadas con Authorization: Bearer TOKEN. Encargado administra; estudiantes/instructores solo gestionan reservas propias. Logout y cambios de usuario revocan sesiones. Contraseñas scrypt; sesiones en MongoDB con tokens hasheados y TTL.

Pruebas: npm test usa TEST_MONGODB_URI independiente, base aleatoria y sin dropDatabase. CI pasó y la API pública pasó 16 verificaciones funcionales. Una sola instancia: bloqueo de solapes local al proceso. No guardar credenciales ni .env en Git.
