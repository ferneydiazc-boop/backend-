const crypto = require('node:crypto');
const { MongoClient } = require('mongodb');

const uri = process.env.MONGODB_URI || process.env.MONGO_URI || 'mongodb://127.0.0.1:27017';
if (process.env.NODE_ENV === 'production' && !process.env.MONGODB_URI && !process.env.MONGO_URI) throw new Error('MONGODB_URI es obligatoria en producción.');
const dbName = process.env.DB_NAME || 'acordes_del_barrio';
const client = new MongoClient(uri, { serverSelectionTimeoutMS: 8000 });
let db;

// Oculta el _id interno de Mongo: la API sigue usando id_usuario / id_recurso / id_reserva (numéricos)
const NO_ID = { projection: { _id: 0 } };
const col = (nombre) => db.collection(nombre);

async function conectar() {
  await client.connect();
  db = client.db(dbName);
  await col('usuario').createIndex({ id_usuario: 1 }, { unique: true });
  await col('usuario').createIndex({ correo: 1 }, { unique: true });
  await col('recurso').createIndex({ id_recurso: 1 }, { unique: true });
  await col('reserva').createIndex({ id_reserva: 1 }, { unique: true });
  await col('reserva').createIndex({ id_recurso: 1, fecha: 1 });
  await col('reserva').createIndex({ id_usuario: 1 });
  await col('sesion').createIndex({ expira: 1 }, { expireAfterSeconds: 0 });
  return db;
}
const getDb = () => db;
const cerrar = () => client.close();

// Autoincremental (1, 2, 3...) con una colección de contadores
async function siguienteId(nombre) {
  const r = await col('counters').findOneAndUpdate(
    { _id: nombre }, { $inc: { seq: 1 } }, { upsert: true, returnDocument: 'after' });
  const doc = r && r.value !== undefined ? r.value : r;   // compatible con distintas versiones del driver
  return doc.seq;
}

async function insertar(coleccion, campoId, datos) {
  const doc = { [campoId]: await siguienteId(coleccion), ...datos };
  await col(coleccion).insertOne(doc);
  delete doc._id;
  return doc;
}

// Exclusión mutua: evita que dos reservas simultáneas pasen la validación de solape a la vez
let cola = Promise.resolve();
function conBloqueo(fn) {
  const corrida = cola.then(fn);
  cola = corrida.catch(() => {});
  return corrida;
}

async function seed() {
  // Migración de datos demo: asignar credenciales solo en desarrollo.
  if (process.env.NODE_ENV === 'production' || process.env.SEED_DEMO !== 'true') return;
  const inicial = process.env.DEMO_PASSWORD;
  if (!inicial || inicial.length < 8 || inicial === 'CAMBIAR_POR_CLAVE_SEGURA') throw new Error('Configura una DEMO_PASSWORD propia para SEED_DEMO=true.');
  if ((await col('usuario').countDocuments()) > 0) {
    // Si ya existen estudiantes, asegurar la cuenta administradora sin modificar cuentas existentes.
    if (!(await col('usuario').findOne({ correo: 'encargado@acordes.local' })) && inicial && inicial.length >= 8) {
      const saltAdmin = crypto.randomBytes(16).toString('hex');
      const siguiente = await siguienteId('usuario');
      await col('usuario').insertOne({ id_usuario: siguiente, nombre: 'Laura Encargada',
        rol: 'encargado', correo: 'encargado@acordes.local', activo: true,
        clave_hash: `${saltAdmin}:${crypto.scryptSync(inicial, saltAdmin, 64).toString('hex')}` });
    }
    return;
  }
  if (!inicial || inicial.length < 8) throw new Error('Configura DEMO_PASSWORD (mínimo 8 caracteres) en .env antes de iniciar.');
  const clave_hash = (clave) => { const salt = crypto.randomBytes(16).toString('hex'); return `${salt}:${crypto.scryptSync(clave, salt, 64).toString('hex')}`; };
  await col('usuario').insertMany([
    { id_usuario: 1, nombre: 'Laura Encargada', rol: 'encargado', correo: 'encargado@acordes.local', clave_hash: clave_hash(inicial), activo: true },
    { id_usuario: 2, nombre: 'Carlos Estudiante', rol: 'estudiante', correo: 'carlos@acordes.local', clave_hash: clave_hash(inicial), activo: true },
    { id_usuario: 3, nombre: 'Ana Estudiante', rol: 'estudiante', correo: 'ana@acordes.local', clave_hash: clave_hash(inicial), activo: true },
  ]);
  await col('usuario').insertOne({ id_usuario: 4, nombre: 'Instructor Demo', rol: 'instructor', correo: 'instructor@acordes.local', clave_hash: clave_hash(inicial), activo: true });
  await col('recurso').insertMany([
    { id_recurso: 1, nombre: 'Sala de ensayo 1', tipo: 'sala', estado_disp: 'disponible', ubicacion: 'Piso 1, sala A' },
    { id_recurso: 2, nombre: 'Sala de ensayo 2', tipo: 'sala', estado_disp: 'disponible', ubicacion: 'Piso 1, sala B' },
    { id_recurso: 3, nombre: 'Sala de ensayo 3', tipo: 'sala', estado_disp: 'disponible', ubicacion: 'Piso 2, sala C' },
    { id_recurso: 4, nombre: 'Guitarra eléctrica', tipo: 'instrumento', estado_disp: 'disponible', ubicacion: 'Armario 1' },
    { id_recurso: 5, nombre: 'Batería', tipo: 'instrumento', estado_disp: 'mantenimiento', ubicacion: 'Sala 2' },
    { id_recurso: 6, nombre: 'Teclado', tipo: 'instrumento', estado_disp: 'disponible', ubicacion: 'Armario 2' },
  ]);
  await col('counters').updateOne({ _id: 'usuario' }, { $set: { seq: 4 } }, { upsert: true });
  await col('counters').updateOne({ _id: 'recurso' }, { $set: { seq: 6 } }, { upsert: true });
}

module.exports = { conectar, cerrar, getDb, col, NO_ID, siguienteId, insertar, conBloqueo, seed };
