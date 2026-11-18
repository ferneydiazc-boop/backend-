package com.acordes.app.util

import com.acordes.app.data.model.Recurso
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Franjas de 30 min entre 07:00 y 22:00 para elegir horario. */
val HORAS: List<String> =
    (7..21).flatMap { listOf("%02d:00".format(it), "%02d:30".format(it)) } + "22:00"

fun formatoFecha(f: LocalDate): String =
    f.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", Locale("es", "CO")))
        .replaceFirstChar { it.uppercase() }

fun String.capitalizar(): String = replaceFirstChar { it.uppercase() }

fun emojiDe(r: Recurso): String = when {
    r.tipo == "sala" -> "🎤"
    r.nombre.contains("guitarra", true) -> "🎸"
    r.nombre.contains("bater", true) -> "🥁"
    r.nombre.contains("teclado", true) || r.nombre.contains("piano", true) -> "🎹"
    else -> "🎵"
}
