'use strict';
// Ejecutar una sola vez desde backend: npm run reset-admin
const crypto = require('node:crypto');
const { MongoClient } = require('mongodb');

async function main() {
  const password = process.env.DEMO_PASSWORD;
  if (!password || password.length < 8 || password === 'CAMBIAR_POR_CLAVE_SEGURA') {
    throw new Error('Establece una contraseña propia de 8 o más caracteres en DEMO_PASSWORD del archivo .env.');
  }
  const client = new MongoClient(process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017', {serverSelectionTimeoutMS: 8000});
  try {
    await client.connect();
    const db = client.db(process.env.DB_NAME || 'acordes_del_barrio');
    const users = db.collection('usuario');
    const email = 'encargado@acordes.local';
    const salt = crypto.randomBytes(16).toString('hex');
    const clave_hash = `${salt}:${crypto.scryptSync(password, salt, 64).toString('hex')}`;
    const existing = await users.findOne({correo:email});
    if (existing && existing.rol !== 'encargado') throw new Error('Conflicto: el correo de administrador pertenece a otro rol. No se realizaron cambios.');
    if (existing) {
      await users.updateOne({_id:existing._id}, {$set:{clave_hash, activo:true}});
      console.log('OK: contraseña del administrador restablecida en la colección usuario.');
    } else {
      const counter = await db.collection('counters').findOneAndUpdate({_id:'usuario'}, {$inc:{seq:1}}, {upsert:true, returnDocument:'after'});
      const id = (counter.value ?? counter).seq;
      await users.insertOne({id_usuario:id, nombre:'Laura Encargada', rol:'encargado', correo:email, clave_hash, activo:true});
      console.log('OK: cuenta administradora creada en la colección usuario.');
    }
    console.log('Correo: '+email+' | Contraseña: valor de DEMO_PASSWORD (no se imprime).');
  } finally { await client.close(); }
}
main().catch(e=>{console.error('ERROR:',e.message); process.exitCode=1;});
