package com.acordes.app.data.model

// Modelos de datos: reflejan el JSON que devuelve la API (colecciones de MongoDB).

data class Recurso(
    val id_recurso: Int, val nombre: String, val tipo: String,
    val estado_disp: String, val ubicacion: String?, val reservado_en_fecha: Boolean? = null
)

data class Franja(val id_reserva: Int, val hora_inicio: String, val hora_fin: String)

data class Disponibilidad(
    val id_recurso: Int, val fecha: String, val estado_disp: String, val franjas_ocupadas: List<Franja>
)

data class Reserva(
    val id_reserva: Int, val id_usuario: Int, val id_recurso: Int,
    val fecha: String, val hora_inicio: String, val hora_fin: String, val estado: String
)

data class NuevaReserva(val id_recurso: Int, val fecha: String, val hora_inicio: String, val hora_fin: String)

data class ApiError(val error: String?)
data class Health(val status: String?)

/** Usuarios de la demo (coinciden con los datos semilla de la base). */
data class UsuarioDemo(val id: Int, val nombre: String, val rol: String)

data class Usuario(val id_usuario: Int, val nombre: String, val correo: String, val rol: String, val activo: Boolean = true)
data class RegistroRequest(val nombre: String, val correo: String, val clave: String)
data class RegistroResponse(val usuario: Usuario, val mensaje: String)
data class LoginRequest(val correo: String, val clave: String, val tipoAcceso: String)
data class LoginResponse(val token: String, val usuario: Usuario)
data class NuevoUsuario(val nombre: String, val correo: String, val clave: String, val rol: String)
data class CambioUsuario(val rol: String? = null, val activo: Boolean? = null)
data class NuevoRecurso(val nombre: String, val tipo: String, val estado_disp: String = "disponible", val ubicacion: String? = null)
