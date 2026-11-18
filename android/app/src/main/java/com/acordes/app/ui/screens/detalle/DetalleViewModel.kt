package com.acordes.app.ui.screens.detalle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acordes.app.data.model.Franja
import com.acordes.app.data.model.Recurso
import com.acordes.app.data.remote.amigable
import com.acordes.app.data.repository.AcordesRepository
import com.acordes.app.util.HORAS
import com.acordes.app.util.formatoFecha
import kotlinx.coroutines.launch
import java.time.LocalDate

class DetalleViewModel : ViewModel() {
    var recurso by mutableStateOf<Recurso?>(null); private set
    var errorCarga by mutableStateOf<String?>(null); private set
    var franjas by mutableStateOf<List<Franja>>(emptyList()); private set
    var inicio by mutableStateOf("09:00"); private set
    var fin by mutableStateOf("10:00"); private set
    var mensaje by mutableStateOf<String?>(null); private set
    var exito by mutableStateOf(false); private set
    var enviando by mutableStateOf(false); private set

    fun cargarRecurso(id: Int) {
        viewModelScope.launch {
            try { recurso = AcordesRepository.recurso(id); errorCarga = null }
            catch (e: Exception) { errorCarga = e.amigable() }
        }
    }

    fun cargarFranjas(id: Int, fecha: LocalDate) {
        viewModelScope.launch {
            franjas = try { AcordesRepository.franjasOcupadas(id, fecha) } catch (e: Exception) { emptyList() }
        }
    }

    fun cambiarInicio(h: String) {
        inicio = h
        if (fin <= h) fin = HORAS[(HORAS.indexOf(h) + 2).coerceAtMost(HORAS.lastIndex)]   // fin = inicio + 1 h
    }
    fun cambiarFin(h: String) { fin = h }
    fun limpiarMensaje() { mensaje = null }

    fun reservar(idRecurso: Int, fecha: LocalDate) {
        viewModelScope.launch {
            enviando = true
            try {
                AcordesRepository.reservar(idRecurso, fecha, inicio, fin)
                exito = true; mensaje = "✅ ¡Reserva creada! ${formatoFecha(fecha)}, $inicio – $fin"
                cargarFranjas(idRecurso, fecha)
            } catch (e: Exception) { exito = false; mensaje = "⛔ ${e.amigable()}" }
            enviando = false
        }
    }
}
