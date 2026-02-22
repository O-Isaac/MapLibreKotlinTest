package io.github.isaac.myapplication.ui.marcadores

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import io.github.isaac.myapplication.ui.map.MapViewModel
import io.github.isaac.myapplication.ui.marcadores.components.MarkerEmpty
import io.github.isaac.myapplication.ui.marcadores.components.MarkerList



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkersScreen(viewModel: MapViewModel, onNavigateBack: () -> Unit) {
    val markers by viewModel.markers.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Tus sitios guardados",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (markers.isEmpty()) {
            MarkerEmpty()
        } else {
            MarkerList(
                modifier = Modifier.padding(padding),
                markers = markers,
                viewModel = viewModel,
                onNavigateBack = onNavigateBack
            )
        }
    }
}
