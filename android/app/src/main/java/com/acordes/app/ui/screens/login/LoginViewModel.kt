package com.acordes.app.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acordes.app.data.remote.ApiClient
import com.acordes.app.data.repository.AcordesRepository
import com.acordes.app.data.session.SessionManager
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var url by mutableStateOf(ApiClient.baseUrl)
    var nombre by mutableStateOf("")
    var modoRegistro by mutableStateOf(false)
    var tipoAcceso by mutableStateOf("estudiante")
    var mensaje by mutableStateOf(""); private set
    var correo by mutableStateOf("")
    var clave by mutableStateOf("")
    var error by mutableStateOf(""); private set
    var conectado by mutableStateOf<Boolean?>(null); private set
    var probando by mutableStateOf(false); private set
    var ingresando by mutableStateOf(false); private set
    init { probarConexion() }
    fun probarConexion() { viewModelScope.launch {
        probando = true
        conectado = AcordesRepository.probarConexion(url)
        probando = false
    } }
    fun registrar() { viewModelScope.launch {
        ingresando = true; error = ""; mensaje = ""
        try {
            if (nombre.trim().length < 2) throw IllegalArgumentException("Escribe tu nombre completo")
            if (clave.length < 8) throw IllegalArgumentException("La contraseña debe tener al menos 8 caracteres")
            require(ApiClient.setUrl(url)) { "Dirección del servidor inválida" }
            AcordesRepository.registrar(nombre.trim(), correo.trim(), clave)
            clave = ""; modoRegistro = false
            mensaje = "Cuenta creada correctamente. Inicia sesión con tu correo."
        } catch (e: Exception) { error = e.message ?: "No fue posible crear la cuenta" }
        finally { ingresando = false }
    } }
    fun entrar(onExito: () -> Unit) { viewModelScope.launch {
        ingresando = true; error = ""
        try {
            require(ApiClient.setUrl(url)) { "Dirección del servidor inválida" }
            val r = AcordesRepository.login(correo.trim(), clave, tipoAcceso)
            SessionManager.iniciar(r.usuario.id_usuario, r.usuario.nombre, r.usuario.rol, r.token)
            clave = ""; onExito()
        } catch (e: Exception) { error = e.message ?: "No fue posible iniciar sesión" }
        finally { ingresando = false }
    } }
}
