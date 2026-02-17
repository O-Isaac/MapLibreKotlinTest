package io.github.isaac.myapplication.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.maps.MapView

@Composable
fun MapView.BindLifecycle(mapState: MapState) { // Pasamos el mapState
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> onCreate(null)
                Lifecycle.Event.ON_START -> onStart()
                Lifecycle.Event.ON_RESUME -> {
                    onResume()
                    mapState.toggleLocationUpdates(true) // Reanuda GPS
                }
                Lifecycle.Event.ON_PAUSE -> {
                    mapState.toggleLocationUpdates(false) // Para GPS al ir a 2º plano
                    onPause()
                }
                Lifecycle.Event.ON_STOP -> onStop()
                Lifecycle.Event.ON_DESTROY -> onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}