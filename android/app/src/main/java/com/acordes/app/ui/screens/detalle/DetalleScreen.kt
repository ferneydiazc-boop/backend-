package com.acordes.app.ui.screens.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acordes.app.ui.components.*
import com.acordes.app.ui.theme.*
import com.acordes.app.util.emojiDe
import com.acordes.app.util.formatoFecha
import java.time.LocalDate

@Composable
fun DetalleScreen(
    idRecurso: Int, fecha: LocalDate, onFecha: (LocalDate) -> Unit, onVolver: () -> Unit,
    vm: DetalleViewModel = viewModel()
) {
    LaunchedEffect(idRecurso) { vm.cargarRecurso(idRecurso) }
    LaunchedEffect(idRecurso, fecha) { vm.cargarFranjas(idRecurso, fecha); vm.limpiarMensaje() }

    val r = vm.recurso
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        TextButton(onClick = onVolver) { Text("← Volver a recursos") }
        if (r == null) {
            vm.errorCarga?.let { Aviso(it, Rojo) } ?: Cargando()
            return@Column
        }

        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(EsquemaClaro.primaryContainer), contentAlignment = Alignment.Center) {
                    Text(emojiDe(r), fontSize = 34.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(r.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(r.ubicacion ?: "—", color = Gris)
                }
                EstadoRecurso(r.estado_disp)
            }
        }
        if (r.estado_disp != "disponible")
            Aviso("⚠ Este recurso está en ${r.estado_disp}; la API rechazará la reserva.", Ambar, Modifier.padding(top = 10.dp))

        Titulo("1. Elige el día")
        FechaBoton(fecha, onFecha)

        Titulo("Horarios ocupados ese día")
        if (vm.franjas.isEmpty()) Pill("✅ Todo el día libre", Verde)
        else Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            vm.franjas.forEach { Pill("🔒 ${it.hora_inicio} – ${it.hora_fin}", Rojo) }
        }

        Titulo("2. Elige el horario")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HoraSelector("Desde", vm.inicio, Modifier.weight(1f)) { vm.cambiarInicio(it) }
            HoraSelector("Hasta", vm.fin, Modifier.weight(1f)) { vm.cambiarFin(it) }
        }

        Button(
            onClick = { vm.reservar(r.id_recurso, fecha) },
            enabled = !vm.enviando,
            modifier = Modifier.fillMaxWidth().height(54.dp).padding(top = 10.dp),
            shape = RoundedCornerShape(16.dp)
        ) { Text(if (vm.enviando) "Reservando…" else "Reservar", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

        vm.mensaje?.let { Aviso(it, if (vm.exito) Verde else Rojo, Modifier.padding(top = 12.dp)) }
        Spacer(Modifier.height(20.dp))
    }
}
