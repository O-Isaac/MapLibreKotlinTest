package io.github.isaac.rutas.ui.map.components.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import io.github.isaac.rutas.ui.map.openLocationSettings

/**
 * Diálogo informativo que pide al usuario activar la ubicación de alta precisión.
 * Ofrece ir directamente a los ajustes del sistema o cancelar.
 */
@Composable
fun AccuracyDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Activa la ubicación precisa") },
        text = {
            Text(
                text = "Para una mejor experiencia, tu dispositivo necesita usar la ubicación precisa.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    openLocationSettings(context)
                    onDismiss()
                }
            ) { Text("Activar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}