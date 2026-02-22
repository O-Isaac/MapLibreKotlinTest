package io.github.isaac.rutas.ui.map.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel
import java.util.Locale

@Composable
fun RecordingStatsCard(modifier: Modifier = Modifier) {
    val viewModel = LocalMapViewModel.current
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = recordingState.isRecording,
        enter = fadeIn(animationSpec = tween(300)) +
                slideInVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    initialOffsetY = { -it }
                ),
        exit = fadeOut(animationSpec = tween(200)) +
                slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { -it }
                ),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    label = "Distancia",
                    value = formatDistance(recordingState.distanceMeters),
                    icon = Icons.Rounded.Timeline,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    modifier = Modifier
                        .height(30.dp)
                        .padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                StatItem(
                    label = "Tiempo",
                    value = formatDuration(recordingState.elapsedMs),
                    icon = Icons.Rounded.Timer,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    modifier = Modifier
                        .height(30.dp)
                        .padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                StatItem(
                    label = "Velocidad",
                    value = "${"%.1f".format(recordingState.avgSpeedKmh)} km/h",
                    icon = Icons.Rounded.Speed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Reemplazamos AnimatedContent por un crossfade suave y rápido.
        // Crossfade es más estable para texto que cambia frecuentemente
        // porque no tiene el delay de entrada/salida simultáneo que causa el "salto".
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
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