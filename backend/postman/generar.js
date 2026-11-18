// Genera postman/Acordes-del-Barrio.postman_collection.json
const fs = require('fs');
const path = require('path');

const req = (name, method, url, { user, body, tests = [], pre = [] } = {}) => ({
  name,
  event: [
    ...(pre.length ? [{ listen: 'prerequest', script: { type: 'text/javascript', exec: pre } }] : []),
    { listen: 'test', script: { type: 'text/javascript', exec: tests } },
  ],
  request: {
    method,
    header: [
      { key: 'Content-Type', value: 'application/json' },
      ...(user ? [{ key: 'Authorization', value: `Bearer {{${user}Token}}` }] : []),
    ],
    ...(body ? { body: { mode: 'raw', raw: JSON.stringify(body, null, 2) } } : {}),
    url: { raw: `{{baseUrl}}${url}`, host: ['{{baseUrl}}'], path: url.split('?')[0].split('/').filter(Boolean),
      ...(url.includes('?') ? { query: url.split('?')[1].split('&').map((q) => { const [key, value] = q.split('='); return { key, value }; }) } : {}) },
  },
});
const status = (code, extra = []) => [`pm.test('Status ${code}', () => pm.response.to.have.status(${code}));`, ...extra];
const reserva = (b) => ({ id_recurso: 1, fecha: '{{fecha}}', ...b });

