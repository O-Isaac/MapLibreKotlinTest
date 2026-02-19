package io.github.isaac.myapplication.ui.rutas.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.isaac.myapplication.data.local.entities.Ruta
import io.github.isaac.myapplication.utils.StringUtils.formatDistance
import io.github.isaac.myapplication.utils.StringUtils.formatDuration

@Composable
fun RouteCard(
    route: Ruta,
    onSelect: () -> Unit,
    onShare: () -> Unit,
    onDelete: (Ruta) -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        route.nombre.ifBlank { "Ruta" },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${formatDistance(route.distancia)} • ${formatDuration(route.duracion)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir GPX")
                    }

                    IconButton(onClick = { onDelete(route) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error // Rojo para advertir peligro
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.size(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip(
                    icon = Icons.Default.Schedule,
                    text = formatDuration(route.duracion)
                )
                MetricChip(
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    text = formatDistance(route.distancia)
                )
                MetricChip(
                    icon = Icons.Default.Speed,
                    text = "${"%.2f".format(route.velocidadMedia)} km/h"
                )
            }


        }
    }
}