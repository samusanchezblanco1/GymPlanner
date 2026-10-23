package com.example.gymplanner.ui.theme

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymplanner.data.Entrenamiento
import com.example.gymplanner.data.Sesion
import kotlin.math.roundToInt

val DIAS = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

fun formatoHora(minutos: Int): String = "%02d:%02d".format(minutos / 60, minutos % 60)

class EstadoArrastre {
    var entrenamiento by mutableStateOf<Entrenamiento?>(null)
    var posicion by mutableStateOf(Offset.Zero)
}

@Composable
fun PantallaSemana(vm: GymViewModel = viewModel()) {
    val entrenamientos by vm.entrenamientos.collectAsState()
    val sesiones by vm.sesiones.collectAsState()

    val arrastre = remember { EstadoArrastre() }
    val limitesDias = remember { mutableStateMapOf<Int, Rect>() }
    var origenRaiz by remember { mutableStateOf(Offset.Zero) }
    var pendiente by remember { mutableStateOf<Pair<Entrenamiento, Int>?>(null) }

    val alSoltar: () -> Unit = {
        val e = arrastre.entrenamiento
        val dia = limitesDias.entries.firstOrNull { it.value.contains(arrastre.posicion) }?.key
        arrastre.entrenamiento = null
        if (e != null && dia != null) {
            pendiente = e to dia
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { origenRaiz = it.positionInWindow() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DIAS.forEachIndexed { indice, nombreDia ->
                    val dia = indice + 1
                    val delDia = sesiones
                        .filter { it.diaSemana == dia }
                        .sortedBy { it.minutoDelDia }
                        .map { s ->
                            s to (entrenamientos
                                .firstOrNull { it.entrenamiento.id == s.entrenamientoId }
                                ?.entrenamiento?.nombre ?: "Entrenamiento")
                        }
                    TarjetaDia(
                        nombre = nombreDia,
                        items = delDia,
                        resaltado = arrastre.entrenamiento != null &&
                                limitesDias[dia]?.contains(arrastre.posicion) == true,
                        onPosicionada = { limitesDias[dia] = it },
                        onQuitar = { vm.borrarSesion(it) }
                    )
                }
            }

            Bandeja(
                entrenamientos = entrenamientos.map { it.entrenamiento },
                arrastre = arrastre,
                onSoltar = alSoltar
            )
        }

        arrastre.entrenamiento?.let { e ->
            Surface(
                modifier = Modifier.offset {
                    IntOffset(
                        (arrastre.posicion.x - origenRaiz.x).roundToInt(),
                        (arrastre.posicion.y - origenRaiz.y).roundToInt()
                    )
                },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp
            ) {
                Text(
                    e.nombre,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }

    pendiente?.let { (e, dia) ->
        DialogHora(
            titulo = "${e.nombre} · ${DIAS[dia - 1]}",
            onCancelar = { pendiente = null },
            onAceptar = { minutos ->
                vm.crearSesion(e.id, dia, minutos)
                pendiente = null
            }
        )
    }
}

@Composable
fun TarjetaDia(
    nombre: String,
    items: List<Pair<Sesion, String>>,
    resaltado: Boolean,
    onPosicionada: (Rect) -> Unit,
    onQuitar: (Sesion) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { onPosicionada(it.boundsInWindow()) },
        colors = CardDefaults.cardColors(
            containerColor = if (resaltado) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(nombre, style = MaterialTheme.typography.titleSmall)
            if (items.isEmpty()) {
                Text("Suelta aquí un entrenamiento", style = MaterialTheme.typography.bodySmall)
            }
            items.forEach { (sesion, nombreEntrenamiento) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${formatoHora(sesion.minutoDelDia)}  $nombreEntrenamiento",
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onQuitar(sesion) }) { Text("Quitar") }
                }
            }
        }
    }
}

@Composable
fun Bandeja(
    entrenamientos: List<Entrenamiento>,
    arrastre: EstadoArrastre,
    onSoltar: () -> Unit
) {
    Surface(tonalElevation = 3.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            if (entrenamientos.isEmpty()) {
                Text(
                    "Crea primero algún entrenamiento en la pestaña Entrenamientos",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(
                    "Mantén pulsado y arrastra a un día",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entrenamientos, key = { it.id }) { e ->
                        BloqueEntrenamiento(e, arrastre, onSoltar)
                    }
                }
            }
        }
    }
}

@Composable
fun BloqueEntrenamiento(e: Entrenamiento, arrastre: EstadoArrastre, onSoltar: () -> Unit) {
    var origen by remember { mutableStateOf(Offset.Zero) }
    val soltar by rememberUpdatedState(onSoltar)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .onGloballyPositioned { origen = it.positionInWindow() }
            .pointerInput(e.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        arrastre.entrenamiento = e
                        arrastre.posicion = origen + offset
                    },
                    onDrag = { change, cantidad ->
                        change.consume()
                        arrastre.posicion += cantidad
                    },
                    onDragEnd = { soltar() },
                    onDragCancel = { arrastre.entrenamiento = null }
                )
            }
    ) {
        Text(e.nombre, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogHora(titulo: String, onCancelar: () -> Unit, onAceptar: (Int) -> Unit) {
    val estado = rememberTimePickerState(initialHour = 18, initialMinute = 0, is24Hour = true)
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { TimeInput(state = estado) },
        confirmButton = {
            TextButton(onClick = { onAceptar(estado.hour * 60 + estado.minute) }) { Text("Añadir") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}