package com.acordes.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val EsquemaClaro = lightColorScheme(
    primary = Violeta, onPrimary = Color.White,
    primaryContainer = Color(0xFFE6E0FF), onPrimaryContainer = VioletaOscuro,
    secondary = Rosa, background = Fondo, surface = Color.White, surfaceVariant = Color(0xFFEDE9FB)
)

@Composable
fun AcordesTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = EsquemaClaro, content = content)
