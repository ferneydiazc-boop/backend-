# Backend desplegado en Render

URL verificada: https://acordes-del-barrio-api.onrender.com/
Salud: https://acordes-del-barrio-api.onrender.com/api/health (200, status ok).

Repositorio: https://github.com/ferneydiazc-boop/backend-; rama main. Servicio srv-dbuau90n74is73ddtbmg en FERNEY ARTURO's workspace. Plan Free, Oregon, una instancia y Auto-Deploy Off.

Root Directory: backend. Build: npm ci --omit=dev. Start: npm start. Health Check: /api/health. PORT lo proporciona Render; no se fija manualmente.

Variables: NODE_ENV=production, NODE_VERSION=24.14.0, DB_NAME=acordes_del_barrio, SEED_DEMO=false y MONGODB_URI secreta. No guardar contraseñas en el repositorio ni activar DEMO_PASSWORD en producción. Usuario Atlas con readWrite en acordes_del_barrio. Redes autorizadas de salida Render: 74.220.48.0/24 y 74.220.56.0/24; verificar las redes reales en Connect antes de cambiar de región o servicio.

Para futuros cambios: ejecutar tests aislados, revisar secretos, publicar código autorizado y usar Manual Deploy. Verificar estado Live y /api/health antes de afirmar éxito. El plan gratuito puede dormir y retrasar el primer acceso. No escalar a varias instancias con el bloqueo local actual.

Android apunta a la URL real por defecto. Para desarrollo local: gradlew.bat assembleDebug -PACORDES_DEBUG_API_URL=http://10.0.2.2:3000/. Configuración también editable desde el acceso de la app.

Consultar REVISION_DESARROLLO.md para las pruebas, migración y límites.
