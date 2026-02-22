package io.github.isaac.rutas.ui.map.components.fabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel

/**
 * FAB que abre el diálogo de lista de waypoints de la ruta actualmente seleccionada.
 * Solo se muestra cuando hay una ruta seleccionada.
 */
@Composable
fun ShowWaypointsFab(onShowWaypointsDialog: () -> Unit) {
    val viewModel = LocalMapViewModel.current
    val selectedRoute by viewModel.selectedRoute.collectAsState()

    if (selectedRoute.route == null) return

    ExtendedFloatingActionButton(
        onClick = onShowWaypointsDialog,
        icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
        text = { Text("Waypoints") }
    )
}