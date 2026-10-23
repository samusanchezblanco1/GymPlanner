package com.example.gymplanner.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymplanner.data.Ejercicio
import com.example.gymplanner.data.Entrenamiento
import com.example.gymplanner.data.EntrenamientoConEjercicios
import com.example.gymplanner.data.GymDatabase
import com.example.gymplanner.data.Sesion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GymViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = GymDatabase.obtener(app).dao()

    val entrenamientos: StateFlow<List<EntrenamientoConEjercicios>> =
        dao.entrenamientos()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sesiones: StateFlow<List<Sesion>> =
        dao.sesiones()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun crearEntrenamiento(entrenamiento: Entrenamiento, ejercicios: List<Ejercicio>) {
        viewModelScope.launch {
            val id = dao.insertarEntrenamiento(entrenamiento)
            ejercicios.forEach { dao.insertarEjercicio(it.copy(entrenamientoId = id)) }
        }
    }

    fun borrarEntrenamiento(entrenamiento: Entrenamiento) {
        viewModelScope.launch { dao.borrarEntrenamiento(entrenamiento) }
    }
    fun crearSesion(entrenamientoId: Long, dia: Int, minutoDelDia: Int) {
        viewModelScope.launch {
            dao.insertarSesion(
                Sesion(entrenamientoId = entrenamientoId, diaSemana = dia, minutoDelDia = minutoDelDia)
            )
        }
    }

    fun borrarSesion(sesion: Sesion) {
        viewModelScope.launch { dao.borrarSesion(sesion) }
    }
}