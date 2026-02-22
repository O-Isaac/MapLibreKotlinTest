package io.github.isaac.rutas.ui.map.orchestrators

import androidx.compose.runtime.Composable
import io.github.isaac.rutas.ui.map.components.effects.AccuracyCheckEffect
import io.github.isaac.rutas.ui.map.components.effects.LifecycleLocationEffect
import io.github.isaac.rutas.ui.map.components.effects.LocationUpdatesEffect
import io.github.isaac.rutas.ui.map.components.effects.MapLifecycleEffect
import io.github.isaac.rutas.ui.map.components.effects.MarkersEffect
import io.github.isaac.rutas.ui.map.components.effects.NavigationEffect
import io.github.isaac.rutas.ui.map.components.effects.RouteLineEffect
import io.github.isaac.rutas.ui.map.components.effects.WaypointsMapEffect

/**
 * Orquesta todos los efectos secundarios del mapa.
 * Sin UI. Se coloca una sola vez en MapLibreContent.
 *
 * @param hasLocationPermission estado actual del permiso de ubicación
 * @param onAccuracyDialogRequired callback para mostrar el diálogo de precisión GPS
 */
@Composable
fun MapEffectsOrchestrator(
    hasLocationPermission: Boolean,
    onAccuracyDialogRequired: () -> Unit
) {
    MapLifecycleEffect()
    AccuracyCheckEffect(onAccuracyDialogRequired = onAccuracyDialogRequired)
    LocationUpdatesEffect(hasLocationPermission)
    LifecycleLocationEffect(
        hasLocationPermission = hasLocationPermission,
        onAccuracyDialogRequired = onAccuracyDialogRequired
    )
    NavigationEffect()
    MarkersEffect()
    RouteLineEffect()
    WaypointsMapEffect()
}
