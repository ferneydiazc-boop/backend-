package com.acordes.app.data.remote

import com.acordes.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

/** Contrato HTTP con la API REST (Retrofit). Solo declara endpoints, no tiene lógica. */
interface AcordesApi {
    @POST("api/auth/logout") suspend fun logout(): Response<Map<String, Boolean>>
    @POST("api/auth/register") suspend fun registrar(@Body body: RegistroRequest): Response<RegistroResponse>
    @POST("api/auth/login") suspend fun login(@Body body: LoginRequest): Response<LoginResponse>
    @GET("api/users") suspend fun usuarios(): Response<List<Usuario>>
    @POST("api/users") suspend fun crearUsuario(@Body body: NuevoUsuario): Response<Usuario>
    @PATCH("api/users/{id}") suspend fun cambiarUsuario(@Path("id") id: Int, @Body body: CambioUsuario): Response<Usuario>
    @POST("api/resources") suspend fun crearRecurso(@Body body: NuevoRecurso): Response<Recurso>
    @DELETE("api/resources/{id}") suspend fun desactivarRecurso(@Path("id") id: Int): Response<Recurso>

    @GET("api/health") suspend fun health(): Response<Health>

    @GET("api/resources")
    suspend fun recursos(@Query("tipo") tipo: String?, @Query("date") date: String?): Response<List<Recurso>>

    @GET("api/resources/{id}") suspend fun recurso(@Path("id") id: Int): Response<Recurso>

    @GET("api/resources/{id}/availability")
    suspend fun disponibilidad(@Path("id") id: Int, @Query("date") date: String): Response<Disponibilidad>

    @GET("api/reservations") suspend fun todasReservas(): Response<List<Reserva>>

    @GET("api/reservations") suspend fun misReservas(@Query("userId") userId: Int): Response<List<Reserva>>

    @POST("api/reservations") suspend fun reservar(@Body body: NuevaReserva): Response<Reserva>

    @PATCH("api/reservations/{id}/cancel") suspend fun cancelar(@Path("id") id: Int): Response<Reserva>
}
