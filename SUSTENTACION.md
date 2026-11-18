> Estado 17/11/2026: el backend ya está Live en https://acordes-del-barrio-api.onrender.com/. Los datos originales se migraron a Atlas. Sesiones persistentes con Bearer; no se usa X-User-Id. Consulta REVISION_DESARROLLO.md para el estado comprobado; este documento conserva contenido histórico y no acredita pruebas visuales ni validación del cliente.

> Actualización 17/11/2026: consulta REVISION_DESARROLLO.md y DESPLIEGUE.md para el estado comprobado. Las evidencias y cifras anteriores son históricas; no acreditan esta revisión. La autenticación vigente usa Bearer y las sesiones se guardan en MongoDB. No ejecutes instrucciones antiguas de borrado de bases.

# Guion de sustentación — Acordes del Barrio

## 0. Antes de entrar (checklist)
- [ ] Iniciar backend con MongoDB conectado; conservar los datos existentes.
- [ ] Emulador abierto y app ya instalada (Run una vez antes)
- [ ] Postman con la colección importada; `evidencia/` a mano
- [ ] Repositorio abierto en el navegador
- [ ] Ficha del problema (Etapa 1) y modelo de datos (Etapa 2) a mano

## 1. Presentación (1 min)
Problema: en la escuela de música las salas e instrumentos se reservan sin control y se cruzan los horarios.
Solución: API REST + app Android que muestra la disponibilidad y bloquea reservas que se superponen.
Usuarios: estudiante (reserva) y encargado (administra recursos).

## 2. Demostración funcional (5 min)
1. Mostrar `/api/health` en el navegador.
2. App → entrar como **Carlos** → lista de recursos → filtrar "Salas".
3. Abrir **Sala de ensayo 1** → reservar 09:00–10:00 → éxito.
4. Volver, entrar como **Ana** → misma sala, 09:30–11:00 → **409** con mensaje de franja ocupada.
5. Ana reserva 10:00–11:00 → **funciona** (franja contigua no es conflicto).
6. Abrir **Batería** → reservar → **400** (mantenimiento).
7. Carlos → Mis reservas → **Cancelar** → la franja vuelve a quedar libre.
8. Postman/Newman: 32 requests, 46 aserciones; mostrar `evidencia/resultado-newman.txt` y `npm test` (13 pruebas).

## 3. Explicación técnica (2 min)
- Modelo: colecciones `usuario`, `recurso`, `reserva` con índices únicos (id, correo) y validación de estados y horas en la API; la reserva guarda `id_usuario` e `id_recurso` como referencias.
- Solape: hay conflicto si `inicio_existente < fin_nuevo` Y `fin_existente > inicio_nuevo`, solo con reservas activas del mismo recurso y fecha.
- Concurrencia: un bloqueo (mutex) en el servidor hace que validar el solape y guardar ocurran juntos, sin que otra reserva se cuele en medio.
- Códigos HTTP: 201, 400, 401, 403, 404, 409.
- App: Retrofit + Gson; un interceptor agrega `X-User-Id`; `unwrap()` convierte los errores de la API en mensajes para el usuario.

## 4. Preguntas probables y respuestas
| Pregunta | Respuesta |
|---|---|
| ¿Por qué 409 y no 400? | 409 indica conflicto con el estado actual del recurso; 400 es para solicitudes mal formadas o recurso no disponible. |
| ¿Qué pasa si dos personas reservan a la vez? | El bloqueo serializa las reservas: la segunda ve la reserva de la primera y recibe 409. |
| ¿Por qué DELETE no borra? | Borrado lógico (`inactivo`) para conservar el historial de reservas. |
| ¿Por qué la autenticación es simple? | Decisión de alcance: cabecera `X-User-Id`. Mejora futura: login con JWT y contraseñas con hash. |
| ¿Por qué MongoDB? | Base documental flexible y con nube gratuita (Atlas). Se usan 3 colecciones (usuario, recurso, reserva) más una de contadores para ids numéricos. Las reglas de negocio viven en la API, no en la base. |
| ¿Cómo se conecta la app con la API? | Retrofit; en emulador la URL es `10.0.2.2`; HTTP permitido solo para ese host con `network_security_config`. |
| ¿Cómo está organizada la app? | MVVM con Repository: la pantalla (Compose) solo dibuja y delega al ViewModel; este llama al Repository, que es el único que usa Retrofit. Hay paquetes `data`, `ui`, `util`; cada pantalla tiene su Screen y su ViewModel. |
| ¿Por qué ViewModel? | Conserva el estado al rotar la pantalla y saca la lógica de la UI, así se puede probar y mantener por separado. |
| ¿Dónde está desplegada la API? | En Render (Node) con MongoDB Atlas; la app usa la URL https de la nube, no el PC. |
| ¿Qué mejorarías? | Login real, notificaciones, calendario visual, despliegue en la nube, edición de reservas en la app, validar estado del recurso al editar una reserva. |
| ¿Qué dificultades tuviste? | Evitar solapes (límites contiguos), y conectar emulador con el servidor local. |

## 5. Cierre (30 s)
Resumen: se cumplió la regla de negocio principal (sin reservas superpuestas), con API probada, app funcional y evidencia de pruebas.
