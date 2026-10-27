package com.example.gymplanner.data

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val IDIOMA_BUSQUEDA = "en"

data class RespuestaEjercicios(
    val results: List<EjercicioWger> = emptyList()
)

data class EjercicioWger(
    val id: Long = 0,
    val muscles: List<MusculoWger> = emptyList(),
    val translations: List<TraduccionWger> = emptyList()
)

data class TraduccionWger(
    val name: String? = null
)

data class MusculoWger(
    val id: Int = 0,
    val name: String? = null,
    @SerializedName("name_en") val nameEn: String? = null
)

// Lo que mostramos en el formulario
data class Sugerencia(
    val nombre: String,
    val musculo: GrupoMuscular?
)

interface WgerApi {

    @GET("exerciseinfo/")
    suspend fun buscar(
        @Query("name__search") termino: String,
        @Query("language__code") idioma: String,
        @Query("limit") limite: Int
    ): RespuestaEjercicios

    companion object {
        val instancia: WgerApi by lazy {
            Retrofit.Builder()
                .baseUrl("https://wger.de/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WgerApi::class.java)
        }
    }
}