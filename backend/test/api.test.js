const crypto = require('node:crypto');
process.env.MONGODB_URI = process.env.TEST_MONGODB_URI || 'mongodb://127.0.0.1:27017';
delete process.env.MONGO_URI;
process.env.DB_NAME = 'acordes_test_' + crypto.randomBytes(8).toString('hex');
process.env.NODE_ENV = 'test';
process.env.SEED_DEMO = 'true';
process.env.DEMO_PASSWORD = crypto.randomBytes(24).toString('hex');
const test = require('node:test');
const assert = require('node:assert');
const { conectar, cerrar, getDb, seed } = require('../src/db');
const app = require('../src/app');

let base, server;
const tokens = {};
const ENC = 1, EST = 2, EST2 = 3;
const call = async (method, path, user, body) => {
  const res = await fetch(base + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(user ? { Authorization: `Bearer ${tokens[user]}` } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  });
  return { status: res.status, body: await res.json() };
};

test.before(async () => {
  await conectar();
  await seed();
  await new Promise((r) => { server = app.listen(0, r); });
  base = `http://127.0.0.1:${server.address().port}`;
  for (const [id, correo] of [[1,'encargado@acordes.local'],[2,'carlos@acordes.local'],[3,'ana@acordes.local']]) {
    const r = await call('POST', '/api/auth/login', null, { correo, clave: process.env.DEMO_PASSWORD, tipoAcceso: id === 1 ? 'encargado' : 'estudiante' });
    assert.strictEqual(r.status,200); tokens[id] = r.body.token;
  }
});
test.after(async () => { if (server) await new Promise(r => server.close(r)); /* Test data retained: no destructive cleanup. */ await cerrar(); });

test('sin token -> 401', async () => assert.strictEqual((await call('GET', '/api/resources')).status, 401));

test('listar recursos (sin inactivos) y filtrar por tipo', async () => {
  const r = await call('GET', '/api/resources?tipo=sala', EST);
  assert.strictEqual(r.status, 200);
  assert.ok(r.body.length === 3 && r.body.every((x) => x.tipo === 'sala'));
  assert.strictEqual((await call('GET', '/api/resources?tipo=xx', EST)).status, 400);
});

test('detalle de recurso 200 / 404', async () => {
  assert.strictEqual((await call('GET', '/api/resources/1', EST)).status, 200);
  assert.strictEqual((await call('GET', '/api/resources/999', EST)).status, 404);
});

test('CRUD de recursos: solo encargado', async () => {
  assert.strictEqual((await call('POST', '/api/resources', EST, { nombre: 'X', tipo: 'sala' })).status, 403);
  assert.strictEqual((await call('POST', '/api/resources', ENC, { nombre: 'X' })).status, 400);
  const c = await call('POST', '/api/resources', ENC, { nombre: 'Amplificador', tipo: 'instrumento' });
  assert.strictEqual(c.status, 201);
  const id = c.body.id_recurso;
  const u = await call('PUT', `/api/resources/${id}`, ENC, { ubicacion: 'Depósito' });
  assert.strictEqual(u.status, 200);
  assert.strictEqual(u.body.ubicacion, 'Depósito');
  const d = await call('DELETE', `/api/resources/${id}`, ENC);
  assert.strictEqual(d.body.estado_disp, 'inactivo');
});

let reservaId;
test('crear reserva válida -> 201', async () => {
  const r = await call('POST', '/api/reservations', EST, { id_recurso: 1, fecha: '2026-09-21', hora_inicio: '09:00', hora_fin: '10:00' });
  assert.strictEqual(r.status, 201);
  assert.strictEqual(r.body.estado, 'activa');
  assert.strictEqual(r.body.id_usuario, EST);
  reservaId = r.body.id_reserva;
});

test('REGLA PRINCIPAL: solape -> 409', async () => {
  const solapes = [['09:00', '11:00'], ['09:30', '09:45'], ['08:00', '09:30'], ['08:00', '12:00']];
  for (const [ini, fin] of solapes) {
    const r = await call('POST', '/api/reservations', EST2, { id_recurso: 1, fecha: '2026-09-21', hora_inicio: ini, hora_fin: fin });
    assert.strictEqual(r.status, 409, `${ini}-${fin} debía dar 409`);
  }
});

