package io.github.isaac.rutas.ui.map.components.fabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel

/**
 * FAB para añadir un waypoint en la posición actual durante una grabación activa.
 * Solo se muestra mientras hay grabación en curso.
 */
@Composable
fun AddWaypointFab() {
    val viewModel = LocalMapViewModel.current
    val recordingState by viewModel.recordingState.collectAsState()

    if (!recordingState.isRecording) return

    ExtendedFloatingActionButton(
        onClick = viewModel::requestWaypoint,
        icon = { Icon(Icons.Default.AddLocationAlt, null) },
        text = { Text("Añadir waypoint") },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
}