package io.github.isaac.rutas.ui.rutas.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.isaac.rutas.data.local.entities.Ruta
import io.github.isaac.rutas.ui.rutas.utils.StringUtils.formatDistance
import io.github.isaac.rutas.ui.rutas.utils.StringUtils.formatDuration

@Composable
fun RouteStatsSection(ruta: Ruta, waypointCount: Int) {
    val speedKmh = if (ruta.duracion > 0)
        (ruta.distancia / 1000.0) / (ruta.duracion / 3_600_000.0)
    else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Straighten,
                label = "Distancia",
                value = formatDistance(ruta.distancia),
                accentColor = MaterialTheme.colorScheme.primary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Timer,
                label = "Duración",
                value = formatDuration(ruta.duracion),
                accentColor = MaterialTheme.colorScheme.tertiary
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Speed,
                label = "Vel. media",
                value = "${"%.1f".format(speedKmh)} km/h",
                accentColor = MaterialTheme.colorScheme.secondary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.PinDrop,
                label = "Waypoints",
                value = "$waypointCount",
                accentColor = Color(0xFFE86C2C)
            )
        }
    }
}