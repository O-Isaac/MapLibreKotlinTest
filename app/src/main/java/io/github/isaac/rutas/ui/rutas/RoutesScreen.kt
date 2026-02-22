package io.github.isaac.rutas.ui.rutas

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.isaac.rutas.ui.map.viewmodels.MapViewModel
import io.github.isaac.rutas.ui.rutas.components.EmptyRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesScreen(
    viewModel: MapViewModel,
    onNavigateBack: () -> Unit,
    onSelect: (Long) -> Unit,
) {
    val routes by viewModel.routes.collectAsState()
    val actions = rememberRouteActions(viewModel)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rutas guardadas") },
                actions = {
                    IconButton(onClick = { actions.onImportClick() }) {
                        Icon(Icons.Default.FileOpen, contentDescription = "Importar")
                    }
                }
            )
        }
    ) { padding ->
        CompositionLocalProvider(LocalRouteActions provides actions) {
            if (routes.isEmpty()) {
                EmptyRoutes(modifier = Modifier.padding(padding))
            } else {
                RouteList(
                    modifier = Modifier.padding(padding),
                    routes = routes,
                    onNavigateBack = onNavigateBack,
                    onSelect = onSelect
                )
            }
        }

    }
}