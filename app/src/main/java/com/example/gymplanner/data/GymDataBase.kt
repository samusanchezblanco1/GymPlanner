package com.example.gymplanner.data

import android.content.Context
import androidx.room.*


//CREA Y ABRE LA BASE DE DATOS (DAO)

@Database(
    entities = [Entrenamiento::class, Ejercicio::class, Sesion::class],
    version = 1,
    exportSchema = false
)


abstract class GymDatabase : RoomDatabase() {

    abstract fun dao(): GymDao

    companion object {
        @Volatile private var instancia: GymDatabase? = null

        fun obtener(context: Context): GymDatabase =
            instancia ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym.db"
                ).build().also { instancia = it }
            }
    }
}