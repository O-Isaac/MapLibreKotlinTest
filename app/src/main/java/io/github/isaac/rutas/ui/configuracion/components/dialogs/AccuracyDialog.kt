package io.github.isaac.rutas.ui.configuracion.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.isaac.rutas.ui.configuracion.utils.openLocationSettings

/**
 * Diálogo que invita al usuario a activar la ubicación precisa (GPS) desde Ajustes.
 */
@Composable
fun AccuracyDialog(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Activa la ubicación precisa") },
        text = { Text("Para el funcionamiento correcto, activa la ubicación precisa (GPS) en ajustes.") },
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