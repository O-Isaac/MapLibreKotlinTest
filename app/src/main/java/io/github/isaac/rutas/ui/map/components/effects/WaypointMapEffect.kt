package io.github.isaac.rutas.ui.map.components.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.isaac.rutas.ui.map.WaypointMarker
import io.github.isaac.rutas.ui.map.locals.LocalMapState
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel
import org.maplibre.android.geometry.LatLng

/**
 * Mantiene los waypoints del mapa sincronizados con el estado del ViewModel.
 * Convierte los waypoints del dominio al tipo que acepta MapState.
 */
@Composable
fun WaypointsMapEffect() {
    val viewModel = LocalMapViewModel.current
    val mapState = LocalMapState.current
    val waypoints by viewModel.mapWaypoints.collectAsState()

    LaunchedEffect(waypoints) {
        mapState.updateWaypoints(
            waypoints.map {
                WaypointMarker(
                    position = LatLng(it.lat, it.lng),
                    description = it.descripcion,
                    photoPath = it.fotoPath
                )
            }
        )
    }
}