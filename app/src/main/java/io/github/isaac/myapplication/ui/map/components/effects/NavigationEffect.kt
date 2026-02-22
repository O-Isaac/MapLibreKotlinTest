package io.github.isaac.myapplication.ui.map.components.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.isaac.myapplication.ui.map.locals.LocalMapState
import io.github.isaac.myapplication.ui.map.locals.LocalMapViewModel

/**
 * Observa targetLocation del ViewModel.
 * Cuando cambia, anima la cámara a esa posición y notifica al ViewModel
 * que la navegación se completó (para limpiar el estado).
 */
@Composable
fun NavigationEffect() {
    val viewModel = LocalMapViewModel.current
    val mapState = LocalMapState.current
    val targetLoc by viewModel.targetLocation.collectAsState()

    LaunchedEffect(targetLoc) {
        targetLoc?.let { coords ->
            mapState.animateToLocation(coords)
            viewModel.navigationDone()
        }
    }
}