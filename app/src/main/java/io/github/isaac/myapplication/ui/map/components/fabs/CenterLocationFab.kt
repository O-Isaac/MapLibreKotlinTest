package io.github.isaac.myapplication.ui.map.components.fabs

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.isaac.myapplication.ui.map.isHighAccuracyEnabled
import io.github.isaac.myapplication.ui.map.locals.LocalMapViewModel
import io.github.isaac.myapplication.ui.map.locals.LocalPermissionLauncher
import io.github.isaac.myapplication.ui.map.locals.LocalRequestHighAccuracy

/**
 * FAB que centra la cámara en la posición actual del usuario.
 * Si la precisión GPS no está activa, solicita activarla primero.
 * Si no hay permiso, lo solicita.
 */
@Composable
fun CenterLocationFab(hasLocationPermission: Boolean) {
    val viewModel = LocalMapViewModel.current
    val context = LocalContext.current
    val permissionLauncher = LocalPermissionLauncher.current
    val requestHighAccuracy = LocalRequestHighAccuracy.current

    FloatingActionButton(
        onClick = {
            val doLocate = {
                if (!hasLocationPermission) {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                } else {
                    viewModel.centerCameraOnUser()
                }
            }
            if (!isHighAccuracyEnabled(context)) {
                requestHighAccuracy(doLocate)
            } else {
                doLocate()
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(Icons.Default.MyLocation, contentDescription = "Centrar ubicación")
    }
}