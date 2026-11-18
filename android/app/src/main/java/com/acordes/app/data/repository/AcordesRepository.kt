package com.acordes.app.data.repository

import com.acordes.app.data.model.*
import com.acordes.app.data.remote.ApiClient
import com.acordes.app.data.remote.unwrap
import com.acordes.app.data.session.SessionManager
import java.time.LocalDate

/**
 * Única puerta de acceso a los datos. Los ViewModels hablan con el repositorio
 * y nunca con Retrofit directamente.
 */
object AcordesRepository {
    private val api get() = ApiClient.api

    suspend fun logout() { try { api.logout().unwrap() } finally { SessionManager.cerrar() } }

    suspend fun registrar(nombre: String, correo: String, clave: String): RegistroResponse =
        api.registrar(RegistroRequest(nombre, correo, clave)).unwrap()
    suspend fun login(correo: String, clave: String, tipoAcceso: String): LoginResponse =
        api.login(LoginRequest(correo, clave, tipoAcceso)).unwrap()
    suspend fun usuarios(): List<Usuario> = api.usuarios().unwrap()
    suspend fun crearUsuario(nombre: String, correo: String, clave: String, rol: String): Usuario =
        api.crearUsuario(NuevoUsuario(nombre, correo, clave, rol)).unwrap()
    suspend fun cambiarUsuario(id: Int, rol: String?, activo: Boolean?): Usuario =
        api.cambiarUsuario(id, CambioUsuario(rol, activo)).unwrap()
    suspend fun crearRecurso(nombre: String, tipo: String, ubicacion: String): Recurso =
        api.crearRecurso(NuevoRecurso(nombre, tipo, ubicacion = ubicacion)).unwrap()
    suspend fun desactivarRecurso(id: Int): Recurso = api.desactivarRecurso(id).unwrap()

    suspend fun probarConexion(url: String): Boolean =
        if (!ApiClient.setUrl(url)) false
        else try { api.health().unwrap(); true } catch (e: Exception) { false }

    suspend fun recursos(tipo: String?, fecha: LocalDate?): List<Recurso> =
        api.recursos(tipo, fecha?.toString()).unwrap()

    suspend fun recurso(id: Int): Recurso = api.recurso(id).unwrap()

    suspend fun franjasOcupadas(idRecurso: Int, fecha: LocalDate): List<Franja> =
        api.disponibilidad(idRecurso, fecha.toString()).unwrap().franjas_ocupadas

    suspend fun reservar(idRecurso: Int, fecha: LocalDate, inicio: String, fin: String): Reserva =
        api.reservar(NuevaReserva(idRecurso, fecha.toString(), inicio, fin)).unwrap()

    suspend fun todasReservas(): List<Reserva> = api.todasReservas().unwrap()

    suspend fun misReservas(): List<Reserva> = api.misReservas(SessionManager.userId).unwrap()

    suspend fun cancelar(idReserva: Int): Reserva = api.cancelar(idReserva).unwrap()
}
