package io.github.isaac.myapplication.ui.map.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.isaac.myapplication.ui.map.locals.LocalMapViewModel

/**
 * Muestra el diálogo para nombrar un nuevo marcador.
 * Solo visible cuando hay una ubicación pendiente en el ViewModel.
 */
@Composable
fun MarkerDialog() {
    val viewModel = LocalMapViewModel.current
    val pendingLatLng by viewModel.pendingMarker.collectAsState()

    pendingLatLng ?: return

    DialogMarker(
        onDismiss = viewModel::dismissDialog,
        onConfirm = viewModel::confirmMarker
    )
}