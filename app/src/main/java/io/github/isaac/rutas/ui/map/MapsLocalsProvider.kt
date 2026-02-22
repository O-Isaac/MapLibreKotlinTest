package io.github.isaac.rutas.ui.map

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import io.github.isaac.rutas.ui.map.locals.LocalMapState
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel
import io.github.isaac.rutas.ui.map.locals.LocalPermissionLauncher
import io.github.isaac.rutas.ui.map.locals.LocalRequestHighAccuracy


/**
 * Punto central de inyección de dependencias para la pantalla del mapa.
 *
 * Responsabilidades:
 *  - Crear los launchers de permisos y resolución de ubicación
 *  - Registrar la lógica de "pedir alta precisión"
 *  - Proveer todos a sus hijos vía CompositionLocal
 *
 * NO contiene lógica de negocio ni estado de UI propio.
 */
@Composable
fun MapsLocalsProviders(viewModel: MapViewModel, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val recordingInterval by viewModel.recordingIntervalSec.collectAsState()
    val settingsClient = remember { LocationServices.getSettingsClient(context) }

    // --- Launcher: resolución de configuración de ubicación ---
    val resolutionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.centerCameraOnUser()
        }
    }

    // --- Lógica: solicitar alta precisión GPS ---
    val requestHighAccuracy: (onReady: () -> Unit) -> Unit = remember(settingsClient) {
        { onReady ->
            val request = LocationRequest
                .Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMinUpdateIntervalMillis(500L)
                .build()

            val settingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(request)
                .setAlwaysShow(true)
                .build()

            settingsClient.checkLocationSettings(settingsRequest)
                .addOnSuccessListener { onReady() }
                .addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        val intentRequest = IntentSenderRequest.Builder(exception.resolution).build()
                        resolutionLauncher.launch(intentRequest)
                    }

                    // Si no es resolvable, simplemente no llamamos onReady
                }
        }
    }

    // --- Launcher: permisos de ubicación ---
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            viewModel.mapState.onLocationPermissionGranted()
            viewModel.startLiveLocationUpdates(recordingInterval)
            viewModel.centerCameraOnUser()
        }
    }

    CompositionLocalProvider(
        LocalMapViewModel provides viewModel,
        LocalMapState provides viewModel.mapState,
        LocalPermissionLauncher provides permissionLauncher,
        LocalRequestHighAccuracy provides requestHighAccuracy
    ) {
        content()
    }

}