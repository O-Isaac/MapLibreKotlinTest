package io.github.isaac.myapplication.ui.map

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
import io.github.isaac.myapplication.ui.map.components.MapOverlays
import io.github.isaac.myapplication.ui.map.components.PermissionOverlay
import io.github.isaac.myapplication.ui.map.components.dialogs.AccuracyDialog
import io.github.isaac.myapplication.ui.map.components.dialogs.MarkerDialog
import io.github.isaac.myapplication.ui.map.components.dialogs.RouteNameDialog
import io.github.isaac.myapplication.ui.map.components.dialogs.WaypointCreationDialog
import io.github.isaac.myapplication.ui.map.components.dialogs.WaypointInfoDialog
import io.github.isaac.myapplication.ui.map.components.dialogs.WaypointListDialog
import io.github.isaac.myapplication.ui.map.components.effects.AccuracyCheckEffect
import io.github.isaac.myapplication.ui.map.components.effects.LifecycleLocationEffect
import io.github.isaac.myapplication.ui.map.components.effects.LocationUpdatesEffect
import io.github.isaac.myapplication.ui.map.components.effects.MapLifecycleEffect
import io.github.isaac.myapplication.ui.map.components.effects.MarkersEffect
import io.github.isaac.myapplication.ui.map.components.effects.NavigationEffect
import io.github.isaac.myapplication.ui.map.components.effects.RouteLineEffect
import io.github.isaac.myapplication.ui.map.components.effects.WaypointsMapEffect
import io.github.isaac.myapplication.ui.map.locals.LocalMapState
import io.github.isaac.myapplication.ui.map.locals.LocalMapViewModel

/**
 * Punto de entrada de la pantalla del mapa.
 * Solo instala los LocalsProvider y delega todo lo demás.
 */
@Composable
fun MapLibreScreen(modifier: Modifier = Modifier, viewModel: MapViewModel) {
    MapsLocalsProviders(viewModel) {
        MapLibreContent(modifier)
    }
}

/**
 * Contenido interno de la pantalla. Gestiona únicamente:
 *  - Estado local de UI (permisos, flags de diálogos, foto pendiente)
 *  - Launchers de sistema (permisos, cámara)
 *  - Orquestación de Effects, UI y Dialogs
 *
 * Sin lógica de negocio. Sin utilidades de sistema.
 */
@Composable
private fun MapLibreContent(modifier: Modifier = Modifier) {
    val viewModel = LocalMapViewModel.current
    val mapState = LocalMapState.current
    val context = LocalContext.current
    val recordingInterval by viewModel.recordingIntervalSec.collectAsState()

    // ── Estado local de UI ─────────────────────────────────────────────────────
    var hasLocationPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    var showAccuracyDialog by remember { mutableStateOf(false) }
    var showWaypointsDialog by remember { mutableStateOf(false) }
    var pendingPhotoPath by remember { mutableStateOf<String?>(null) }
    var startRecordingRequested by remember { mutableStateOf(false) }

    // ── Launchers ──────────────────────────────────────────────────────────────
    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (!success) pendingPhotoPath = null }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.any { it }
        if (hasLocationPermission) {
            mapState.onLocationPermissionGranted()
            viewModel.startLiveLocationUpdates(recordingInterval)
            viewModel.centerCameraOnUser()
            if (startRecordingRequested) viewModel.startRecording()
        }
        startRecordingRequested = false
    }

    // ── Effects ────────────────────────────────────────────────────────────────
    // Crear un orquestador global de effectos
    MapLifecycleEffect()
    AccuracyCheckEffect(onAccuracyDialogRequired = { showAccuracyDialog = true })
    LocationUpdatesEffect(hasLocationPermission)
    LifecycleLocationEffect(
        hasLocationPermission = hasLocationPermission,
        onAccuracyDialogRequired = { showAccuracyDialog = true }
    )
    NavigationEffect()
    MarkersEffect()
    RouteLineEffect()
    WaypointsMapEffect()

    // ── UI ─────────────────────────────────────────────────────────────────────
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

    // ── Dialogs ────────────────────────────────────────────────────────────────
    MarkerDialog()
    WaypointCreationDialog(
        pendingPhotoPath = pendingPhotoPath,
        onPhotoTaken = { path -> pendingPhotoPath = path },
        onPhotoClear = { pendingPhotoPath = null },
        onPhotoLaunch = { uri -> photoLauncher.launch(uri) }
    )
    // Error falta parametros
    RouteNameDialog()
    // Error falta parametros
    WaypointInfoDialog()

    if (showWaypointsDialog) {
        // Error falta parametros
        WaypointListDialog(
            onDismiss = { showWaypointsDialog = false }
        )
    }
    if (showAccuracyDialog) {
        AccuracyDialog(onDismiss = { showAccuracyDialog = false })
    }
}