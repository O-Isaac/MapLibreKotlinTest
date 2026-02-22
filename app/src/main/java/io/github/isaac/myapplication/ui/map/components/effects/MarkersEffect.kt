package io.github.isaac.myapplication.ui.map.components.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.isaac.myapplication.ui.map.locals.LocalMapState
import io.github.isaac.myapplication.ui.map.locals.LocalMapViewModel

/**
 * Mantiene los marcadores del mapa sincronizados con el estado del ViewModel.
 * Se re-ejecuta cada vez que la lista de marcadores cambia.
 */
@Composable
fun MarkersEffect() {
    val viewModel = LocalMapViewModel.current
    val mapState = LocalMapState.current
    val markers by viewModel.markers.collectAsState()

    LaunchedEffect(markers) {
        mapState.updateMarkers(markers)
    }
}