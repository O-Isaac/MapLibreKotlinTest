package io.github.isaac.rutas.ui.map.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel

/**
 * Diálogo con la lista de waypoints de la ruta seleccionada.
 * Lee el estado directamente del ViewModel.
 * Solo recibe onDismiss porque el cierre es responsabilidad del estado local de la pantalla.
 */
@Composable
fun WaypointListDialog(onDismiss: () -> Unit) {
    val viewModel = LocalMapViewModel.current
    val selectedRoute by viewModel.selectedRoute.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Waypoints") },
        text = {
            if (selectedRoute.waypoints.isEmpty()) {
                Text(
                    "No hay waypoints en esta ruta.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedRoute.waypoints) { waypoint ->
                        ListItem(
                            headlineContent = { Text(waypoint.descripcion) },
                            supportingContent = {
                                Text(if (waypoint.fotoPath == null) "Sin foto" else "Con foto")
                            },
                            trailingContent = {
                                Button(onClick = {
                                    onDismiss()
                                    viewModel.openWaypoint(waypoint)
                                }) { Text("Ver") }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}