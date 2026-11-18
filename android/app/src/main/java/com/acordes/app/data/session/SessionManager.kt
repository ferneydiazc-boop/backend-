package com.acordes.app.data.session

object SessionManager {
    var userId: Int = 0; private set
    var userName: String = ""; private set
    var rol: String = ""; private set
    var token: String = ""; private set
    fun iniciar(id: Int, nombre: String, nuevoRol: String, nuevoToken: String) {
        userId = id; userName = nombre; rol = nuevoRol; token = nuevoToken
    }
    fun cerrar() { userId = 0; userName = ""; rol = ""; token = "" }
}
