const os = require('os');
const app = require('./app');
const { conectar, seed } = require('./db');

const PORT = process.env.PORT || 3000;

(async () => {
  try {
    await conectar();
    await seed();
  } catch (e) {
    console.error('No se pudo conectar a MongoDB:', e.message);
    console.error('Revisa MONGODB_URI en el archivo .env (o que MongoDB esté encendido).');
    process.exit(1);
  }
  // '0.0.0.0' = acepta conexiones del emulador Android (10.0.2.2) y de celulares en la misma red
  app.listen(PORT, '0.0.0.0', () => {
    console.log(`API Acordes del Barrio lista en http://localhost:${PORT}  (MongoDB conectado)`);
    console.log(`Desde el emulador Android usa:  http://10.0.2.2:${PORT}/`);
    for (const list of Object.values(os.networkInterfaces()))
      for (const i of list || [])
        if (i.family === 'IPv4' && !i.internal) console.log(`Desde un celular fisico (misma wifi): http://${i.address}:${PORT}/`);
  });
})();
