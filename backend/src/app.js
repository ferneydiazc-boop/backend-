const crypto = require('node:crypto');
const express = require('express');
const { col, NO_ID, insertar, conBloqueo } = require('./db');

const app = express();
app.use(express.json());

// ---------- Helpers ----------
const DATE_RE = /^\d{4}-\d{2}-\d{2}$/;
const TIME_RE = /^([01]\d|2[0-3]):[0-5]\d$/;

const isValidDate = (s) => {
  if (!DATE_RE.test(s)) return false;
  const d = new Date(s + 'T00:00:00Z');
  return !isNaN(d) && d.toISOString().slice(0, 10) === s;
};
const err = (res, status, mensaje, extra = {}) => res.status(status).json({ error: mensaje, ...extra });
const toId = (v) => (/^\d+$/.test(String(v)) ? Number(v) : null);

const uno = (coleccion, filtro) => col(coleccion).findOne(filtro, NO_ID);
const lista = (coleccion, filtro, orden) => col(coleccion).find(filtro, NO_ID).sort(orden).toArray();

// Sesiones opacas: nunca confiar en un ID enviado por el cliente.
const tokenHash = (token) => crypto.createHash('sha256').update(token).digest('hex');
const hashClave = (clave, salt = crypto.randomBytes(16).toString('hex')) =>
  `${salt}:${crypto.scryptSync(clave, salt, 64).toString('hex')}`;
function verificarClave(clave, almacenado) {
  if (!almacenado || !almacenado.includes(':')) return false;
  const [salt, hash] = almacenado.split(':');
  const a = Buffer.from(hash, 'hex');
  const b = crypto.scryptSync(clave, salt, 64);
  return a.length === b.length && crypto.timingSafeEqual(a, b);
}
const publico = ({ clave_hash, _id, ...u }) => u;
async function auth(req, res, next) {
  const token = (req.header('Authorization') || '').replace(/^Bearer /i, '');
  const sesion = token && await col('sesion').findOne({ _id: tokenHash(token) });
  if (!sesion || sesion.expira <= new Date()) return err(res, 401, 'Inicia sesión nuevamente.');
  const user = await uno('usuario', { id_usuario: sesion.id });
  if (!user || user.activo === false) return err(res, 401, 'Usuario inactivo.');
  req.user = user; req.token = token; next();
}
const soloEncargado = (req, res, next) =>
  req.user.rol === 'encargado' ? next() : err(res, 403, 'Solo el encargado puede realizar esta acción.');

// Registro público: el rol se fija en el servidor; nadie puede registrarse como administrador.
app.post('/api/auth/register', async (req, res) => {
  const { nombre, correo, clave } = req.body || {};
  if (typeof nombre !== 'string' || nombre.trim().length < 2 || nombre.trim().length > 100 ||
      typeof correo !== 'string' || !/^\S+@\S+\.\S+$/.test(correo.trim()) ||
      typeof clave !== 'string' || clave.length < 8 || clave.length > 128)
    return err(res, 400, 'Ingresa nombre, correo válido y contraseña de 8 a 128 caracteres.');
  const email = correo.trim().toLowerCase();
  try {
    const u = await insertar('usuario', 'id_usuario', {
      nombre: nombre.trim(), correo: email, clave_hash: hashClave(clave),
      rol: 'estudiante', activo: true
    });
    res.status(201).json({ usuario: publico(u), mensaje: 'Cuenta creada. Ahora puedes iniciar sesión.' });
  } catch (e) {
    if (e.code === 11000) return err(res, 409, 'Este correo ya está registrado.');
    throw e;
  }
});

app.post('/api/auth/login', async (req, res) => {
  const { correo, clave, tipoAcceso } = req.body || {};
  if (typeof correo !== 'string' || correo.length > 254 || typeof clave !== 'string' || clave.length > 128) return err(res, 400, 'Correo y contraseña obligatorios.');
  const u = await uno('usuario', { correo: correo.trim().toLowerCase() });
  if (!u || u.activo === false || !verificarClave(clave, u.clave_hash)) return err(res, 401, 'Credenciales incorrectas.');
  if (!['estudiante','encargado'].includes(tipoAcceso)) return err(res, 400, 'Selecciona Estudiante o Administrador.');
  if (tipoAcceso === 'encargado' && u.rol !== 'encargado') return err(res, 403, 'Esta cuenta no tiene permisos de administrador.');
  if (tipoAcceso === 'estudiante' && u.rol === 'encargado') return err(res, 403, 'Selecciona Administrador para esta cuenta.');
  const token = crypto.randomBytes(32).toString('hex');
  await col('sesion').insertOne({ _id: tokenHash(token), id: u.id_usuario, expira: new Date(Date.now() + 8 * 3600000) });
  res.json({ token, usuario: publico(u) });
});
app.post('/api/auth/logout', auth, async (req, res) => { await col('sesion').deleteOne({ _id: tokenHash(req.token) }); res.json({ ok: true }); });
app.get('/api/auth/me', auth, (req, res) => res.json(publico(req.user)));

