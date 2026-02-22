package io.github.isaac.rutas.ui.configuracion.components.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.isaac.rutas.ui.configuracion.utils.isHighAccuracyEnabled

/**
 * Refresca [onAccuracyChanged] cada vez que la pantalla vuelve a primer plano (ON_START).
 * Permite que el estado del chip "Activada/Desactivada" se actualice tras volver de Ajustes.
 */
@Composable
fun HighAccuracyCheckEffect(onAccuracyChanged: (Boolean) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                onAccuracyChanged(isHighAccuracyEnabled(context))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}