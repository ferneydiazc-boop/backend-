package com.acordes.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.acordes.app.data.session.SessionManager
import com.acordes.app.ui.components.Cabecera
import com.acordes.app.ui.screens.detalle.DetalleScreen
import com.acordes.app.ui.screens.login.LoginScreen
import com.acordes.app.ui.screens.admin.AdminScreen
import com.acordes.app.ui.screens.recursos.RecursosScreen
import com.acordes.app.ui.screens.reservas.MisReservasScreen
import com.acordes.app.ui.theme.Fondo
import java.time.LocalDate
import kotlinx.coroutines.launch
import com.acordes.app.data.repository.AcordesRepository

object Rutas {
    const val LOGIN = "login"
    const val ADMIN = "admin"
    const val RECURSOS = "recursos"
    const val RESERVAS = "reservas"
    const val DETALLE = "detalle/{id}"
    fun detalle(id: Int) = "detalle/$id"
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    var fecha by remember { mutableStateOf(LocalDate.now()) }   // día elegido, compartido entre lista y detalle
    val ruta = nav.currentBackStackEntryAsState().value?.destination?.route
    val conBarraInferior = ruta == Rutas.RECURSOS || ruta == Rutas.RESERVAS || ruta == Rutas.ADMIN

    Scaffold(
        containerColor = Fondo,
        topBar = {
            if (ruta != null && ruta != Rutas.LOGIN) Cabecera {
                scope.launch {
                    try { AcordesRepository.logout() } catch (_: Exception) { SessionManager.cerrar() }
                    nav.navigate(Rutas.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } }
                }
            }
        },
        bottomBar = {
            if (conBarraInferior) NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                NavigationBarItem(
                    selected = ruta == Rutas.RECURSOS, label = { Text("Recursos") },
                    icon = { Text("🎼", fontSize = 22.sp) },
                    onClick = { nav.navigate(Rutas.RECURSOS) { popUpTo(Rutas.RECURSOS); launchSingleTop = true } }
                )
                if (SessionManager.rol == "encargado") NavigationBarItem(
                    selected = ruta == Rutas.ADMIN, label = { Text("Administrar") },
                    icon = { Text("⚙", fontSize = 22.sp) },
                    onClick = { nav.navigate(Rutas.ADMIN) { launchSingleTop = true } }
                )
                NavigationBarItem(
                    selected = ruta == Rutas.RESERVAS, label = { Text("Mis reservas") },
                    icon = { Text("📅", fontSize = 22.sp) },
                    onClick = { nav.navigate(Rutas.RESERVAS) { popUpTo(Rutas.RECURSOS); launchSingleTop = true } }
                )
            }
        }
    ) { pad ->
        NavHost(nav, startDestination = Rutas.LOGIN, modifier = Modifier.padding(pad)) {
            composable(Rutas.LOGIN) {
                LoginScreen(onEntrar = { nav.navigate(if (SessionManager.rol == "encargado") Rutas.ADMIN else Rutas.RECURSOS) { popUpTo(Rutas.LOGIN) { inclusive = true } } })
            }
            composable(Rutas.RECURSOS) {
                RecursosScreen(
                    fecha = fecha,
                    onFecha = { nuevaFecha -> fecha = nuevaFecha },
                    onDetalle = { recurso ->
                        nav.navigate(Rutas.detalle(recurso.id_recurso))
                    }
                )
            }
            composable(Rutas.RESERVAS) { MisReservasScreen() }
            composable(Rutas.ADMIN) { if (SessionManager.rol == "encargado") AdminScreen() }
            composable(Rutas.DETALLE, arguments = listOf(navArgument("id") { type = NavType.IntType })) { e ->
                DetalleScreen(
                    idRecurso = e.arguments?.getInt("id") ?: return@composable,
                    fecha = fecha,
                    onFecha = { nuevaFecha -> fecha = nuevaFecha },
                    onVolver = { nav.popBackStack() }
                )
            }
        }
    }
}
