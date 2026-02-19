package io.github.isaac.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.isaac.myapplication.data.local.entities.Waypoint

@Composable
fun WaypointListDialog(
    waypoints: List<Waypoint>,
    onDismiss: () -> Unit,
    onSelect: (Waypoint) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Waypoints") },
        text = {
            if (waypoints.isEmpty()) {
                Text(
                    "No hay waypoints en esta ruta.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(waypoints) { waypoint ->
                        ListItem(
                            headlineContent = { Text(waypoint.descripcion) },
                            supportingContent = {
                                Text(if (waypoint.fotoPath == null) "Sin foto" else "Con foto")
                            },
                            trailingContent = {
                                Button(onClick = { onSelect(waypoint) }) {
                                    Text("Ver")
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
