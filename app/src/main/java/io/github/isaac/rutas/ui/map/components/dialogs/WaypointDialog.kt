package io.github.isaac.rutas.ui.map.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WaypointDialog(
    description: String,
    photoPath: String?,
    onTakePhoto: () -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember(description) { mutableStateOf(description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo waypoint") },
        text = {
            Column {
                Text("Descripcion del punto:")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Descripcion") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onTakePhoto) {
                    Text(if (photoPath == null) "Agregar foto" else "Cambiar foto")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