// ---------- Salud ----------
app.get('/api/health', async (req, res) => {
  try { await col('usuario').findOne({}, { projection: { _id: 1 }, maxTimeMS: 2000 }); res.json({ status: 'ok' }); }
  catch { err(res, 503, 'Base de datos no disponible.'); }
});

// ---------- Usuarios ----------
app.get('/api/users', auth, soloEncargado, async (req, res) => {
  res.json((await lista('usuario', {}, { id_usuario: 1 })).map(publico));
});
app.post('/api/users', auth, soloEncargado, async (req, res) => {
  const { nombre, correo, clave, rol } = req.body || {};
  if (typeof nombre !== 'string' || !nombre.trim() || typeof correo !== 'string' || !/^\S+@\S+\.\S+$/.test(correo) || typeof clave !== 'string' || clave.length < 8 || clave.length > 128 || !['instructor','estudiante'].includes(rol))
    return err(res, 400, 'Nombre, correo, rol y contraseña de al menos 8 caracteres obligatorios.');
  try {
    const u = await insertar('usuario', 'id_usuario', { nombre: nombre.trim(), correo: correo.trim().toLowerCase(), rol, clave_hash: hashClave(clave), activo: true });
    res.status(201).json(publico(u));
  } catch (e) { if (e.code === 11000) return err(res, 409, 'Correo registrado.'); throw e; }
});
app.patch('/api/users/:id', auth, soloEncargado, async (req, res) => {
  const id = toId(req.params.id);
  const u = id && await uno('usuario', { id_usuario: id });
  if (!u) return err(res, 404, 'Usuario no encontrado.');
  const b = req.body || {}, cambios = {};
  if (b.rol !== undefined) {
    if (!['instructor','estudiante'].includes(b.rol)) return err(res, 400, 'Rol inválido.');
    if (id === req.user.id_usuario && b.rol !== 'encargado') return err(res, 400, 'No puedes quitarte el rol administrador.');
    cambios.rol = b.rol;
  }
  if (b.activo !== undefined) {
    if (typeof b.activo !== 'boolean' || (id === req.user.id_usuario && !b.activo)) return err(res, 400, 'Estado inválido.');
    cambios.activo = b.activo;
  }
  if (b.clave !== undefined) {
    if (typeof b.clave !== 'string' || b.clave.length < 8 || b.clave.length > 128) return err(res, 400, 'Contraseña demasiado corta.');
    cambios.clave_hash = hashClave(b.clave);
  }
  if (!Object.keys(cambios).length) return err(res, 400, 'No hay cambios válidos.');
  await col('usuario').updateOne({ id_usuario: id }, { $set: cambios });
  await col('sesion').deleteMany({ id });
  res.json(publico(await uno('usuario', { id_usuario: id })));
});

// ---------- Recursos ----------
const TIPOS = ['sala', 'instrumento'];
const ESTADOS = ['disponible', 'mantenimiento', 'inactivo'];

app.get('/api/resources', auth, async (req, res) => {
  const { tipo, date } = req.query;
  if (tipo && !TIPOS.includes(tipo)) return err(res, 400, `tipo debe ser: ${TIPOS.join(', ')}.`);
  if (date && !isValidDate(date)) return err(res, 400, 'date debe tener formato YYYY-MM-DD.');

  const filtro = { estado_disp: { $ne: 'inactivo' } };
  if (tipo) filtro.tipo = tipo;
  const recursos = await lista('recurso', filtro, { id_recurso: 1 });

  // Con ?date= se indica si el recurso tiene reservas activas ese día
  if (date) {
    await Promise.all(recursos.map(async (r) => {
      r.reservado_en_fecha = (await col('reserva').countDocuments(
        { id_recurso: r.id_recurso, fecha: date, estado: 'activa' }, { limit: 1 })) > 0;
    }));
  }
  res.json(recursos);
});

app.get('/api/resources/:id', auth, async (req, res) => {
  const id = toId(req.params.id);
  const r = id && await uno('recurso', { id_recurso: id });
  if (!r) return err(res, 404, 'Recurso no encontrado.');
  res.json(r);
});