test('franjas contiguas, otra fecha u otro recurso NO son conflicto', async () => {
  for (const b of [
    { id_recurso: 1, fecha: '2026-09-21', hora_inicio: '10:00', hora_fin: '11:00' },
    { id_recurso: 1, fecha: '2026-09-21', hora_inicio: '08:00', hora_fin: '09:00' },
    { id_recurso: 1, fecha: '2026-09-22', hora_inicio: '09:00', hora_fin: '10:00' },
    { id_recurso: 2, fecha: '2026-09-21', hora_inicio: '09:00', hora_fin: '10:00' },
  ]) assert.strictEqual((await call('POST', '/api/reservations', EST2, b)).status, 201);
});

test('REGLA SECUNDARIA: recurso en mantenimiento/inactivo -> 400', async () => {
  const r = await call('POST', '/api/reservations', EST, { id_recurso: 5, fecha: '2026-09-21', hora_inicio: '09:00', hora_fin: '10:00' });
  assert.strictEqual(r.status, 400);
  assert.match(r.body.error, /no está disponible/);
});

test('validaciones -> 400 / 404', async () => {
  const ok = { id_recurso: 1, fecha: '2026-09-25', hora_inicio: '09:00', hora_fin: '10:00' };
  for (const bad of [
    { ...ok, hora_inicio: '11:00', hora_fin: '10:00' },
    { ...ok, fecha: '21/09/2026' },
    { ...ok, hora_inicio: '9am' },
    { ...ok, id_recurso: undefined },
  ]) assert.strictEqual((await call('POST', '/api/reservations', EST, bad)).status, 400);
  assert.strictEqual((await call('POST', '/api/reservations', EST, { ...ok, id_recurso: 999 })).status, 404);
});

test('consultar reservas por recurso+fecha y por usuario', async () => {
  const a = await call('GET', '/api/reservations?resourceId=1&date=2026-09-21', EST);
  assert.strictEqual(a.status, 200);
  assert.strictEqual(a.body.length, 1);
  assert.ok(a.body.every(x => x.id_usuario === EST));
  const b = await call('GET', `/api/reservations?userId=${EST}`, EST);
  assert.ok(b.body.every((x) => x.id_usuario === EST));
  assert.strictEqual((await call('GET', `/api/reservations?userId=${EST2}`, EST)).status, 403);
});

test('disponibilidad del recurso', async () => {
  const r = await call('GET', '/api/resources/1/availability?date=2026-09-21', EST);
  assert.strictEqual(r.status, 200);
  assert.strictEqual(r.body.franjas_ocupadas.length, 3);
});

test('editar reserva: ok, solape 409, ajena 403', async () => {
  assert.strictEqual((await call('PUT', `/api/reservations/${reservaId}`, EST, { hora_fin: '09:45' })).status, 200);
  assert.strictEqual((await call('PUT', `/api/reservations/${reservaId}`, EST, { hora_fin: '10:30' })).status, 409);
  assert.strictEqual((await call('PUT', `/api/reservations/${reservaId}`, EST2, { hora_fin: '09:30' })).status, 403);
});

test('cancelar: ajena 403, propia 200, doble 400, libera franja', async () => {
  assert.strictEqual((await call('PATCH', `/api/reservations/${reservaId}/cancel`, EST2)).status, 403);
  const c = await call('PATCH', `/api/reservations/${reservaId}/cancel`, EST);
  assert.strictEqual(c.status, 200);
  assert.strictEqual(c.body.estado, 'cancelada');
  assert.strictEqual((await call('PATCH', `/api/reservations/${reservaId}/cancel`, EST)).status, 400);
  const re = await call('POST', '/api/reservations', EST2, { id_recurso: 1, fecha: '2026-09-21', hora_inicio: '09:00', hora_fin: '10:00' });
  assert.strictEqual(re.status, 201);
  assert.strictEqual((await call('PATCH', '/api/reservations/9999/cancel', EST)).status, 404);
});

