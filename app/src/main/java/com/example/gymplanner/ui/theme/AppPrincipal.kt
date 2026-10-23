package com.example.gymplanner.ui.theme


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun AppPrincipal() {
    var pestana by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pestana == 0,
                    onClick = { pestana = 0 },
                    icon = { Text("🏋️") },
                    label = { Text("Entrenamientos") }
                )
                NavigationBarItem(
                    selected = pestana == 1,
                    onClick = { pestana = 1 },
                    icon = { Text("📅") },
                    label = { Text("Semana") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (pestana == 0) PantallaEntrenamientos() else PantallaSemana()
        }
    }
}