// Franjas ocupadas de un recurso en una fecha (para la pantalla de detalle)
app.get('/api/resources/:id/availability', auth, async (req, res) => {
  const id = toId(req.params.id);
  const r = id && await uno('recurso', { id_recurso: id });
  if (!r) return err(res, 404, 'Recurso no encontrado.');
  const { date } = req.query;
  if (!date || !isValidDate(date)) return err(res, 400, 'date es obligatorio con formato YYYY-MM-DD.');
  const ocupadas = await col('reserva')
    .find({ id_recurso: id, fecha: date, estado: 'activa' }, { projection: { _id: 0, id_reserva: 1, hora_inicio: 1, hora_fin: 1 } })
    .sort({ hora_inicio: 1 }).toArray();
  res.json({ id_recurso: id, fecha: date, estado_disp: r.estado_disp, franjas_ocupadas: ocupadas });
});

function validarRecurso(b) {
  if (!b.nombre || typeof b.nombre !== 'string') return 'nombre es obligatorio.';
  if (!TIPOS.includes(b.tipo)) return `tipo debe ser: ${TIPOS.join(', ')}.`;
  if (b.estado_disp !== undefined && !ESTADOS.includes(b.estado_disp))
    return `estado_disp debe ser: ${ESTADOS.join(', ')}.`;
  return null;
}

app.post('/api/resources', auth, soloEncargado, async (req, res) => {
  const b = req.body || {};
  const e = validarRecurso(b);
  if (e) return err(res, 400, e);
  res.status(201).json(await insertar('recurso', 'id_recurso', {
    nombre: b.nombre, tipo: b.tipo, estado_disp: b.estado_disp || 'disponible', ubicacion: b.ubicacion || null,
  }));
});

app.put('/api/resources/:id', auth, soloEncargado, async (req, res) => {
  const id = toId(req.params.id);
  const actual = id && await uno('recurso', { id_recurso: id });
  if (!actual) return err(res, 404, 'Recurso no encontrado.');
  const b = { ...actual, ...(req.body || {}) };
  const e = validarRecurso(b);
  if (e) return err(res, 400, e);
  await col('recurso').updateOne({ id_recurso: id },
    { $set: { nombre: b.nombre, tipo: b.tipo, estado_disp: b.estado_disp, ubicacion: b.ubicacion ?? null } });
  res.json(await uno('recurso', { id_recurso: id }));
});

// DELETE = desactivar (borrado lógico, conserva el historial de reservas)
app.delete('/api/resources/:id', auth, soloEncargado, async (req, res) => {
  const id = toId(req.params.id);
  const actual = id && await uno('recurso', { id_recurso: id });
  if (!actual) return err(res, 404, 'Recurso no encontrado.');
  await col('recurso').updateOne({ id_recurso: id }, { $set: { estado_disp: 'inactivo' } });
  res.json(await uno('recurso', { id_recurso: id }));
});

// ---------- Reservas ----------
function validarReserva(b) {
  if (!toId(b.id_recurso)) return 'id_recurso es obligatorio.';
  if (!b.fecha || !isValidDate(b.fecha)) return 'fecha es obligatoria con formato YYYY-MM-DD.';
  if (!TIME_RE.test(b.hora_inicio || '') || !TIME_RE.test(b.hora_fin || ''))
    return 'hora_inicio y hora_fin son obligatorias con formato HH:MM.';
  if (b.hora_inicio >= b.hora_fin) return 'hora_inicio debe ser menor que hora_fin.';
  return null;
}

// Regla principal: busca una reserva activa del mismo recurso/fecha que se cruce
// (existente.inicio < nuevo.fin  Y  existente.fin > nuevo.inicio)
function buscarSolape(id_recurso, fecha, ini, fin, excluirId = 0) {
  return uno('reserva', {
    id_recurso, fecha, estado: 'activa', id_reserva: { $ne: excluirId },
    hora_inicio: { $lt: fin }, hora_fin: { $gt: ini },
  });
}

// Regla secundaria: recurso inactivo o en mantenimiento -> 400
async function chequearRecurso(res, id_recurso) {
  const r = await uno('recurso', { id_recurso });
  if (!r) { err(res, 404, 'Recurso no encontrado.'); return false; }
  if (r.estado_disp !== 'disponible') {
    err(res, 400, `El recurso "${r.nombre}" no está disponible (${r.estado_disp}).`);
    return false;
  }
  return true;
}

function conflicto(res, s) {
  return err(res, 409,
    `El recurso ya está reservado de ${s.hora_inicio} a ${s.hora_fin}. Elige otra franja.`,
    { reserva_en_conflicto: s.id_reserva });
}