test('registro normalizado, rol forzado, duplicado y login por rol', async () => {
  const body = { nombre: 'Nueva Cuenta', correo: ' NUEVA@EXAMPLE.COM ', clave: 'test-password-123', rol: 'encargado' };
  const r = await call('POST','/api/auth/register',null,body);
  assert.strictEqual(r.status,201); assert.strictEqual(r.body.usuario.rol,'estudiante');
  assert.ok(!r.body.usuario.clave_hash);
  assert.strictEqual((await call('POST','/api/auth/register',null,body)).status,409);
  assert.strictEqual((await call('POST','/api/auth/login',null,{...body, tipoAcceso:'encargado'})).status,403);
  assert.strictEqual((await call('POST','/api/auth/login',null,{...body, clave:'incorrecta', tipoAcceso:'estudiante'})).status,401);
  const login = await call('POST','/api/auth/login',null,{...body,tipoAcceso:'estudiante'});
  assert.strictEqual(login.status,200);
  tokens.nuevo = login.body.token;
  assert.strictEqual((await call('GET','/api/users','nuevo')).status,403);
  assert.strictEqual((await call('POST','/api/auth/logout','nuevo')).status,200);
  assert.strictEqual((await call('GET','/api/auth/me','nuevo')).status,401);
});
test('sesión persistente y revocación al desactivar usuario', async () => {
  const { col } = require('../src/db');
  assert.ok(await col('sesion').findOne({_id:crypto.createHash('sha256').update(tokens[EST2]).digest('hex')}));
  assert.strictEqual((await call('PATCH','/api/users/3',ENC,{activo:false})).status,200);
  assert.strictEqual((await call('GET','/api/auth/me',EST2)).status,401);
});
test('reservas concurrentes permiten solo una y edición rechaza mantenimiento', async () => {
  const body = {id_recurso:3,fecha:'2027-01-10',hora_inicio:'12:00',hora_fin:'13:00'};
  const r = await Promise.all([call('POST','/api/reservations',EST,body),call('POST','/api/reservations',EST,body)]);
  assert.deepStrictEqual(r.map(x=>x.status).sort(),[201,409]);
  const id = r.find(x=>x.status===201).body.id_reserva;
  assert.strictEqual((await call('PUT','/api/resources/3',ENC,{estado_disp:'mantenimiento'})).status,200);
  assert.strictEqual((await call('PUT','/api/reservations/'+id,EST,{hora_fin:'14:00'})).status,400);
});

test('sesión funciona tras recargar la aplicación Express', async () => {
  delete require.cache[require.resolve('../src/app')];
  const nueva = require('../src/app');
  const otro = await new Promise(resolve => { const s = nueva.listen(0,'127.0.0.1',()=>resolve(s)); });
  try {
    const res = await fetch('http://127.0.0.1:'+otro.address().port+'/api/auth/me',{headers:{Authorization:'Bearer '+tokens[ENC]}});
    assert.strictEqual(res.status,200); assert.strictEqual((await res.json()).rol,'encargado');
  } finally { await new Promise(resolve=>otro.close(resolve)); }
});
test('cabecera de ID y token adulterado no autentican', async () => {
  for (const headers of [{'X-User-Id':'1'},{Authorization:'Bearer invalid-token'}]) {
    const res=await fetch(base+'/api/users',{headers}); assert.strictEqual(res.status,401);
  }
});
test('administrador debe elegir su acceso e instructor usa acceso usuario', async () => {
  assert.strictEqual((await call('POST','/api/auth/login',null,{correo:'encargado@acordes.local',clave:process.env.DEMO_PASSWORD,tipoAcceso:'estudiante'})).status,403);
  const r=await call('POST','/api/auth/login',null,{correo:'instructor@acordes.local',clave:process.env.DEMO_PASSWORD,tipoAcceso:'estudiante'});
  assert.strictEqual(r.status,200); tokens.instructor=r.body.token;
  assert.strictEqual((await call('GET','/api/users','instructor')).status,403);
  assert.strictEqual((await call('GET','/api/resources','instructor')).status,200);
});
