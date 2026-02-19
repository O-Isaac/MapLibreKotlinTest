package io.github.isaac.myapplication.ui.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import io.github.isaac.myapplication.ui.components.DialogMarker
import io.github.isaac.myapplication.ui.components.RouteNameDialog
import io.github.isaac.myapplication.ui.components.WaypointDialog
import java.io.File
import java.util.Locale
import java.util.UUID

@Composable
fun MapLibreScreen(modifier: Modifier = Modifier, viewModel: MapViewModel) {
    val markers by viewModel.markers.collectAsState()
    val pendingLatLng by viewModel.pendingMarker.collectAsState()
    val targetLoc by viewModel.targetLocation.collectAsState()
    val recordingState by viewModel.recordingState.collectAsState()
    val routePoints by viewModel.mapRoutePoints.collectAsState()
    val waypoints by viewModel.mapWaypoints.collectAsState()
    val pendingWaypointLocation by viewModel.pendingWaypointLocation.collectAsState()
    val routeSaveRequest by viewModel.routeSaveRequest.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val recordingInterval by viewModel.recordingIntervalSec.collectAsState()
    val selectedWaypoint by viewModel.selectedWaypoint.collectAsState()
    val mapState = viewModel.mapState
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var pendingPhotoPath by remember { mutableStateOf<String?>(null) }
    var startRecordingRequested by remember { mutableStateOf(false) }
    var hasLocationPermission by remember {
        mutableStateOf(hasLocationPermission(context))
    }
    var showAccuracyDialog by remember { mutableStateOf(false) }
    var showWaypointsDialog by remember { mutableStateOf(false) }
    val settingsClient = remember { LocationServices.getSettingsClient(context) }
    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) {
            pendingPhotoPath = null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.any { it }
        if (hasLocationPermission) {
            mapState.onLocationPermissionGranted()
            viewModel.startLiveLocationUpdates(recordingInterval)
            viewModel.centerCameraOnUser()
            if (startRecordingRequested) {
                viewModel.startRecording()
            }
        }
        startRecordingRequested = false
    }

    val resolutionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (!hasLocationPermission) {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                )
            } else {
                viewModel.centerCameraOnUser()
            }
        }
    }

    val requestHighAccuracy: (() -> Unit) -> Unit = { onReady ->
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
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
                } else {
                    showAccuracyDialog = true
                }
            }
    }

    LaunchedEffect(Unit) {
        showAccuracyDialog = !isHighAccuracyEnabled(context)
    }

    LaunchedEffect(hasLocationPermission, recordingInterval) {
        if (hasLocationPermission) {
            viewModel.startLiveLocationUpdates(recordingInterval)
        } else {
            viewModel.stopLiveLocationUpdates()
        }
    }

    DisposableEffect(lifecycleOwner, hasLocationPermission, recordingInterval) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (hasLocationPermission) {
                        viewModel.startLiveLocationUpdates(recordingInterval)
                    }
                    showAccuracyDialog = !isHighAccuracyEnabled(context)
                }
                Lifecycle.Event.ON_STOP -> {
                    viewModel.stopLiveLocationUpdates()
                }
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopLiveLocationUpdates()
        }
    }

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

    LaunchedEffect(routePoints) {
        mapState.updateRouteLine(routePoints)
    }

    LaunchedEffect(waypoints) {
        mapState.updateWaypoints(
            waypoints.map {
                WaypointMarker(
                    position = org.maplibre.android.geometry.LatLng(it.lat, it.lng),
                    description = it.descripcion,
                    photoPath = it.fotoPath
                )
            }
        )
    }



    pendingLatLng?.let {
        DialogMarker(
            onDismiss = { viewModel.dismissDialog() },
            onConfirm = { name -> viewModel.confirmMarker(name) }
        )
    }

    pendingWaypointLocation?.let {
        WaypointDialog(
            description = "",
            photoPath = pendingPhotoPath,
            onTakePhoto = {
                val (uri, path) = createWaypointPhotoUri(context)
                pendingPhotoPath = path
                photoLauncher.launch(uri)
            },
            onConfirm = { description ->
                viewModel.confirmWaypoint(description, pendingPhotoPath)
                pendingPhotoPath = null
            },
            onDismiss = {
                pendingPhotoPath = null
                viewModel.dismissWaypointDialog()
            }
        )
    }

    routeSaveRequest?.let { request ->
        RouteNameDialog(
            defaultName = request.suggestedName,
            onConfirm = viewModel::confirmRouteSave,
            onDismiss = viewModel::cancelRouteSave
        )
    }

    // IMPORTANTE: Usamos factory directamente con la instancia persistente
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                val mapView = mapState.mapView
                (mapView.parent as? ViewGroup)?.removeView(mapView)
                mapView
            },
            modifier = Modifier.fillMaxSize(),
            update = { }
        )

        if (!hasLocationPermission) {
            PermissionOverlay(
                onRequestPermission = {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                    )
                }
            )
        }

        if (hasLocationPermission) {
            RecordingStatsCard(
                isVisible = recordingState.isRecording,
                distanceMeters = recordingState.distanceMeters,
                elapsedMs = recordingState.elapsedMs,
                avgSpeed = recordingState.avgSpeedKmh,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
            )

            selectedRoute.route?.let { route ->
                RouteSummaryCard(
                    route = route,
                    onClose = viewModel::clearSelectedRoute,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 84.dp, start = 12.dp, end = 12.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (selectedRoute.route != null) {
                    ExtendedFloatingActionButton(
                        onClick = { showWaypointsDialog = true },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                        text = { Text("Waypoints") }
                    )
                }

                if (recordingState.isRecording) {
                    ExtendedFloatingActionButton(
                        onClick = viewModel::requestWaypoint,
                        icon = { Icon(Icons.Default.AddLocationAlt, null) },
                        text = { Text("Anadir waypoint") },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                RecordingControls(
                    isRecording = recordingState.isRecording,
                    isPaused = recordingState.isPaused,
                    onStart = {
                        startRecordingRequested = true
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                        )
                    },
                    onPause = viewModel::pauseRecording,
                    onResume = viewModel::resumeRecording,
                    onStop = viewModel::stopRecording
                )

                FloatingActionButton(
                    onClick = {
                        val doLocate = {
                            if (!hasLocationPermission) {
                                permissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                                )
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
                    Icon(Icons.Default.MyLocation, contentDescription = "Centrar ubicacion")
                }
            }
        }
    }

    if (showAccuracyDialog) {
        AlertDialog(
            onDismissRequest = { showAccuracyDialog = false },
            title = { Text("Activa la ubicacion precisa") },
            text = {
                Text(
                    "Para una mejor experiencia, tu dispositivo necesita usar la ubicacion precisa.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openLocationSettings(context)
                        showAccuracyDialog = false
                    }
                ) {
                    Text("Activar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccuracyDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    selectedWaypoint?.let { details ->
        io.github.isaac.myapplication.ui.components.WaypointInfoDialog(
            description = details.description,
            photoPath = details.photoPath,
            onDismiss = viewModel::dismissWaypointDetails
        )
    }

    if (showWaypointsDialog) {
        io.github.isaac.myapplication.ui.components.WaypointListDialog(
            waypoints = selectedRoute.waypoints,
            onDismiss = { showWaypointsDialog = false },
            onSelect = { waypoint ->
                showWaypointsDialog = false
                viewModel.openWaypoint(waypoint)
            }
        )
    }
}

@Composable
private fun PermissionOverlay(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.LocationOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Necesitamos permiso de ubicacion para mostrar el mapa y grabar rutas.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                ExtendedFloatingActionButton(
                    onClick = onRequestPermission,
                    icon = { Icon(Icons.Default.MyLocation, null) },
                    text = { Text("Conceder permiso") }
                )
            }
        }
    }
}

@Composable
private fun RecordingControls(
    isRecording: Boolean,
    isPaused: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    if (!isRecording) {
        ExtendedFloatingActionButton(
            onClick = onStart,
            icon = { Icon(Icons.Default.PlayArrow, null) },
            text = { Text("Iniciar ruta") }
        )
        return
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ExtendedFloatingActionButton(
            onClick = if (isPaused) onResume else onPause,
            icon = {
                Icon(
                    if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    null
                )
            },
            text = { Text(if (isPaused) "Reanudar" else "Pausa") }
        )
        FloatingActionButton(
            onClick = onStop,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Icon(Icons.Default.Stop, contentDescription = "Detener")
        }
    }
}

@Composable
private fun RecordingStatsCard(
    isVisible: Boolean,
    distanceMeters: Double,
    elapsedMs: Long,
    avgSpeed: Double,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatItem("Distancia", formatDistance(distanceMeters))
            StatItem("Tiempo", formatDuration(elapsedMs))
            StatItem("Vel. media", "${"%.2f".format(avgSpeed)} km/h")
        }
    }
}

@Composable
private fun RouteSummaryCard(
    route: io.github.isaac.myapplication.data.local.entities.Ruta,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(route.nombre.ifBlank { "Ruta" }, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${formatDistance(route.distancia)}")
                    Text(formatDuration(route.duracion))
                    Text("${"%.2f".format(route.velocidadMedia)} km/h")
                }
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

private fun formatDistance(distanceMeters: Double): String {
    return if (distanceMeters >= 1000) {
        "${"%.2f".format(distanceMeters / 1000.0)} km"
    } else {
        "${"%.0f".format(distanceMeters)} m"
    }
}

private fun createWaypointPhotoUri(context: Context): Pair<Uri, String> {
    val imagesDir = File(context.filesDir, "waypoints")
    imagesDir.mkdirs()
    val file = File(imagesDir, "${UUID.randomUUID()}.jpg")
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    return uri to file.absolutePath
}

private fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine
}

private fun isHighAccuracyEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val networkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    val locationMode = try {
        Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.LOCATION_MODE
        )
    } catch (exception: Settings.SettingNotFoundException) {
        Settings.Secure.LOCATION_MODE_OFF
    }

    return locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY ||
        (gpsEnabled && networkEnabled)
}

private fun openLocationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK
    )
    context.startActivity(intent)
}