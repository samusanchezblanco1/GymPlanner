package com.example.gymplanner.data

import androidx.room.*

enum class TipoEntrenamiento { FUERZA, CARDIO_SUAVE, RUNNING }

enum class GrupoMuscular {
    PECHO, ESPALDA, HOMBROS, BICEPS, TRICEPS, PIERNAS, GLUTEOS, ABDOMEN
}


@Entity(tableName = "entrenamientos")
data class Entrenamiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: TipoEntrenamiento,
    val duracionMin: Int? = null,      // para cardio y running
    val distanciaKm: Double? = null    // para running
)

@Entity(
    tableName = "ejercicios",
    foreignKeys = [ForeignKey(
        entity = Entrenamiento::class,
        parentColumns = ["id"],
        childColumns = ["entrenamientoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("entrenamientoId")]
)

data class Ejercicio(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entrenamientoId: Long,
    val nombre: String,
    val series: Int,
    val repeticiones: Int,
    val musculo: GrupoMuscular
)
@Entity(
    tableName = "sesiones",
    foreignKeys = [ForeignKey(
        entity = Entrenamiento::class,
        parentColumns = ["id"],
        childColumns = ["entrenamientoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("entrenamientoId")]
)
data class Sesion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entrenamientoId: Long,
    val diaSemana: Int,          // 1 = lunes ... 7 = domingo
    val minutoDelDia: Int,       // 0..1439, p. ej. 18:30 = 1110
    val minutosAviso: Int = 30   // recordatorio antes de la sesión
)


data class EntrenamientoConEjercicios(
    @Embedded val entrenamiento: Entrenamiento,
    @Relation(parentColumn = "id", entityColumn = "entrenamientoId")
    val ejercicios: List<Ejercicio>
)
