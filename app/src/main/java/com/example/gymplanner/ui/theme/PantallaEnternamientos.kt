package com.example.gymplanner.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymplanner.data.*

@Composable
fun PantallaEntrenamientos(vm: GymViewModel = viewModel()) {
    val lista by vm.entrenamientos.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) { Text("+") }
        }
    ) { padding ->
        if (lista.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no tienes entrenamientos.\nPulsa + para crear el primero.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lista, key = { it.entrenamiento.id }) { item ->
                    TarjetaEntrenamiento(
                        item = item,
                        onBorrar = { vm.borrarEntrenamiento(item.entrenamiento) }
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogNuevoEntrenamiento(
            onCancelar = { mostrarDialogo = false },
            onGuardar = { entrenamiento, ejercicios ->
                vm.crearEntrenamiento(entrenamiento, ejercicios)
                mostrarDialogo = false
            }
        )
    }
}

@Composable
fun TarjetaEntrenamiento(item: EntrenamientoConEjercicios, onBorrar: () -> Unit) {
    val e = item.entrenamiento
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(e.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(
                        e.tipo.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(onClick = onBorrar) { Text("Borrar") }
            }
            e.duracionMin?.let { Text("Duración: $it min") }
            e.distanciaKm?.let { Text("Distancia: $it km") }
            if (item.ejercicios.isNotEmpty()) {
                val musculos = item.ejercicios.map { it.musculo.name }.distinct()
                Text("Músculos: " + musculos.joinToString(", "))
                item.ejercicios.forEach {
                    Text("• ${it.nombre}: ${it.series}x${it.repeticiones}")
                }
            }
        }
    }
}

@Composable
fun DialogNuevoEntrenamiento(
    onCancelar: () -> Unit,
    onGuardar: (Entrenamiento, List<Ejercicio>) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoEntrenamiento.FUERZA) }
    var duracion by remember { mutableStateOf("") }
    var distancia by remember { mutableStateOf("") }
    val ejercicios = remember { mutableStateListOf<Ejercicio>() }

    var exNombre by remember { mutableStateOf("") }
    var exSeries by remember { mutableStateOf("") }
    var exReps by remember { mutableStateOf("") }
    var exMusculo by remember { mutableStateOf(GrupoMuscular.PECHO) }
    var menuAbierto by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nuevo entrenamiento") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )

                TipoEntrenamiento.entries.forEach { opcion ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = tipo == opcion, onClick = { tipo = opcion })
                        Text(opcion.name.replace("_", " "))
                    }
                }

                if (tipo == TipoEntrenamiento.FUERZA) {
                    ejercicios.forEach {
                        Text("• ${it.nombre}: ${it.series}x${it.repeticiones} (${it.musculo.name})")
                    }
                    OutlinedTextField(
                        value = exNombre,
                        onValueChange = { exNombre = it },
                        label = { Text("Ejercicio") },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = exSeries,
                            onValueChange = { exSeries = it },
                            label = { Text("Series") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = exReps,
                            onValueChange = { exReps = it },
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Box {
                        TextButton(onClick = { menuAbierto = true }) {
                            Text("Músculo: ${exMusculo.name}")
                        }
                        DropdownMenu(
                            expanded = menuAbierto,
                            onDismissRequest = { menuAbierto = false }
                        ) {
                            GrupoMuscular.entries.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.name) },
                                    onClick = {
                                        exMusculo = m
                                        menuAbierto = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedButton(onClick = {
                        val s = exSeries.toIntOrNull()
                        val r = exReps.toIntOrNull()
                        if (exNombre.isNotBlank() && s != null && r != null) {
                            ejercicios.add(
                                Ejercicio(
                                    entrenamientoId = 0,
                                    nombre = exNombre,
                                    series = s,
                                    repeticiones = r,
                                    musculo = exMusculo
                                )
                            )
                            exNombre = ""
                            exSeries = ""
                            exReps = ""
                        }
                    }) { Text("Añadir ejercicio") }
                } else {
                    OutlinedTextField(
                        value = duracion,
                        onValueChange = { duracion = it },
                        label = { Text("Duración (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    if (tipo == TipoEntrenamiento.RUNNING) {
                        OutlinedTextField(
                            value = distancia,
                            onValueChange = { distancia = it },
                            label = { Text("Distancia (km)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nombre.isNotBlank()) {
                    val entrenamiento = Entrenamiento(
                        nombre = nombre,
                        tipo = tipo,
                        duracionMin = if (tipo != TipoEntrenamiento.FUERZA) duracion.toIntOrNull() else null,
                        distanciaKm = if (tipo == TipoEntrenamiento.RUNNING)
                            distancia.replace(',', '.').toDoubleOrNull() else null
                    )
                    val lista = if (tipo == TipoEntrenamiento.FUERZA) ejercicios.toList() else emptyList()
                    onGuardar(entrenamiento, lista)
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}