package com.acordes.app.ui.screens.recursos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acordes.app.data.model.Recurso
import com.acordes.app.data.remote.amigable
import com.acordes.app.data.repository.AcordesRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class RecursosViewModel : ViewModel() {
    var recursos by mutableStateOf<List<Recurso>>(emptyList()); private set
    var cargando by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set
    var tipo by mutableStateOf<String?>(null); private set

    fun cargar(fecha: LocalDate) {
        viewModelScope.launch {
            cargando = true; error = null
            try { recursos = AcordesRepository.recursos(tipo, fecha) }
            catch (e: Exception) { error = e.amigable() }
            cargando = false
        }
    }

    fun cambiarTipo(nuevo: String?, fecha: LocalDate) { tipo = nuevo; cargar(fecha) }
}
