package com.acordes.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acordes.app.data.session.SessionManager
import com.acordes.app.ui.theme.*

/** Etiqueta redondeada de color (estado, horario ocupado, etc.). */
@Composable
fun Pill(texto: String, color: Color) {
    Box(Modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.14f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(texto, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Recuadro de mensaje (éxito, error, advertencia). */
@Composable
fun Aviso(texto: String, color: Color, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(color.copy(alpha = 0.12f)).padding(14.dp)) {
        Text(texto, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun Titulo(texto: String) = Text(
    texto, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VioletaOscuro,
    modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
)

@Composable
fun Cargando() = Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) { CircularProgressIndicator() }

/** Pill de disponibilidad de un recurso. */
@Composable
fun EstadoRecurso(estado: String) =
    if (estado == "disponible") Pill("Disponible", Verde)
    else Pill(estado.replaceFirstChar { it.uppercase() }, Rojo)

@Composable
fun Cabecera(onSalir: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(VioletaOscuro, Violeta)))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("🎵 Entregable Final", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Hola, ${SessionManager.userName}", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
        }
        TextButton(onClick = onSalir) { Text("Salir", color = Color.White) }
    }
}
