package io.github.isaac.myapplication.ui.map.components.dialogs

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.isaac.myapplication.ui.map.locals.LocalMapViewModel
import java.io.File

/**
 * Diálogo de solo lectura con la descripción y foto de un waypoint.
 * Lee el estado directamente del ViewModel. Solo visible cuando hay un waypoint seleccionado.
 */
@Composable
fun WaypointInfoDialog() {
    val viewModel = LocalMapViewModel.current
    val selectedWaypoint by viewModel.selectedWaypoint.collectAsState()
    val details = selectedWaypoint ?: return

    val bitmap = remember(details.photoPath) {
        details.photoPath?.let {
            val file = File(it)
            if (file.exists()) BitmapFactory.decodeFile(it) else null
        }
    }

    AlertDialog(
        onDismissRequest = viewModel::dismissWaypointDetails,
        title = { Text("Waypoint") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(details.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        "Sin foto",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = viewModel::dismissWaypointDetails) { Text("Cerrar") }
        }
    )
}