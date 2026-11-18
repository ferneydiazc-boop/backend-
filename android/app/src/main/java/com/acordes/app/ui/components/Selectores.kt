package com.acordes.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.acordes.app.util.HORAS
import com.acordes.app.util.formatoFecha
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FechaBoton(fecha: LocalDate, onChange: (LocalDate) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    OutlinedButton(
        onClick = { abierto = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
    ) { Text("📅  ${formatoFecha(fecha)}", fontWeight = FontWeight.Medium) }

    if (abierto) {
        val st = rememberDatePickerState(initialSelectedDateMillis = fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                TextButton(onClick = {
                    st.selectedDateMillis?.let { onChange(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                    abierto = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { abierto = false }) { Text("Cancelar") } }
        ) { DatePicker(state = st) }
    }
}

@Composable
fun HoraSelector(etiqueta: String, valor: String, modifier: Modifier, onSelect: (String) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(
            onClick = { abierto = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) { Text("$etiqueta  $valor", fontWeight = FontWeight.Medium) }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            HORAS.forEach { h -> DropdownMenuItem(text = { Text(h) }, onClick = { onSelect(h); abierto = false }) }
        }
    }
}

/** Ejecuta [accion] cada vez que la pantalla vuelve a mostrarse (y cuando cambia [clave]). */
@Composable
fun AlReanudar(clave: Any?, accion: () -> Unit) {
    val owner = LocalLifecycleOwner.current
    val actual by rememberUpdatedState(accion)
    DisposableEffect(owner, clave) {
        val obs = LifecycleEventObserver { _, ev -> if (ev == Lifecycle.Event.ON_RESUME) actual() }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
}
