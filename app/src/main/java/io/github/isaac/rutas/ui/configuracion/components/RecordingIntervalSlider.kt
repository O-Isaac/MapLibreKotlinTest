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
 * Slider para configurar el intervalo de grabación de ruta.
 *
 * Convenio con la DB (sin migración de schema):
 *   dbValue == 0  →  500 ms
 *   dbValue 1..30 →  N segundos exactos
 *
 * El slider expone 31 posiciones (0..30):
 *   posición 0  → "0.5 s"  → guarda 0 en DB
 *   posición 1  → "1 s"    → guarda 1 en DB
 *   posición 30 → "30 s"   → guarda 30 en DB
 */
@Composable
fun RecordingIntervalSlider(viewModel: MapViewModel) {
    val dbValue by viewModel.recordingIntervalSec.collectAsState()

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
                "Intervalo de grabación",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            StatusChip(dbValueToLabel(dbValue))
        }
        Text(
            "Cada cuánto se guarda un punto de la ruta",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )
        Slider(
            value = dbValue.toFloat(),
            onValueChange = { viewModel.updateRecordingInterval(it.toInt()) },
            valueRange = 0f..30f,
            steps = 29,  // 31 posiciones (0..30) → 29 pasos internos
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "0.5 s",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "30 s",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun dbValueToLabel(dbValue: Int): String =
    if (dbValue == 0) "0.5 s" else "$dbValue s"