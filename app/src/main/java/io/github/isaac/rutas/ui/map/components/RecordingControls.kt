package io.github.isaac.rutas.ui.map.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel

@Composable
fun RecordingControls(onStartRequest: () -> Unit) {
    val viewModel = LocalMapViewModel.current
    val recordingState by viewModel.recordingState.collectAsState()

    // Animación de entrada/salida de los controles
    AnimatedContent(
        targetState = recordingState.isRecording,
        transitionSpec = {
            (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
        },
        label = "RecordingControlsTransition"
    ) { isRecording ->
        if (!isRecording) {
            // Estado Inicial: Botón Prominente
            Button(
                onClick = onStartRequest,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                shape = CircleShape,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Iniciar ruta", style = MaterialTheme.typography.labelLarge)
            }
        } else {
            // Estado Activo: El "Player"
            RecordingPlayerBar(
                isPaused = recordingState.isPaused,
                onPauseResume = {
                    if (recordingState.isPaused) viewModel.resumeRecording()
                    else viewModel.pauseRecording()
                },
                onStop = viewModel::stopRecording
            )
        }
    }
}

@Composable
private fun RecordingPlayerBar(
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        tonalElevation = 8.dp,
        shadowElevation = 10.dp,
        modifier = Modifier.height(64.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            // Indicador de "Grabando" (Círculo rojo que parpadea)
            RecordingDot(isPaused = isPaused)

            Spacer(Modifier.width(12.dp))

            // Boton de waypoint
            WaypointIconButton()

            // Botón Pausa / Play
            FilledIconButton(
                onClick = onPauseResume,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Reanudar" else "Pausar"
                )
            }

            Spacer(Modifier.width(8.dp))

            // Botón Detener
            FilledIconButton(
                onClick = onStop,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Detener")
            }
        }
    }
}

@Composable
private fun RecordingDot(isPaused: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaPulse"
    )

    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .size(12.dp)
            .clip(CircleShape)
            .background(if (isPaused) Color.Gray else Color.Red.copy(alpha = alpha))
    )
}