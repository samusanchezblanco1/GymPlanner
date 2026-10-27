package com.example.gymplanner.data

fun musculoPrincipal(musculos: List<MusculoWger>): GrupoMuscular? {
    val primero = musculos.firstOrNull() ?: return null
    val texto = "${primero.name.orEmpty()} ${primero.nameEn.orEmpty()}".lowercase()

    return when {
        listOf("femoris", "hamstring", "quadriceps", "quads", "gastrocnemius", "soleus", "calves", "calf")
            .any { it in texto } -> GrupoMuscular.PIERNAS
        listOf("gluteus", "glutes").any { it in texto } -> GrupoMuscular.GLUTEOS
        listOf("abdominis", "obliquus", "serratus", "abs").any { it in texto } -> GrupoMuscular.ABDOMEN
        listOf("latissimus", "lats", "trapezius", "traps").any { it in texto } -> GrupoMuscular.ESPALDA
        listOf("deltoid", "shoulder").any { it in texto } -> GrupoMuscular.HOMBROS
        "triceps" in texto -> GrupoMuscular.TRICEPS
        listOf("biceps", "brachialis").any { it in texto } -> GrupoMuscular.BICEPS
        listOf("pectoralis", "chest").any { it in texto } -> GrupoMuscular.PECHO
        else -> null
    }
}