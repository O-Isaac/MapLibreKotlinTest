package io.github.isaac.rutas.ui.configuracion.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.isaac.rutas.ui.map.viewmodels.MapViewModel

/**
 * Slider para configurar el intervalo máximo de grabación de ruta.
 * Lee y escribe directamente en el ViewModel.
 */
@Composable
fun RecordingIntervalSlider(viewModel: MapViewModel) {
    val recordingInterval by viewModel.recordingIntervalSec.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Intervalo máximo",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            StatusChip("$recordingInterval s")
        }
        Text(
            "Mínimo 0.5 s, máximo $recordingInterval s",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )
        Slider(
            value = recordingInterval.toFloat(),
            onValueChange = { viewModel.updateRecordingInterval(it.toInt()) },
            valueRange = 1f..30f,
            steps = 28,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}