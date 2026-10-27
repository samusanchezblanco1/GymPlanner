package com.example.gymplanner.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymplanner.data.Ejercicio
import com.example.gymplanner.data.Entrenamiento
import com.example.gymplanner.data.EntrenamientoConEjercicios
import com.example.gymplanner.data.GymDatabase
import com.example.gymplanner.data.Sesion
import com.example.gymplanner.notificaciones.Recordatorios
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.gymplanner.data.GrupoMuscular
import com.example.gymplanner.data.Sugerencia
import com.example.gymplanner.data.WgerApi
import com.example.gymplanner.data.IDIOMA_BUSQUEDA
import com.example.gymplanner.data.musculoPrincipal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

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

    fun crearSesion(entrenamientoId: Long, dia: Int, minutoDelDia: Int, minutosAviso: Int = 30) {
        viewModelScope.launch {
            val id = dao.insertarSesion(
                Sesion(
                    entrenamientoId = entrenamientoId,
                    diaSemana = dia,
                    minutoDelDia = minutoDelDia,
                    minutosAviso = minutosAviso
                )
            )
            val nombre = entrenamientos.value
                .firstOrNull { it.entrenamiento.id == entrenamientoId }
                ?.entrenamiento?.nombre ?: "Entrenamiento"
            Recordatorios.programar(
                getApplication<Application>(),
                id,
                nombre,
                dia,
                minutoDelDia,
                minutosAviso
            )
        }
    }

    fun borrarSesion(sesion: Sesion) {
        viewModelScope.launch {
            dao.borrarSesion(sesion)
            Recordatorios.cancelar(getApplication<Application>(), sesion.id)
        }
    }

    fun borrarEntrenamiento(entrenamiento: Entrenamiento) {
        viewModelScope.launch {
            sesiones.value
                .filter { it.entrenamientoId == entrenamiento.id }
                .forEach { Recordatorios.cancelar(getApplication<Application>(), it.id) }
            dao.borrarEntrenamiento(entrenamiento)
        }
    }
    private val wger = WgerApi.instancia

    private val _sugerencias = MutableStateFlow<List<Sugerencia>>(emptyList())
    val sugerencias: StateFlow<List<Sugerencia>> = _sugerencias

    private val _buscando = MutableStateFlow(false)
    val buscando: StateFlow<Boolean> = _buscando

    private val _errorBusqueda = MutableStateFlow<String?>(null)
    val errorBusqueda: StateFlow<String?> = _errorBusqueda

    private var trabajoBusqueda: Job? = null

    fun buscarEjercicios(texto: String) {
        trabajoBusqueda?.cancel()
        if (texto.length < 3) {
            limpiarBusqueda()
            return
        }
        trabajoBusqueda = viewModelScope.launch {
            delay(400)
            _buscando.value = true
            _errorBusqueda.value = null
            try {
                val resultados = wger.buscar(texto, IDIOMA_BUSQUEDA, 8).results
                _sugerencias.value = resultados.mapNotNull { r ->
                    val nombre = r.translations
                        .firstOrNull { it.name?.contains(texto, ignoreCase = true) == true }
                        ?.name
                        ?: r.translations.firstOrNull()?.name
                    nombre?.let { Sugerencia(it, musculoPrincipal(r.muscles)) }
                }.distinctBy { it.nombre }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _sugerencias.value = emptyList()
                _errorBusqueda.value = "No se pudo buscar (¿sin conexión?)"
            } finally {
                _buscando.value = false
            }
        }
    }



    fun limpiarBusqueda() {
        trabajoBusqueda?.cancel()
        _sugerencias.value = emptyList()
        _errorBusqueda.value = null
        _buscando.value = false
    }
}