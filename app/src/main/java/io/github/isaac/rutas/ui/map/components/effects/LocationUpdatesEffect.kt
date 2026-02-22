package io.github.isaac.rutas.ui.map.components.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel
import io.github.isaac.rutas.utils.dbValueToMs

/**
 * Arranca o para las actualizaciones de ubicación en tiempo real
 * según si el permiso está concedido y el intervalo configurado.
 */
@Composable
fun LocationUpdatesEffect(hasLocationPermission: Boolean) {
    val viewModel = LocalMapViewModel.current
    val recordingInterval by viewModel.recordingIntervalSec.collectAsState()

    LaunchedEffect(hasLocationPermission, recordingInterval) {
        if (hasLocationPermission) {
            viewModel.startLiveLocationUpdates(dbValueToMs(recordingInterval))
        } else {
            viewModel.stopLiveLocationUpdates()
        }
    }
}