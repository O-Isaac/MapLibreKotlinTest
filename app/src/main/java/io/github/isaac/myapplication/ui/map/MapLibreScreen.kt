package io.github.isaac.myapplication.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.isaac.myapplication.ui.components.DialogMarker

@Composable
fun MapLibreScreen(modifier: Modifier = Modifier, viewModel: MapViewModel) {
    val markers by viewModel.markers.collectAsState()
    val pendingLatLng by viewModel.pendingMarker.collectAsState()
    val targetLoc by viewModel.targetLocation.collectAsState()
    val mapState = viewModel.mapState

    // 1.1 Escuchar eventos de navegación
    LaunchedEffect(targetLoc) {
        targetLoc?.let { coords ->
            mapState.animateToLocation(coords)
            viewModel.navigationDone() // Importante: limpiar el estado para que no repita la animación
        }
    }

    // 2. Gestionamos el ciclo de vida con tu extensión
    mapState.mapView.BindLifecycle(mapState)

    LaunchedEffect(markers) {
        mapState.updateMarkers(markers)
    }

    LaunchedEffect(targetLoc) {
        targetLoc?.let {
            mapState.animateToLocation(it)
            viewModel.navigationDone()
        }
    }



    pendingLatLng?.let {
        DialogMarker(
            onDismiss = { viewModel.dismissDialog() },
            onConfirm = { name -> viewModel.confirmMarker(name) }
        )
    }

    // IMPORTANTE: Usamos factory directamente con la instancia persistente
    AndroidView(
        factory = { mapState.mapView },
        modifier = modifier.fillMaxSize(),
        update = { }
    )
}