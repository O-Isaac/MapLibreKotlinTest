package io.github.isaac.rutas.ui.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel

/**
 * FABs de control de grabación:
 *  - Sin grabación → botón "Iniciar ruta"
 *  - Grabando      → botones Pausa/Reanudar + Detener
 *
 * @param onStartRequest callback hacia la pantalla para gestionar el permiso antes de grabar
 */
@Composable
fun RecordingControls(onStartRequest: () -> Unit) {
    val viewModel = LocalMapViewModel.current
    val recordingState by viewModel.recordingState.collectAsState()

    if (!recordingState.isRecording) {
        ExtendedFloatingActionButton(
            onClick = onStartRequest,
            icon = { Icon(Icons.Default.PlayArrow, null) },
            text = { Text("Iniciar ruta") }
        )
        return
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ExtendedFloatingActionButton(
            onClick = if (recordingState.isPaused) viewModel::resumeRecording
            else viewModel::pauseRecording,
            icon = {
                Icon(
                    if (recordingState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    null
                )
            },
            text = { Text(if (recordingState.isPaused) "Reanudar" else "Pausa") }
        )
        FloatingActionButton(
            onClick = viewModel::stopRecording,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Icon(Icons.Default.Stop, contentDescription = "Detener")
        }
    }
}