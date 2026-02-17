package io.github.isaac.myapplication.ui.components

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
fun DialogMarker(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    // Estado interno para el nombre que el usuario escribe
    var markerName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Nuevo Marcador")
        },
        text = {
            Column {
                Text(text = "Escribe el nombre para esta ubicación:")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = markerName,
                    onValueChange = { markerName = it },
                    label = { Text("Nombre del lugar") },
                    placeholder = { Text("Ej. Mi cafetería favorita") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (markerName.isNotBlank()) {
                        onConfirm(markerName)
                    }
                },
                enabled = markerName.isNotBlank() // Deshabilita si está vacío
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