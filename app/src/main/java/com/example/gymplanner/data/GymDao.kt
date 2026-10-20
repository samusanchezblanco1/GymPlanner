package com.example.gymplanner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

//Acceso a Datos - Operaciones Datos

@Dao
interface GymDao {
    //Corrutinas
    @Insert suspend fun insertarEntrenamiento(e: Entrenamiento): Long //insertar datos NUEVOS Ent y Ejercicios
    @Insert suspend fun insertarEjercicio(e: Ejercicio)
    @Insert suspend fun insertarSesion(s: Sesion): Long
    @Update suspend fun actualizarSesion(s: Sesion)   //Actualizar la fecha del calendario
    @Delete suspend fun borrarSesion(s: Sesion)
    @Delete suspend fun borrarEntrenamiento(e: Entrenamiento) //Borra sesión (ON CASCADE para ejercicios y sesiones)

    @Transaction  //Agrupa las lecturas para
    @Query("SELECT * FROM entrenamientos")
    fun entrenamientos(): Flow<List<EntrenamientoConEjercicios>>

    @Query("SELECT * FROM sesiones")
    fun sesiones(): Flow<List<Sesion>>
}