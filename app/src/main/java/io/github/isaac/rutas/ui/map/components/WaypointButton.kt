package io.github.isaac.rutas.ui.map.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel

/**
 * Versión compacta para ser usada dentro del Player Bar.
 */
@Composable
fun WaypointIconButton(modifier: Modifier = Modifier) {
    val viewModel = LocalMapViewModel.current
    val recordingState by viewModel.recordingState.collectAsState()

    // Solo habilitado si no está pausado (opcional)
    val isEnabled = !recordingState.isPaused

    FilledTonalIconButton(
        onClick = viewModel::requestWaypoint, // Usamos la lógica que ya tenías
        modifier = modifier.size(44.dp),
        enabled = isEnabled,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            Icons.Default.AddLocationAlt,
            contentDescription = "Añadir waypoint",
            modifier = Modifier.size(20.dp)
        )
    }
}