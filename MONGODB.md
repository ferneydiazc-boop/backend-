> Estado 17/11/2026: el backend ya está Live en https://acordes-del-barrio-api.onrender.com/. Los datos originales se migraron a Atlas. Sesiones persistentes con Bearer; no se usa X-User-Id. Consulta REVISION_DESARROLLO.md para el estado comprobado; este documento conserva contenido histórico y no acredita pruebas visuales ni validación del cliente.

> Actualización 17/11/2026: consulta REVISION_DESARROLLO.md y DESPLIEGUE.md para el estado comprobado. Las evidencias y cifras anteriores son históricas; no acreditan esta revisión. La autenticación vigente usa Bearer y las sesiones se guardan en MongoDB. No ejecutes instrucciones antiguas de borrado de bases.

# Conectar la API a tu MongoDB

La app Android **no** habla con MongoDB: habla con la API, y la API habla con MongoDB. Solo necesitas darle a la API tu cadena de conexión.

## Opción A: MongoDB Atlas (en la nube, gratis)
1. Entra a https://cloud.mongodb.com y crea un cluster gratuito (M0).
2. **Database Access** → *Add New Database User* → crea usuario y contraseña (anótalos).
3. **Network Access** → *Add IP Address* → *Allow Access from Anywhere* (`0.0.0.0/0`) para la demo.
4. **Database → Connect → Drivers** → copia la cadena, algo como:
   `mongodb+srv://USUARIO:CLAVE@cluster0.xxxxx.mongodb.net/`
   (si la clave tiene símbolos como `@` o `#`, codifícalos: `@` = `%40`, `#` = `%23`).
5. En `backend/` copia `.env.example` como `.env` y pega la cadena:
   ```
   MONGODB_URI=mongodb+srv://USUARIO:CLAVE@cluster0.xxxxx.mongodb.net/
   DB_NAME=acordes_del_barrio
   ```

## Opción B: MongoDB local
1. Instala MongoDB Community Server + Compass desde https://www.mongodb.com/try/download/community
2. Con el servicio encendido, el `.env` queda así:
   ```
   MONGODB_URI=mongodb://127.0.0.1:27017
   DB_NAME=acordes_del_barrio
   ```

## Arrancar
```
cd backend
npm install
npm start
```
Debe imprimir `MongoDB conectado`. Al primer arranque se crean las colecciones y los datos semilla (3 usuarios, 6 recursos). Abre Compass o Atlas → *Browse Collections* → base `acordes_del_barrio` y verás: `usuario`, `recurso`, `reserva`, `counters`.

## Si falla
- `No se pudo conectar a MongoDB`: revisa la cadena, la clave, y en Atlas el acceso de red (`0.0.0.0/0`).
- `bad auth`: usuario o contraseña incorrectos.
- Después de crear reservas en la app, aparecen en la colección `reserva` (refresca Compass/Atlas).

## Pruebas automáticas
`npm test` usa la base `acordes_test` (la crea y la borra sola) en la misma conexión del `.env`. No toca tus datos reales.
