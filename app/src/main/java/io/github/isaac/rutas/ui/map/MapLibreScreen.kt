package io.github.isaac.rutas.ui.map

import android.Manifest
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.github.isaac.rutas.ui.map.components.MapOverlays
import io.github.isaac.rutas.ui.map.components.PermissionOverlay
import io.github.isaac.rutas.ui.map.locals.LocalMapState
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel
import io.github.isaac.rutas.ui.map.orchestrators.MapDialogsOrchestrator
import io.github.isaac.rutas.ui.map.orchestrators.MapEffectsOrchestrator
import io.github.isaac.rutas.ui.map.utils.hasLocationPermission
import io.github.isaac.rutas.ui.map.viewmodels.MapViewModel
import io.github.isaac.rutas.utils.dbValueToMs

@Composable
fun MapLibreScreen(modifier: Modifier = Modifier, viewModel: MapViewModel) {
    MapsLocalsProviders(viewModel) {
        MapLibreContent(modifier)
    }
}

@Composable
private fun MapLibreContent(modifier: Modifier = Modifier) {
    val viewModel = LocalMapViewModel.current
    val mapState = LocalMapState.current
    val context = LocalContext.current
    val recordingInterval by viewModel.recordingIntervalSec.collectAsState()

    var hasLocationPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    var showAccuracyDialog by remember { mutableStateOf(false) }
    var showWaypointsDialog by remember { mutableStateOf(false) }
    var pendingPhotoPath by remember { mutableStateOf<String?>(null) }
    var startRecordingRequested by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (!success) pendingPhotoPath = null }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.any { it }

        if (hasLocationPermission) {
            mapState.onLocationPermissionGranted()
            viewModel.startLiveLocationUpdates(dbValueToMs(recordingInterval))
            viewModel.centerCameraOnUser()
            if (startRecordingRequested) viewModel.startRecording()
        }

        startRecordingRequested = false
    }

    MapEffectsOrchestrator(
        hasLocationPermission = hasLocationPermission,
        onAccuracyDialogRequired = { showAccuracyDialog = true }
    )

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                val mapView = mapState.mapView
                (mapView.parent as? ViewGroup)?.removeView(mapView)
                mapView
            },
            modifier = Modifier.fillMaxSize(),
            update = {}
        )

        if (!hasLocationPermission) {
            PermissionOverlay(
                onRequestPermission = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                }
            )
        }

        MapOverlays(
            hasLocationPermission = hasLocationPermission,
            onShowWaypointsDialog = { showWaypointsDialog = true },
            onStartRecordingRequest = {
                startRecordingRequested = true
                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        )
    }

    MapDialogsOrchestrator(
        showAccuracyDialog = showAccuracyDialog,
        showWaypointsDialog = showWaypointsDialog,
        pendingPhotoPath = pendingPhotoPath,
        onAccuracyDialogDismiss = { showAccuracyDialog = false },
        onWaypointsDialogDismiss = { showWaypointsDialog = false },
        onPhotoTaken = { path -> pendingPhotoPath = path },
        onPhotoClear = { pendingPhotoPath = null },
        onPhotoLaunch = { uri -> photoLauncher.launch(uri) }
    )
}