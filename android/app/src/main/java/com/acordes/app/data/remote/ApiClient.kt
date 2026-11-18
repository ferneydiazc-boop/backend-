package com.acordes.app.data.remote

import android.content.SharedPreferences
import com.acordes.app.BuildConfig
import okhttp3.HttpUrl
import com.acordes.app.data.session.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Configura Retrofit/OkHttp y guarda la dirección del servidor (editable desde la app). */
object ApiClient {
    // HTTPS verificado en Render. El servidor local sigue disponible para desarrollo.
    const val URL_DESARROLLO_LOCAL = "http://10.0.2.2:3000/"
    val URL_POR_DEFECTO = BuildConfig.API_URL

    var baseUrl: String = URL_POR_DEFECTO
        private set
    var api: AcordesApi = construir(URL_POR_DEFECTO)
        private set
    private var prefs: SharedPreferences? = null

    fun iniciar(p: SharedPreferences) {
        prefs = p
        val guardada = p.getString("url", null)
        val migrarLocal = !p.getBoolean("backend_render_v1", false) &&
            guardada == URL_DESARROLLO_LOCAL && URL_POR_DEFECTO.startsWith("https://")
        val inicial = if (migrarLocal) URL_POR_DEFECTO else guardada ?: URL_POR_DEFECTO
        if (!setUrl(inicial)) setUrl(URL_POR_DEFECTO)
        p.edit().putBoolean("backend_render_v1", true).apply()
    }

    /** Normaliza y aplica la dirección. Devuelve false si no es válida. */
    fun setUrl(raw: String): Boolean {
        var u = raw.trim()
        if (u.isEmpty()) return false
        if (!u.startsWith("http://") && !u.startsWith("https://")) u = "http://$u"
        if (!u.endsWith("/")) u += "/"
        val parsed = HttpUrl.parse(u) ?: return false
        if (parsed.username().isNotEmpty() || parsed.password().isNotEmpty() || parsed.query() != null || parsed.fragment() != null) return false
        if (!BuildConfig.DEBUG && !parsed.isHttps()) return false
        return try {
            if (baseUrl != u) SessionManager.cerrar()
            api = construir(u); baseUrl = u
            prefs?.edit()?.putString("url", u)?.apply(); true
        } catch (e: Exception) { false }
    }

    private fun construir(url: String): AcordesApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS).readTimeout(90, TimeUnit.SECONDS) // la nube gratuita tarda en "despertar"
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder()
                    .apply { if (chain.request().url().encodedPath() !in listOf("/api/health", "/api/auth/login", "/api/auth/register") && SessionManager.token.isNotEmpty()) header("Authorization", "Bearer ${SessionManager.token}") }.build())
            }.build()
        return Retrofit.Builder().baseUrl(url).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(AcordesApi::class.java)
    }
}
