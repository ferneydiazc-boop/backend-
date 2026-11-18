package com.acordes.app.data.remote

import com.acordes.app.data.model.ApiError
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException

/** Devuelve el cuerpo, o lanza una excepción con el mensaje que envía la API (400/403/409...). */
fun <T> Response<T>.unwrap(): T {
    if (isSuccessful) return body()!!
    val msg = try { Gson().fromJson(errorBody()?.string(), ApiError::class.java)?.error } catch (e: Exception) { null }
    throw Exception("(${code()}) ${msg ?: "Error del servidor"}")
}

/** Convierte cualquier excepción en un mensaje entendible para el usuario. */
fun Throwable.amigable(): String =
    if (this is IOException)
        "No se pudo conectar con el servidor.\nRevisa tu internet y la dirección en ⚙ Servidor."
    else message ?: "Error inesperado"
