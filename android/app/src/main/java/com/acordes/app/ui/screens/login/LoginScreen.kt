package com.acordes.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acordes.app.ui.theme.*

@Composable
fun LoginScreen(onEntrar: () -> Unit, vm: LoginViewModel = viewModel()) {
    var mostrarServidor by remember { mutableStateOf(false) }
    val oscuro = Color(0xFF21154C)
    val morado = Color(0xFF6641D8)
    val gris = Color(0xFF6E6683)
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(oscuro, Color(0xFF4426A1), morado)))
            .verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(38.dp))
        Text("♫", color = Color(0xFFFFD78C), fontSize = 66.sp, fontWeight = FontWeight.Bold)
        Text("Entregable Final", color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.Bold)
        Text("ACORDES DEL BARRIO", color = Color.White.copy(alpha = .8f), fontSize = 12.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        Text("Tu espacio para crear, ensayar y compartir música", color = Color.White.copy(alpha = .85f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(25.dp))
        Card(shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(22.dp)) {
                Text(if (vm.modoRegistro) "Crea tu cuenta" else "¡Qué bueno verte!", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = oscuro)
                Text(if (vm.modoRegistro) "Regístrate como estudiante para comenzar." else "Selecciona el tipo de acceso e ingresa tus credenciales.", color = gris, fontSize = 13.sp)
                if (!vm.modoRegistro) {
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = vm.tipoAcceso == "estudiante", onClick = { vm.tipoAcceso = "estudiante" }, label = { Text("Estudiante") })
                        FilterChip(selected = vm.tipoAcceso == "encargado", onClick = { vm.tipoAcceso = "encargado" }, label = { Text("Administrador") })
                    }
                    Text(if (vm.tipoAcceso == "encargado") "Acceso exclusivo para la cuenta administradora autorizada." else "Accede a tus salas, instrumentos y reservas.", color = gris, fontSize = 12.sp)
                }
                Spacer(Modifier.height(20.dp))
                if (vm.modoRegistro) {
                    OutlinedTextField(vm.nombre, { vm.nombre = it }, label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(13.dp))
                    Spacer(Modifier.height(10.dp))
                }
                OutlinedTextField(vm.correo, { vm.correo = it }, label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(13.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(vm.clave, { vm.clave = it }, label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(13.dp),
                    visualTransformation = PasswordVisualTransformation())
                if (vm.modoRegistro) Text("Mínimo 8 caracteres. Tu cuenta tendrá permisos de estudiante.", color = gris, fontSize = 12.sp)
                if (vm.error.isNotBlank()) Text(vm.error, color = Color(0xFFB3261E), fontSize = 13.sp)
                if (vm.mensaje.isNotBlank()) Text(vm.mensaje, color = Color(0xFF16744A), fontSize = 13.sp)
                Spacer(Modifier.height(18.dp))
                Button(onClick = { if (vm.modoRegistro) vm.registrar() else vm.entrar(onEntrar) },
                    enabled = !vm.ingresando && vm.correo.isNotBlank() && vm.clave.isNotBlank() && (!vm.modoRegistro || vm.nombre.isNotBlank()),
                    modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = morado)) {
                    Text(if (vm.ingresando) "Procesando…" else if (vm.modoRegistro) "Crear mi cuenta" else "Iniciar sesión", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = { vm.modoRegistro = !vm.modoRegistro; vm.tipoAcceso = "estudiante" }) {
                    Text(if (vm.modoRegistro) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate", color = morado)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        val estado = when(vm.conectado) { true -> "● Servidor conectado"; false -> "● Sin conexión al servidor"; null -> "● Comprobando servidor…" }
        Text(estado, color = if (vm.conectado == true) Color(0xFFAEF0BE) else Color.White, fontSize = 13.sp)
        TextButton(onClick = { mostrarServidor = !mostrarServidor }) { Text("⚙ Configurar servidor", color = Color.White) }
        if (mostrarServidor) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Dirección del servidor", fontWeight = FontWeight.Bold)
                    Text("Emulador: http://10.0.2.2:3000/", fontSize = 12.sp)
                    Text("Celular: http://IP_DE_TU_PC:3000/", fontSize = 12.sp)
                    OutlinedTextField(vm.url, { vm.url = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { vm.probarConexion() }, enabled = !vm.probando, modifier = Modifier.fillMaxWidth()) {
                        Text(if (vm.probando) "Probando…" else "Guardar y comprobar")
                    }
                }
            }
        }
        Spacer(Modifier.height(26.dp))
    }
}
