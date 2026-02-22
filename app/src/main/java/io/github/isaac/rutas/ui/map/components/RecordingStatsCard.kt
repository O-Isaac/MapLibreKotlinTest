package io.github.isaac.rutas.ui.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel
import java.util.Locale

/**
 * Tarjeta con distancia, tiempo y velocidad media durante una grabación activa.
 * Solo se muestra si hay grabación en curso.
 */
@Composable
fun RecordingStatsCard(modifier: Modifier = Modifier) {
    val viewModel = LocalMapViewModel.current
    val recordingState by viewModel.recordingState.collectAsState()

    if (!recordingState.isRecording) return

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatItem("Distancia", formatDistance(recordingState.distanceMeters))
            StatItem("Tiempo", formatDuration(recordingState.elapsedMs))
            StatItem("Vel. media", "${"%.2f".format(recordingState.avgSpeedKmh)} km/h")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
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