const collection = {
  info: {
    name: 'Acordes del Barrio - API',
    description: 'Colección de pruebas. Ejecutar en orden con el Collection Runner (o Newman). Requiere la API en {{baseUrl}} con datos semilla.',
    schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
  },
  variable: [
    { key: 'baseUrl', value: 'http://localhost:3000' },
    { key: 'encargadoId', value: '1' },
    { key: 'estudianteId', value: '2' },
    { key: 'estudiante2Id', value: '3' },
  ],
  item: [
    { name: 'Autenticación (contraseña en variable local demoPassword)', item: [
      ...[['encargadoId','encargado@acordes.local','encargado'],['estudianteId','carlos@acordes.local','estudiante'],['estudiante2Id','ana@acordes.local','estudiante']].map(([key,correo,tipoAcceso]) =>
        req('Login '+tipoAcceso+' '+correo,'POST','/api/auth/login',{body:{correo,clave:'{{demoPassword}}',tipoAcceso},tests:status(200,["pm.collectionVariables.set('tokenKey', pm.response.json().token);".replace('tokenKey',key+'Token')])}))
    ] },
    { name: '0. Salud y seguridad', item: [
      req('Health check', 'GET', '/api/health', { tests: status(200, ["pm.test('status ok', () => pm.expect(pm.response.json().status).to.eql('ok'));"]) }),
      req('Sin token -> 401', 'GET', '/api/resources', { tests: status(401) }),
    ] },
    { name: '1. Recursos', item: [
      req('Listar recursos', 'GET', '/api/resources', { user: 'estudianteId',
        tests: status(200, ["pm.test('Es un arreglo', () => pm.expect(pm.response.json()).to.be.an('array').that.is.not.empty);"]) }),
      req('Listar solo salas', 'GET', '/api/resources?tipo=sala', { user: 'estudianteId',
        tests: status(200, ["pm.test('Todas son salas', () => pm.response.json().forEach(r => pm.expect(r.tipo).to.eql('sala')));"]) }),
      req('Listar con tipo inválido -> 400', 'GET', '/api/resources?tipo=xx', { user: 'estudianteId', tests: status(400) }),
      req('Detalle de recurso', 'GET', '/api/resources/1', { user: 'estudianteId', tests: status(200, ["pm.test('id correcto', () => pm.expect(pm.response.json().id_recurso).to.eql(1));"]) }),
      req('Detalle inexistente -> 404', 'GET', '/api/resources/9999', { user: 'estudianteId', tests: status(404) }),
      req('Crear recurso como estudiante -> 403', 'POST', '/api/resources', { user: 'estudianteId', body: { nombre: 'Micrófono', tipo: 'instrumento' }, tests: status(403) }),
      req('Crear recurso inválido -> 400', 'POST', '/api/resources', { user: 'encargadoId', body: { nombre: 'Sin tipo' }, tests: status(400) }),
      req('Crear recurso (encargado) -> 201', 'POST', '/api/resources', { user: 'encargadoId',
        body: { nombre: 'Micrófono', tipo: 'instrumento', ubicacion: 'Armario 3' },
        tests: status(201, ["pm.collectionVariables.set('recursoNuevoId', pm.response.json().id_recurso);"]) }),
      req('Editar recurso (encargado)', 'PUT', '/api/resources/{{recursoNuevoId}}', { user: 'encargadoId',
        body: { ubicacion: 'Depósito' },
        tests: status(200, ["pm.test('Ubicación actualizada', () => pm.expect(pm.response.json().ubicacion).to.eql('Depósito'));"]) }),
    ] },
    { name: '2. Reservas', item: [
      req('Preparar fecha de prueba', 'GET', '/api/health', {
        pre: ["const m = String(1 + Math.floor(Math.random()*12)).padStart(2,'0');",
              "const d = String(1 + Math.floor(Math.random()*28)).padStart(2,'0');",
              "pm.collectionVariables.set('fecha', `2027-${m}-${d}`);"],
        tests: status(200) }),
      req('Crear reserva válida -> 201', 'POST', '/api/reservations', { user: 'estudianteId',
        body: reserva({ hora_inicio: '09:00', hora_fin: '10:00' }),
        tests: status(201, ["const r = pm.response.json();",
          "pm.test('Estado activa', () => pm.expect(r.estado).to.eql('activa'));",
          "pm.collectionVariables.set('reservaId', r.id_reserva);"]) }),
      req('REGLA PRINCIPAL: solapamiento -> 409', 'POST', '/api/reservations', { user: 'estudiante2Id',
        body: reserva({ hora_inicio: '09:30', hora_fin: '10:30' }),
        tests: status(409, ["pm.test('Mensaje explicativo', () => pm.expect(pm.response.json().error).to.include('ya está reservado'));"]) }),
      req('Franja contigua (no es conflicto) -> 201', 'POST', '/api/reservations', { user: 'estudiante2Id',
        body: reserva({ hora_inicio: '10:00', hora_fin: '11:00' }),
        tests: status(201, ["pm.collectionVariables.set('reserva2Id', pm.response.json().id_reserva);"]) }),
      req('REGLA SECUNDARIA: recurso en mantenimiento -> 400', 'POST', '/api/reservations', { user: 'estudianteId',
        body: reserva({ id_recurso: 5, hora_inicio: '09:00', hora_fin: '10:00' }),
        tests: status(400, ["pm.test('Indica no disponible', () => pm.expect(pm.response.json().error).to.include('no está disponible'));"]) }),
      req('Hora fin menor que inicio -> 400', 'POST', '/api/reservations', { user: 'estudianteId',
        body: reserva({ hora_inicio: '15:00', hora_fin: '14:00' }), tests: status(400) }),
      req('Formato de fecha inválido -> 400', 'POST', '/api/reservations', { user: 'estudianteId',
        body: reserva({ fecha: '21/09/2026', hora_inicio: '15:00', hora_fin: '16:00' }), tests: status(400) }),
      req('Recurso inexistente -> 404', 'POST', '/api/reservations', { user: 'estudianteId',
        body: reserva({ id_recurso: 9999, hora_inicio: '15:00', hora_fin: '16:00' }), tests: status(404) }),
      req('Consultar por recurso y fecha', 'GET', '/api/reservations?resourceId=1&date={{fecha}}', { user: 'estudianteId',
        tests: status(200, ["pm.test('Solo reserva propia', () => pm.expect(pm.response.json()).to.have.length(1));"]) }),
      req('Disponibilidad del recurso (franjas ocupadas)', 'GET', '/api/resources/1/availability?date={{fecha}}', { user: 'estudianteId',
        tests: status(200, ["pm.test('2 franjas ocupadas', () => pm.expect(pm.response.json().franjas_ocupadas).to.have.length(2));"]) }),
      req('Mis reservas (por usuario)', 'GET', '/api/reservations?userId={{estudianteId}}', { user: 'estudianteId',
        tests: status(200, ["pm.test('Todas son mías', () => pm.response.json().forEach(r => pm.expect(r.id_usuario).to.eql(2)));"]) }),
      req('Ver reservas de otro usuario -> 403', 'GET', '/api/reservations?userId={{estudiante2Id}}', { user: 'estudianteId', tests: status(403) }),
      req('Editar reserva propia', 'PUT', '/api/reservations/{{reservaId}}', { user: 'estudianteId',
        body: { hora_fin: '09:45' },
        tests: status(200, ["pm.test('hora_fin actualizada', () => pm.expect(pm.response.json().hora_fin).to.eql('09:45'));"]) }),
      req('Editar generando solape -> 409', 'PUT', '/api/reservations/{{reservaId}}', { user: 'estudianteId',
        body: { hora_fin: '10:30' }, tests: status(409) }),
      req('Cancelar reserva ajena -> 403', 'PATCH', '/api/reservations/{{reservaId}}/cancel', { user: 'estudiante2Id', tests: status(403) }),
      req('Cancelar reserva propia -> 200', 'PATCH', '/api/reservations/{{reservaId}}/cancel', { user: 'estudianteId',
        tests: status(200, ["pm.test('Estado cancelada', () => pm.expect(pm.response.json().estado).to.eql('cancelada'));"]) }),
      req('Cancelar dos veces -> 400', 'PATCH', '/api/reservations/{{reservaId}}/cancel', { user: 'estudianteId', tests: status(400) }),
      req('Franja liberada: reservar de nuevo -> 201', 'POST', '/api/reservations', { user: 'estudiante2Id',
        body: reserva({ hora_inicio: '09:00', hora_fin: '10:00' }), tests: status(201) }),
      req('Cancelar reserva inexistente -> 404', 'PATCH', '/api/reservations/99999/cancel', { user: 'estudianteId', tests: status(404) }),
    ] },
    { name: '3. Limpieza', item: [
      req('Desactivar recurso (encargado)', 'DELETE', '/api/resources/{{recursoNuevoId}}', { user: 'encargadoId',
        tests: status(200, ["pm.test('Queda inactivo', () => pm.expect(pm.response.json().estado_disp).to.eql('inactivo'));"]) }),
      req('Recurso inactivo no se puede reservar -> 400', 'POST', '/api/reservations', { user: 'estudianteId',
        body: reserva({ id_recurso: '{{recursoNuevoId}}', hora_inicio: '09:00', hora_fin: '10:00' }),
        tests: status(400) }),
    ] },
  ],
};
// id_recurso con variable debe ir como string en JSON -> la API lo valida con toId
fs.writeFileSync(path.join(__dirname, 'Acordes-del-Barrio.postman_collection.json'), JSON.stringify(collection, null, 2));
console.log('Colección generada.');
