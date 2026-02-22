package io.github.isaac.myapplication.ui.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.isaac.myapplication.ui.map.locals.LocalMapViewModel
import java.util.Locale

/**
 * Tarjeta con el resumen (nombre, distancia, duración, velocidad) de la ruta seleccionada.
 * Solo se muestra cuando hay una ruta seleccionada activa.
 */
@Composable
fun RouteSummaryCard(modifier: Modifier = Modifier) {
    val viewModel = LocalMapViewModel.current
    val selectedRoute by viewModel.selectedRoute.collectAsState()

    val route = selectedRoute.route ?: return

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    route.nombre.ifBlank { "Ruta" },
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(formatDistance(route.distancia))
                    Text(formatDuration(route.duracion))
                    Text("${"%.2f".format(route.velocidadMedia)} km/h")
                }
            }
            IconButton(onClick = viewModel::clearSelectedRoute) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar ruta")
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0)
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    else
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

private fun formatDistance(distanceMeters: Double): String =
    if (distanceMeters >= 1000)
        "${"%.2f".format(distanceMeters / 1000.0)} km"
    else
        "${"%.0f".format(distanceMeters)} m"