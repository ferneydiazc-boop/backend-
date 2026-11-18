package com.acordes.app.ui.screens.reservas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acordes.app.data.model.Recurso
import com.acordes.app.data.model.Reserva
import com.acordes.app.data.remote.amigable
import com.acordes.app.data.repository.AcordesRepository
import kotlinx.coroutines.launch

class MisReservasViewModel : ViewModel() {
    var reservas by mutableStateOf<List<Reserva>>(emptyList()); private set
    var recursos by mutableStateOf<Map<Int, Recurso>>(emptyMap()); private set
    var cargando by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set
    var porCancelar by mutableStateOf<Reserva?>(null); private set

    fun cargar() {
        viewModelScope.launch {
            cargando = true; error = null
            try {
                recursos = AcordesRepository.recursos(null, null).associateBy { it.id_recurso }
                reservas = AcordesRepository.misReservas()
                    .sortedWith(compareBy({ it.estado != "activa" }, { it.fecha }, { it.hora_inicio }))
            } catch (e: Exception) { error = e.amigable() }
            cargando = false
        }
    }

    fun pedirCancelar(r: Reserva) { porCancelar = r }
    fun descartar() { porCancelar = null }

    fun confirmarCancelar() {
        val r = porCancelar ?: return
        porCancelar = null
        viewModelScope.launch {
            try { AcordesRepository.cancelar(r.id_reserva) } catch (e: Exception) { error = e.amigable() }
            cargar()
        }
    }
}
