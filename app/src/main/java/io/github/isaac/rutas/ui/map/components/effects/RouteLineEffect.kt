package io.github.isaac.rutas.ui.map.components.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.isaac.rutas.ui.map.locals.LocalMapState
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel

/**
 * Mantiene la línea de ruta del mapa sincronizada con el estado del ViewModel.
 * Funciona tanto para rutas en grabación como para rutas seleccionadas.
 */
@Composable
fun RouteLineEffect() {
    val viewModel = LocalMapViewModel.current
    val mapState = LocalMapState.current
    val routePoints by viewModel.mapRoutePoints.collectAsState()

    LaunchedEffect(routePoints) {
        mapState.updateRouteLine(routePoints)
    }
}