app.get('/api/reservations', auth, async (req, res) => {
  const { resourceId, date, userId } = req.query;
  const filtro = req.user.rol === 'encargado' ? {} : { id_usuario: req.user.id_usuario };
  if (resourceId !== undefined) {
    if (!toId(resourceId)) return err(res, 400, 'resourceId inválido.');
    filtro.id_recurso = Number(resourceId);
  }
  if (date !== undefined) {
    if (!isValidDate(date)) return err(res, 400, 'date debe tener formato YYYY-MM-DD.');
    filtro.fecha = date;
  }
  if (userId !== undefined) {
    if (!toId(userId)) return err(res, 400, 'userId inválido.');
    // Un estudiante solo puede consultar sus propias reservas
    if (req.user.rol !== 'encargado' && Number(userId) !== req.user.id_usuario)
      return err(res, 403, 'Solo puedes consultar tus propias reservas.');
    filtro.id_usuario = Number(userId);
  }
  res.json(await lista('reserva', filtro, { fecha: 1, hora_inicio: 1 }));
});

app.post('/api/reservations', auth, async (req, res) => {
  const b = req.body || {};
  const e = validarReserva(b);
  if (e) return err(res, 400, e);
  const id_recurso = Number(b.id_recurso);
  if (!(await chequearRecurso(res, id_recurso))) return;

  // Exclusión mutua: validar solape y guardar ocurren juntos, sin que otra reserva se cuele en medio
  const out = await conBloqueo(async () => {
    const s = await buscarSolape(id_recurso, b.fecha, b.hora_inicio, b.hora_fin);
    if (s) return { solape: s };
    return { doc: await insertar('reserva', 'id_reserva', {
      id_usuario: req.user.id_usuario, id_recurso, fecha: b.fecha,
      hora_inicio: b.hora_inicio, hora_fin: b.hora_fin, estado: 'activa',
    }) };
  });
  if (out.solape) return conflicto(res, out.solape);
  res.status(201).json(out.doc);
});

// Editar reserva (fecha/horas). Solo el dueño o el encargado.
app.put('/api/reservations/:id', auth, async (req, res) => {
  const id = toId(req.params.id);
  const actual = id && await uno('reserva', { id_reserva: id });
  if (!actual) return err(res, 404, 'Reserva no encontrada.');
  if (req.user.rol !== 'encargado' && actual.id_usuario !== req.user.id_usuario)
    return err(res, 403, 'Solo puedes editar tus propias reservas.');
  if (actual.estado !== 'activa') return err(res, 400, 'No se puede editar una reserva cancelada.');

  const b = { fecha: actual.fecha, hora_inicio: actual.hora_inicio, hora_fin: actual.hora_fin,
              ...(req.body || {}), id_recurso: actual.id_recurso };
  const e = validarReserva(b);
  if (e) return err(res, 400, e);

  if (!(await chequearRecurso(res, actual.id_recurso))) return;
  const s = await conBloqueo(async () => {
    const x = await buscarSolape(actual.id_recurso, b.fecha, b.hora_inicio, b.hora_fin, id);
    if (!x) await col('reserva').updateOne({ id_reserva: id },
      { $set: { fecha: b.fecha, hora_inicio: b.hora_inicio, hora_fin: b.hora_fin } });
    return x;
  });
  if (s) return conflicto(res, s);
  res.json(await uno('reserva', { id_reserva: id }));
});

app.patch('/api/reservations/:id/cancel', auth, async (req, res) => {
  const id = toId(req.params.id);
  const actual = id && await uno('reserva', { id_reserva: id });
  if (!actual) return err(res, 404, 'Reserva no encontrada.');
  if (req.user.rol !== 'encargado' && actual.id_usuario !== req.user.id_usuario)
    return err(res, 403, 'Solo puedes cancelar tus propias reservas.');
  if (actual.estado === 'cancelada') return err(res, 400, 'La reserva ya estaba cancelada.');
  await col('reserva').updateOne({ id_reserva: id }, { $set: { estado: 'cancelada' } });
  res.json(await uno('reserva', { id_reserva: id }));
});

// ---------- 404 y errores ----------
app.use((req, res) => err(res, 404, 'Ruta no encontrada.'));
app.use((e, req, res, next) => {
  if (e.type === 'entity.parse.failed') return err(res, 400, 'JSON inválido en el cuerpo de la solicitud.');
  console.error('Error de API:', e.name, e.code || '');
  err(res, 500, 'Error interno del servidor.');
});

module.exports = app;
