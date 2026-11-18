package com.acordes.app.ui.screens.reservas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acordes.app.data.model.Reserva
import com.acordes.app.ui.components.*
import com.acordes.app.ui.theme.*
import com.acordes.app.util.emojiDe
import com.acordes.app.util.formatoFecha
import java.time.LocalDate

@Composable
fun MisReservasScreen(vm: MisReservasViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.cargar() }

    vm.porCancelar?.let { b ->
        AlertDialog(
            onDismissRequest = { vm.descartar() },
            title = { Text("¿Cancelar reserva?") },
            text = { Text("${vm.recursos[b.id_recurso]?.nombre ?: "Recurso"} · ${b.fecha} · ${b.hora_inicio} – ${b.hora_fin}") },
            confirmButton = { TextButton(onClick = { vm.confirmarCancelar() }) { Text("Sí, cancelar", color = Rojo) } },
            dismissButton = { TextButton(onClick = { vm.descartar() }) { Text("No") } }
        )
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text("Mis reservas", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VioletaOscuro, modifier = Modifier.padding(vertical = 12.dp))
        val error = vm.error
        when {
            vm.cargando -> Cargando()
            error != null -> Column {
                Aviso(error, Rojo)
                Button(onClick = { vm.cargar() }, Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("Reintentar") }
            }
            vm.reservas.isEmpty() -> Aviso("Aún no tienes reservas. Ve a Recursos y reserva tu primera sala 🎵", Violeta)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                items(vm.reservas) { b -> TarjetaReserva(b, vm) }
            }
        }
    }
}

@Composable
private fun TarjetaReserva(b: Reserva, vm: MisReservasViewModel) {
    val rec = vm.recursos[b.id_recurso]
    val activa = b.estado == "activa"
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (activa) Color.White else Color(0xFFEFEDF5))
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (rec != null) emojiDe(rec) else "🎵", fontSize = 30.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(rec?.nombre ?: "Recurso #${b.id_recurso}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(formatoFecha(LocalDate.parse(b.fecha)), color = Gris, fontSize = 13.sp)
                Text("🕘 ${b.hora_inicio} – ${b.hora_fin}", fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                if (activa) Pill("Activa", Verde) else Pill("Cancelada", Gris)
            }
            if (activa) TextButton(onClick = { vm.pedirCancelar(b) }) { Text("Cancelar", color = Rojo) }
        }
    }
}
