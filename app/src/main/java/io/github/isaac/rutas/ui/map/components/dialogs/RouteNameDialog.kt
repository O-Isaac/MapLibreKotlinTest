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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel

/**
 * Diálogo para nombrar y guardar una ruta al finalizar la grabación.
 * Lee el estado directamente del ViewModel. Solo visible cuando hay una solicitud pendiente.
 */
@Composable
fun RouteNameDialog() {
    val viewModel = LocalMapViewModel.current
    val routeSaveRequest by viewModel.routeSaveRequest.collectAsState()
    val request = routeSaveRequest ?: return

    var name by remember(request.suggestedName) { mutableStateOf(request.suggestedName) }

    AlertDialog(
        onDismissRequest = viewModel::cancelRouteSave,
        title = { Text("Guardar ruta") },
        text = {
            Column {
                Text("Escribe un nombre para esta ruta:")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.confirmRouteSave(name.ifBlank { request.suggestedName }) },
                enabled = name.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = viewModel::cancelRouteSave) { Text("Descartar") }
        }
    )
}