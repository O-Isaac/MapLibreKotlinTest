package io.github.isaac.rutas.ui.map.components.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.isaac.rutas.ui.map.isHighAccuracyEnabled
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel

/**
 * Observa los eventos ON_START y ON_STOP del ciclo de vida:
 *  - ON_START: reactiva actualizaciones de ubicación si hay permiso,
 *              y re-comprueba la precisión GPS.
 *  - ON_STOP:  para las actualizaciones para ahorrar batería.
 *  - onDispose: limpieza final al salir de la pantalla.
 */
@Composable
fun LifecycleLocationEffect(
    hasLocationPermission: Boolean,
    onAccuracyDialogRequired: () -> Unit
) {
    val viewModel = LocalMapViewModel.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val recordingInterval by viewModel.recordingIntervalSec.collectAsState()

    DisposableEffect(lifecycleOwner, hasLocationPermission, recordingInterval) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (hasLocationPermission) {
                        viewModel.startLiveLocationUpdates(recordingInterval)
                    }

                    if (!isHighAccuracyEnabled(context)) {
                        onAccuracyDialogRequired()
                    }
                }

                Lifecycle.Event.ON_STOP ->
                    viewModel.stopLiveLocationUpdates()
                else -> Unit
            }
        }


        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopLiveLocationUpdates()
        }
    }
}