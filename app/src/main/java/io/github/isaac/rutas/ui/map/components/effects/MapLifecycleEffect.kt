package io.github.isaac.rutas.ui.map.components.effects

import androidx.compose.runtime.Composable
import io.github.isaac.rutas.ui.map.BindLifecycle
import io.github.isaac.rutas.ui.map.locals.LocalMapState

/**
 * Enlaza el ciclo de vida nativo del MapView de MapLibre con el Lifecycle de Compose.
 * Debe llamarse una vez desde la pantalla principal.
 */
@Composable
fun MapLifecycleEffect() {
    val mapState = LocalMapState.current
    mapState.mapView.BindLifecycle(mapState)
}
