package com.acordes.app.ui.screens.recursos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acordes.app.data.model.Recurso
import com.acordes.app.ui.components.*
import com.acordes.app.ui.theme.*
import com.acordes.app.util.capitalizar
import com.acordes.app.util.emojiDe
import java.time.LocalDate

@Composable
fun RecursosScreen(
    fecha: LocalDate, onFecha: (LocalDate) -> Unit, onDetalle: (Recurso) -> Unit,
    vm: RecursosViewModel = viewModel()
) {
    // Recarga al abrir, al cambiar la fecha y al volver desde el detalle
    AlReanudar(fecha) { vm.cargar(fecha) }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))
        FechaBoton(fecha, onFecha)
        Row(Modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(vm.tipo == null, { vm.cambiarTipo(null, fecha) }, { Text("Todos") })
            FilterChip(vm.tipo == "sala", { vm.cambiarTipo("sala", fecha) }, { Text("🎤 Salas") })
            FilterChip(vm.tipo == "instrumento", { vm.cambiarTipo("instrumento", fecha) }, { Text("🎸 Instrumentos") })
        }
        val error = vm.error
        when {
            vm.cargando -> Cargando()
            error != null -> Column {
                Aviso(error, Rojo)
                Button(onClick = { vm.cargar(fecha) }, Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("Reintentar") }
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                items(vm.recursos) { r -> TarjetaRecurso(r) { onDetalle(r) } }
            }
        }
    }
}

@Composable
private fun TarjetaRecurso(r: Recurso, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(EsquemaClaro.primaryContainer), contentAlignment = Alignment.Center) {
                Text(emojiDe(r), fontSize = 28.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(r.nombre, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("${r.tipo.capitalizar()} · ${r.ubicacion ?: "—"}", color = Gris, fontSize = 13.sp)
                if (r.reservado_en_fecha == true) Text("Tiene reservas ese día", color = Ambar, fontSize = 12.sp)
            }
            EstadoRecurso(r.estado_disp)
        }
    }
}
