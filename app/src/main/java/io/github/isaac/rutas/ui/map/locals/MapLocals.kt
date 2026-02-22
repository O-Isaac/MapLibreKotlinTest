package io.github.isaac.rutas.ui.map.locals

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.isaac.rutas.ui.map.MapState
import io.github.isaac.rutas.ui.map.viewmodels.MapViewModel

// staticCompositionLocalOf → para objetos que no cambian (ViewModel, MapState)
// compositionLocalOf    → para valores que pueden cambiar y necesitan recomposición

val LocalMapViewModel = staticCompositionLocalOf<MapViewModel> {
    error("MapViewModel no proporcionado. Envuelve el contenido con LocalsProvider.")
}

val LocalMapState = staticCompositionLocalOf<MapState> {
    error("MapState no proporcionado. Envuelve el contenido con LocalsProvider.")
}

// Launchers: se crean en la pantalla y se pasan a los locales para que
// los componentes puedan lanzarlos sin recibirlos por parámetro
val LocalPermissionLauncher = staticCompositionLocalOf<ActivityResultLauncher<Array<String>>> {
    error("PermissionLauncher no proporcionado.")
}

val LocalRequestHighAccuracy = staticCompositionLocalOf<(onReady: () -> Unit) -> Unit> {
    error("RequestHighAccuracy no proporcionado.")
}