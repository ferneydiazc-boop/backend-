package com.acordes.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.acordes.app.data.model.Usuario
import com.acordes.app.data.model.Recurso
import com.acordes.app.data.model.Reserva
import com.acordes.app.data.repository.AcordesRepository
import com.acordes.app.data.session.SessionManager
import kotlinx.coroutines.launch

@Composable
fun AdminScreen() {
    if (SessionManager.rol != "encargado") return
    val scope = rememberCoroutineScope()
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var recursos by remember { mutableStateOf<List<Recurso>>(emptyList()) }
    var reservas by remember { mutableStateOf<List<Reserva>>(emptyList()) }
    var confirmacion by remember { mutableStateOf<Int?>(null) }
    var aviso by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("estudiante") }
    var recursoNombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("sala") }
    var ubicacion by remember { mutableStateOf("") }
    fun actualizar() { scope.launch { try {
        usuarios = AcordesRepository.usuarios(); recursos = AcordesRepository.recursos(null, null); reservas = AcordesRepository.todasReservas()
    } catch (e: Exception) { aviso = e.message ?: "Error de conexión" } } }
    LaunchedEffect(Unit) { actualizar() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Panel de administración", style = MaterialTheme.typography.headlineSmall)
        Text("Bienvenida, ${SessionManager.userName}. Gestiona la academia desde aquí.")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(Modifier.weight(1f)) { Column(Modifier.padding(12.dp)) { Text("Usuarios"); Text("${usuarios.size}", style = MaterialTheme.typography.headlineMedium) } }
            Card(Modifier.weight(1f)) { Column(Modifier.padding(12.dp)) { Text("Recursos"); Text("${recursos.size}", style = MaterialTheme.typography.headlineMedium) } }
            Card(Modifier.weight(1f)) { Column(Modifier.padding(12.dp)) { Text("Activas"); Text("${reservas.count { it.estado == "activa" }}", style = MaterialTheme.typography.headlineMedium) } }
        }
        Spacer(Modifier.height(16.dp))
        Text("Todas las reservas", style = MaterialTheme.typography.titleLarge)
        Text("Puedes cancelar reservas de cualquier estudiante por motivos administrativos.")
        reservas.forEach { r ->
            val propietario = usuarios.firstOrNull { it.id_usuario == r.id_usuario }?.nombre ?: "Usuario #${r.id_usuario}"
            val recurso = recursos.firstOrNull { it.id_recurso == r.id_recurso }?.nombre ?: "Recurso #${r.id_recurso}"
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("$recurso · $propietario", style = MaterialTheme.typography.titleSmall)
                    Text("${r.fecha} · ${r.hora_inicio}–${r.hora_fin} · ${r.estado}")
                    if (r.estado == "activa") TextButton(onClick = { confirmacion = r.id_reserva }) { Text("Cancelar reserva") }
                }
            }
        }
        if (confirmacion != null) AlertDialog(
            onDismissRequest = { confirmacion = null },
            title = { Text("Confirmar cancelación") },
            text = { Text("Esta reserva será cancelada y su horario quedará disponible.") },
            confirmButton = { TextButton(onClick = {
                val id = confirmacion ?: return@TextButton
                confirmacion = null
                scope.launch { try { AcordesRepository.cancelar(id); aviso = "Reserva cancelada"; actualizar() }
                    catch (e: Exception) { aviso = e.message ?: "No se pudo cancelar" } }
            }) { Text("Sí, cancelar") } },
            dismissButton = { TextButton(onClick = { confirmacion = null }) { Text("Volver") } }
        )
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        if (aviso.isNotBlank()) Text(aviso)
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        Text("Crear usuario", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(correo, { correo = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(clave, { clave = it }, label = { Text("Contraseña (mín. 8)") }, modifier = Modifier.fillMaxWidth(), visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation())
        Row { listOf("estudiante", "instructor").forEach { r ->
            FilterChip(selected = rol == r, onClick = { rol = r }, label = { Text(r.take(3)) }, modifier = Modifier.padding(end = 4.dp))
        } }
        Button(onClick = { scope.launch { try {
            AcordesRepository.crearUsuario(nombre, correo, clave, rol)
            nombre = ""; correo = ""; clave = ""; aviso = "Usuario creado"; actualizar()
        } catch (e: Exception) { aviso = e.message ?: "Error" } } }) { Text("Crear usuario") }
        Text("Usuarios registrados", style = MaterialTheme.typography.titleMedium)
        usuarios.forEach { u ->
            Text("${u.nombre} · ${u.rol} · ${if (u.activo) "Activo" else "Inactivo"}")
            Row { if (u.id_usuario != SessionManager.userId) {
                TextButton(onClick = { scope.launch { try { AcordesRepository.cambiarUsuario(u.id_usuario, null, !u.activo); actualizar() } catch (e: Exception) { aviso = e.message ?: "Error" } } }) { Text(if (u.activo) "Desactivar" else "Activar") }
                TextButton(onClick = { scope.launch { try { AcordesRepository.cambiarUsuario(u.id_usuario, "instructor", null); actualizar() } catch (e: Exception) { aviso = e.message ?: "Error" } } }) { Text("Instructor") }
                TextButton(onClick = { scope.launch { try { AcordesRepository.cambiarUsuario(u.id_usuario, "estudiante", null); actualizar() } catch (e: Exception) { aviso = e.message ?: "Error" } } }) { Text("Estudiante") }
            } }
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        Text("Crear sala o instrumento", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(recursoNombre, { recursoNombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(ubicacion, { ubicacion = it }, label = { Text("Ubicación") }, modifier = Modifier.fillMaxWidth())
        Row { listOf("sala", "instrumento").forEach { t -> FilterChip(selected = tipo == t, onClick = { tipo = t }, label = { Text(t) }, modifier = Modifier.padding(end = 8.dp)) } }
        Button(onClick = { scope.launch { try {
            AcordesRepository.crearRecurso(recursoNombre, tipo, ubicacion)
            recursoNombre = ""; ubicacion = ""; aviso = "Recurso creado"; actualizar()
        } catch (e: Exception) { aviso = e.message ?: "Error" } } }) { Text("Crear recurso") }
        recursos.forEach { r -> Row {
            Text("${r.nombre} (${r.estado_disp})", modifier = Modifier.weight(1f))
            TextButton(onClick = { scope.launch { try { AcordesRepository.desactivarRecurso(r.id_recurso); actualizar() } catch (e: Exception) { aviso = e.message ?: "Error" } } }) { Text("Desactivar") }
        } }
    }
}
