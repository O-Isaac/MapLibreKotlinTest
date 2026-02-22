package io.github.isaac.rutas.ui.map.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.database.Cursor
import android.location.Location
import android.os.Looper
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.util.Xml
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.FileProvider
import androidx.lifecycle.asLiveData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import io.github.isaac.rutas.BaseApplication
import io.github.isaac.rutas.Constants
import io.github.isaac.rutas.data.local.entities.MapSettingsEntity
import io.github.isaac.rutas.data.local.entities.MarkerEntity
import io.github.isaac.rutas.data.local.entities.PuntoRuta
import io.github.isaac.rutas.data.local.entities.Ruta
import io.github.isaac.rutas.data.local.entities.Waypoint
import io.github.isaac.rutas.data.local.entities.toMarkerData
import io.github.isaac.rutas.data.local.repositories.MapRepository
import io.github.isaac.rutas.data.local.repositories.RutaRepository
import io.github.isaac.rutas.data.local.repositories.SettingsRepository
import io.github.isaac.rutas.data.model.MarkerData
import io.github.isaac.rutas.ui.map.MapState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import org.maplibre.android.geometry.LatLng
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.coroutines.resume

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as BaseApplication).database
    private val mapRepository = MapRepository(database.mapDao())
    private val rutaRepository = RutaRepository(database.rutaDao())

    private val settingsRepository = SettingsRepository(database.settingsDao());

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // --- PERSISTENCIA: MARCADORES ---
    // Convertimos automáticamente los MarkerEntity (DB) a MarkerData (UI)
    val markers: StateFlow<List<MarkerData>> = mapRepository.getAllMarkers()
        .map { it.toMarkerData() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- ESTADO DE LA UI ---
    val mapState: MapState by lazy {
        MapState(application.applicationContext, this, ::onLongClick, ::showWaypointDetails)
    }

    private val _pendingMarker = MutableStateFlow<LatLng?>(null)
    val pendingMarker = _pendingMarker.asStateFlow()

    private val _targetLocation = MutableStateFlow<LatLng?>(null)
    val targetLocation = _targetLocation.asStateFlow()

    // Para mantener la posición de la cámara tras rotaciones o cambios de pantalla
    var lastCameraPosition: LatLng = LatLng(40.4167, -3.7033)
    var lastZoom: Double = 10.0

    private val _recordingState = MutableStateFlow(RecordingState())
    val recordingState = _recordingState.asStateFlow()

    private val _recordingTrack = MutableStateFlow<List<LatLng>>(emptyList())
    val recordingTrack = _recordingTrack.asStateFlow()

    private val _recordingWaypoints = MutableStateFlow<List<Waypoint>>(emptyList())
    val recordingWaypoints = _recordingWaypoints.asStateFlow()

    private val _pendingWaypointLocation = MutableStateFlow<LatLng?>(null)
    val pendingWaypointLocation = _pendingWaypointLocation.asStateFlow()

    private val _routeSaveRequest = MutableStateFlow<RouteSaveRequest?>(null)
    val routeSaveRequest = _routeSaveRequest.asStateFlow()

    private val _selectedRoute = MutableStateFlow(RouteDisplayState())
    val selectedRoute = _selectedRoute.asStateFlow()

    private val _selectedWaypoint = MutableStateFlow<WaypointDetails?>(null)
    val selectedWaypoint = _selectedWaypoint.asStateFlow()

    val mapRoutePoints: StateFlow<List<LatLng>> = combine(
        _selectedRoute,
        _recordingTrack
    ) { selected, recording ->
        if (selected.route != null) {
            selected.points.map { LatLng(it.lat, it.lng) }
        } else {
            recording
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val mapWaypoints: StateFlow<List<Waypoint>> = combine(
        _selectedRoute,
        _recordingWaypoints
    ) { selected, recording ->
        if (selected.route != null) selected.waypoints else recording
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val routes: StateFlow<List<Ruta>> = rutaRepository.getAllRutas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recordingIntervalSec: StateFlow<Int> = settingsRepository.getRecordingInterval()
        .map { it ?: 10 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 10
        )

    private var timerJob: Job? = null
    private var lastRecordedLatLng: LatLng? = null
    private var recordingStartEpochMs: Long? = null
    private var liveLocationCallback: LocationCallback? = null
    private var liveLocationIntervalMs: Long? = null  // en ms para mayor precisión
    private val minLocationUpdateMs = 200L  // mínimo físico razonable del GPS

    init {
        ensureDefaultSettings()
    }

    // --- OPERACIONES DE BASE DE DATOS (CRUD) ---

    fun confirmMarker(name: String) {
        _pendingMarker.value?.let { latLng ->
            viewModelScope.launch {
                mapRepository.upsertMarker(
                    MarkerEntity(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        latitude = latLng.latitude,
                        longitude = latLng.longitude
                    )
                )
            }
        }
        _pendingMarker.value = null
    }

    fun deleteMarker(id: String) {
        viewModelScope.launch {
            mapRepository.deleteMarker(id)
        }
    }

    fun updateMarkerName(id: String, newName: String) {
        // Aprovechamos el upsert de Room si la entidad tiene el mismo ID
        // O podemos usar un método específico en el DAO si lo prefieres
        viewModelScope.launch {
            // Nota: Aquí necesitarías recuperar la entidad completa o tener un método UPDATE en el DAO
            // Por simplicidad, si tu DAO tiene @Query UPDATE, úsalo:
            mapRepository.updateMarkerName(id, newName)
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            mapRepository.clearMarkers()
            settingsRepository.clearSettings()
            rutaRepository.clearRutas()
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation() {
        viewModelScope.launch {
            val current = getCurrentLatLng()
            if (current != null) {
                lastCameraPosition = current
                lastZoom = 15.0
                navigateToMarker(current)
                return@launch
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)

                    lastCameraPosition = currentLatLng
                    lastZoom = 15.0

                    navigateToMarker(currentLatLng)
                }
            }
        }
    }

    // MapViewModel.kt

    @SuppressLint("MissingPermission")
    fun centerCameraOnUser() {
        if (mapState.isLocationComponentActive()) {
            mapState.toggleLocationUpdates(true)

            val locationComponent = mapState.map?.locationComponent
            val lastLoc = locationComponent?.lastKnownLocation

            if (lastLoc != null) {
                val currentLatLng = LatLng(lastLoc.latitude, lastLoc.longitude)
                navigateToMarker(currentLatLng)
                return
            }
        }

        // Si el componente no esta listo o no hay ubicacion, usamos el GPS del sistema como respaldo
        fetchCurrentLocation()
    }

    // --- EVENTOS DE INTERACCIÓN ---

    fun onLongClick(latLng: LatLng) {
        if (_recordingState.value.isRecording) {
            _pendingWaypointLocation.value = latLng
        } else {
            _pendingMarker.value = latLng
        }
    }

    fun dismissDialog() {
        _pendingMarker.value = null
    }

    fun navigateToMarker(position: LatLng) {
        _targetLocation.value = position
    }

    fun navigationDone() {
        _targetLocation.value = null
    }

    fun startRecording() {
        if (_recordingState.value.isRecording) return

        viewModelScope.launch {
            val routeId = System.currentTimeMillis()
            rutaRepository.upsertRuta(Ruta(id = routeId))

            _routeSaveRequest.value = null
            _selectedRoute.value = RouteDisplayState()
            _recordingState.value = RecordingState(
                isRecording = true,
                isPaused = false,
                elapsedMs = 0L,
                distanceMeters = 0.0,
                avgSpeedKmh = 0.0,
                routeId = routeId
            )
            _recordingTrack.value = emptyList()
            _recordingWaypoints.value = emptyList()
            lastRecordedLatLng = null
            recordingStartEpochMs = System.currentTimeMillis()

            startTimer()
            startRecordingLoop()
        }
    }

    fun pauseRecording() {
        if (!_recordingState.value.isRecording || _recordingState.value.isPaused) return
        _recordingState.value = _recordingState.value.copy(isPaused = true)
        stopJobs()
    }

    fun resumeRecording() {
        if (!_recordingState.value.isRecording || !_recordingState.value.isPaused) return
        _recordingState.value = _recordingState.value.copy(isPaused = false)
        recordingStartEpochMs = System.currentTimeMillis() - _recordingState.value.elapsedMs
        startTimer()
        startRecordingLoop()
    }

    fun stopRecording() {
        if (!_recordingState.value.isRecording) return
        stopJobs()

        val current = _recordingState.value
        val routeId = current.routeId ?: return
        val defaultName = buildDefaultRouteName()

        _routeSaveRequest.value = RouteSaveRequest(routeId, defaultName)
        _recordingState.value = current.copy(isRecording = false, isPaused = false)
    }

    fun cancelRouteSave() {
        val routeId = _routeSaveRequest.value?.routeId ?: return
        viewModelScope.launch {
            rutaRepository.deleteRuta(routeId)
            resetRecordingState()
        }
    }

    fun confirmRouteSave(name: String) {
        val current = _recordingState.value
        val routeId = _routeSaveRequest.value?.routeId ?: return

        viewModelScope.launch {
            val durationMs = current.elapsedMs
            val distanceMeters = current.distanceMeters
            val avgSpeed = calculateAverageSpeed(distanceMeters, durationMs)

            rutaRepository.upsertRuta(
                Ruta(
                    id = routeId,
                    nombre = name,
                    distancia = distanceMeters,
                    duracion = durationMs,
                    velocidadMedia = avgSpeed
                )
            )

            resetRecordingState()
        }
    }

    fun requestWaypoint() {
        if (!_recordingState.value.isRecording) return
        viewModelScope.launch {
            val location = getCurrentLatLng()
            _pendingWaypointLocation.value = location
        }
    }

    fun dismissWaypointDialog() {
        _pendingWaypointLocation.value = null
    }

    fun showWaypointDetails(details: WaypointDetails) {
        _selectedWaypoint.value = details
    }

    fun dismissWaypointDetails() {
        _selectedWaypoint.value = null
    }

    fun openWaypoint(waypoint: Waypoint) {
        showWaypointDetails(
            WaypointDetails(
                description = waypoint.descripcion,
                photoPath = waypoint.fotoPath
            )
        )
        navigateToMarker(LatLng(waypoint.lat, waypoint.lng))
    }

    fun confirmWaypoint(description: String, photoPath: String?) {
        val routeId = _recordingState.value.routeId ?: return
        val location = _pendingWaypointLocation.value ?: return

        viewModelScope.launch {
            val waypoint = Waypoint(
                rutaId = routeId,
                lat = location.latitude,
                lng = location.longitude,
                descripcion = description,
                fotoPath = photoPath
            )
            rutaRepository.upsertWaypoint(waypoint)
            _recordingWaypoints.value = _recordingWaypoints.value + waypoint
            _pendingWaypointLocation.value = null
        }
    }

    fun selectRoute(routeId: Long) {
        viewModelScope.launch {
            val route = rutaRepository.getRunta(routeId) ?: return@launch
            val points = rutaRepository.getPuntosRutaOnce(routeId)
            val waypoints = rutaRepository.getWaypointsRutaOnce(routeId)

            _selectedRoute.value = RouteDisplayState(route, points, waypoints)
            _recordingState.value = _recordingState.value.copy(isRecording = false, isPaused = false)
        }
    }

    fun exportRouteGpx(
        routeId: Long,
        onReady: (Uri, String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val route = rutaRepository.getRunta(routeId)
            if (route == null) {
                onError("Ruta no encontrada")
                return@launch
            }

            val points = rutaRepository.getPuntosRutaOnce(routeId)
            if (points.isEmpty()) {
                onError("La ruta no tiene puntos")
                return@launch
            }

            val waypoints = rutaRepository.getWaypointsRutaOnce(routeId)
            val gpx = buildGpx(route, points, waypoints)
            val fileName = buildGpxFileName(route)

            val app = getApplication<Application>()
            val uri = withContext(Dispatchers.IO) {
                val exportDir = File(app.cacheDir, "exports").apply { mkdirs() }
                val file = File(exportDir, fileName)
                file.writeText(gpx, Charsets.UTF_8)
                FileProvider.getUriForFile(
                    app,
                    "${app.packageName}.fileprovider",
                    file
                )
            }

            onReady(uri, fileName)
        }
    }

    fun importRouteGpx(
        uri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val result = withContext(Dispatchers.IO) {
                val contentResolver = app.contentResolver
                val stream = contentResolver.openInputStream(uri)
                stream?.use { input ->
                    parseGpx(input)
                }
            }

            if (result == null) {
                onError("No se pudo leer el archivo GPX")
                return@launch
            }

            if (result.points.isEmpty()) {
                onError("El GPX no contiene puntos")
                return@launch
            }

            val displayName = getDisplayName(uri)
            val routeName = result.name
                ?.takeIf { it.isNotBlank() }
                ?: displayName
                    ?.substringBeforeLast('.')
                    ?.takeIf { it.isNotBlank() }
                ?: "Ruta importada"

            val routeId = System.currentTimeMillis()
            val baseTimestamp = System.currentTimeMillis()
            val pointsWithTime = result.points.mapIndexed { index, point ->
                val timestamp = point.timestampMs ?: (baseTimestamp + index * 1000L)
                PuntoRuta(
                    id = routeId + index,
                    rutaId = routeId,
                    lat = point.lat,
                    lng = point.lng,
                    timestamp = timestamp
                )
            }

            val distanceMeters = calculateDistance(pointsWithTime)
            val durationMs = calculateDuration(pointsWithTime)
            val avgSpeed = calculateAverageSpeed(distanceMeters, durationMs)

            rutaRepository.upsertRuta(
                Ruta(
                    id = routeId,
                    nombre = routeName,
                    distancia = distanceMeters,
                    duracion = durationMs,
                    velocidadMedia = avgSpeed
                )
            )

            pointsWithTime.forEach { rutaRepository.upsertPunto(it) }

            val waypointBaseId = routeId + 100000
            result.waypoints.forEachIndexed { index, waypoint ->
                rutaRepository.upsertWaypoint(
                    Waypoint(
                        id = waypointBaseId + index,
                        rutaId = routeId,
                        lat = waypoint.lat,
                        lng = waypoint.lng,
                        descripcion = waypoint.description,
                        fotoPath = null
                    )
                )
            }

            onSuccess(routeName)
        }
    }

    fun clearSelectedRoute() {
        _selectedRoute.value = RouteDisplayState()
    }

    // dbValue: 0 = 500ms, 1..30 = segundos exactos
    fun updateRecordingInterval(dbValue: Int) {
        Log.i("MapViewModel", "updateRecordingInterval: $dbValue (0=500ms, resto=segundos)")
        viewModelScope.launch {
            val selectedStyle = mapRepository.getMapStyle().first() ?: Constants.STYLES_MAP

            settingsRepository.saveSettings(
                MapSettingsEntity(
                    selectedStyleUrl = selectedStyle,
                    recordingIntervalSec = dbValue
                )
            )

            // Si hay grabación activa, aplicar el nuevo intervalo inmediatamente
            if (liveLocationCallback != null) {
                val intervalMs = dbValueToMs(dbValue)
                startLiveLocationUpdates(intervalMs)
            }
        }
    }

    /** Convierte el valor guardado en DB a milisegundos reales */
    private fun dbValueToMs(dbValue: Int): Long =
        if (dbValue == 0) 500L else dbValue * 1000L


    fun deleteRuta(ruta: Ruta) {
        viewModelScope.launch {
            rutaRepository.deleteRuta(ruta.id)
        }
    }

    fun deleteRuta(ruta: Long) {
        viewModelScope.launch {
            rutaRepository.deleteRuta(ruta)
        }
    }

    private fun ensureDefaultSettings() {
        viewModelScope.launch {
            val currentStyle = mapRepository.getMapStyle().first()

            if (currentStyle == null) {
                settingsRepository.saveSettings(
                    MapSettingsEntity(
                        selectedStyleUrl = Constants.STYLES_MAP,
                        recordingIntervalSec = 10
                    )
                )
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val base = recordingStartEpochMs ?: System.currentTimeMillis()
                val elapsed = System.currentTimeMillis() - base
                val distance = _recordingState.value.distanceMeters
                _recordingState.value = _recordingState.value.copy(
                    elapsedMs = elapsed,
                    avgSpeedKmh = calculateAverageSpeed(distance, elapsed)
                )
                delay(1000)
            }
        }
    }

    private fun startRecordingLoop() {
        startLiveLocationUpdates(dbValueToMs(recordingIntervalSec.value))
    }

    @SuppressLint("MissingPermission")
    fun startLiveLocationUpdates(intervalMs: Long) {
        val safeIntervalMs = intervalMs.coerceAtLeast(minLocationUpdateMs)

        // No reiniciamos si el intervalo es el mismo
        if (liveLocationCallback != null && liveLocationIntervalMs == safeIntervalMs) {
            return
        }

        liveLocationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, safeIntervalMs)
            .setMinUpdateIntervalMillis(minLocationUpdateMs)
            .setMaxUpdateDelayMillis(safeIntervalMs)
            .setWaitForAccurateLocation(false)  // true bloquea actualizaciones rápidas < 1s
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                handleLiveLocation(location)
            }
        }

        liveLocationCallback = callback
        liveLocationIntervalMs = safeIntervalMs
        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    fun stopLiveLocationUpdates() {
        liveLocationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        liveLocationCallback = null
        liveLocationIntervalMs = null
        mapState.toggleLocationUpdates(false)
    }

    private fun handleLiveLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        mapState.updateLocationComponent(location)
        lastCameraPosition = latLng

        val state = _recordingState.value
        if (state.isRecording && !state.isPaused) {
            val routeId = state.routeId ?: return
            recordLocation(routeId, location)
        }
    }

    private fun recordLocation(routeId: Long, location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)

        viewModelScope.launch {
            val punto = PuntoRuta(
                rutaId = routeId,
                lat = latLng.latitude,
                lng = latLng.longitude
            )

            rutaRepository.upsertPunto(punto)
        }

        val newDistance = lastRecordedLatLng?.let { previous ->
            _recordingState.value.distanceMeters + calculateDistance(previous, latLng)
        } ?: _recordingState.value.distanceMeters

        lastRecordedLatLng = latLng
        _recordingTrack.value = _recordingTrack.value + latLng
        _recordingState.value = _recordingState.value.copy(
            distanceMeters = newDistance,
            avgSpeedKmh = calculateAverageSpeed(newDistance, _recordingState.value.elapsedMs)
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLatLng(): LatLng? = suspendCancellableCoroutine { cont ->
        val tokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
            .addOnSuccessListener { location ->
                cont.resume(location?.let { LatLng(it.latitude, it.longitude) })
            }
            .addOnFailureListener {
                cont.resume(null)
            }

        cont.invokeOnCancellation { tokenSource.cancel() }
    }

    private fun stopJobs() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun resetRecordingState() {
        _routeSaveRequest.value = null
        _recordingState.value = RecordingState()
        _recordingTrack.value = emptyList()
        _recordingWaypoints.value = emptyList()
        _pendingWaypointLocation.value = null
        lastRecordedLatLng = null
        recordingStartEpochMs = null
    }

    override fun onCleared() {
        stopLiveLocationUpdates()
        super.onCleared()
    }

    private fun buildDefaultRouteName(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return "Ruta ${formatter.format(Date())}"
    }

    private fun buildGpxFileName(route: Ruta): String {
        val baseName = route.nombre.ifBlank { "ruta-${route.id}" }
        val safeName = baseName
            .replace(Regex("[^A-Za-z0-9._-]+"), "_")
            .trim('_')
            .ifBlank { "ruta-${route.id}" }
        return "$safeName.gpx"
    }

    private fun buildGpx(
        route: Ruta,
        points: List<PuntoRuta>,
        waypoints: List<Waypoint>
    ): String {
        val name = escapeXml(route.nombre.ifBlank { "Ruta" })
        val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<gpx version=\"1.1\" creator=\"MapLibreKotlinTest\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")

        if (points.isNotEmpty()) {
            sb.append("<metadata><name>")
                .append(name)
                .append("</name><time>")
                .append(timeFormat.format(Date(points.first().timestamp)))
                .append("</time></metadata>\n")
        } else {
            sb.append("<metadata><name>").append(name).append("</name></metadata>\n")
        }

        waypoints.forEachIndexed { index, waypoint ->
            val waypointName = waypoint.descripcion.ifBlank { "Waypoint ${index + 1}" }
            sb.append("<wpt lat=\"")
                .append(waypoint.lat)
                .append("\" lon=\"")
                .append(waypoint.lng)
                .append("\"><name>")
                .append(escapeXml(waypointName))
                .append("</name>")
            if (waypoint.descripcion.isNotBlank()) {
                sb.append("<desc>")
                    .append(escapeXml(waypoint.descripcion))
                    .append("</desc>")
            }
            sb.append("</wpt>\n")
        }

        sb.append("<trk><name>")
            .append(name)
            .append("</name><trkseg>\n")

        points.forEach { point ->
            sb.append("<trkpt lat=\"")
                .append(point.lat)
                .append("\" lon=\"")
                .append(point.lng)
                .append("\"><time>")
                .append(timeFormat.format(Date(point.timestamp)))
                .append("</time></trkpt>\n")
        }

        sb.append("</trkseg></trk>\n</gpx>")
        return sb.toString()
    }

    private fun escapeXml(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun getDisplayName(uri: Uri): String? {
        val app = getApplication<Application>()
        val resolver = app.contentResolver
        var cursor: Cursor? = null
        return try {
            cursor = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                cursor.getString(0)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            cursor?.close()
        }
    }

    private data class ParsedPoint(
        val lat: Double,
        val lng: Double,
        val timestampMs: Long?
    )

    private data class ParsedWaypoint(
        val lat: Double,
        val lng: Double,
        val description: String
    )

    private data class ParsedGpx(
        val name: String?,
        val points: List<ParsedPoint>,
        val waypoints: List<ParsedWaypoint>
    )

    private fun parseGpx(input: InputStream): ParsedGpx? {
        val parser = Xml.newPullParser()
        parser.setInput(input, null)

        var eventType = parser.eventType
        var routeName: String? = null
        val points = mutableListOf<ParsedPoint>()
        val waypoints = mutableListOf<ParsedWaypoint>()

        var inMetadata = false
        var inTrk = false
        var inTrkpt = false
        var inWpt = false

        var currentLat = 0.0
        var currentLng = 0.0
        var currentTimestamp: Long? = null
        var currentWptName: String? = null
        var currentWptDesc: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "metadata" -> inMetadata = true
                        "trk" -> inTrk = true
                        "trkpt" -> {
                            inTrkpt = true
                            currentLat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull() ?: 0.0
                            currentLng = parser.getAttributeValue(null, "lon")?.toDoubleOrNull() ?: 0.0
                            currentTimestamp = null
                        }
                        "wpt" -> {
                            inWpt = true
                            currentLat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull() ?: 0.0
                            currentLng = parser.getAttributeValue(null, "lon")?.toDoubleOrNull() ?: 0.0
                            currentWptName = null
                            currentWptDesc = null
                        }
                        "name" -> {
                            val text = parser.nextText()
                            if (inWpt) {
                                currentWptName = text
                            } else if (routeName.isNullOrBlank() && (inMetadata || inTrk)) {
                                routeName = text
                            }
                        }
                        "desc" -> {
                            if (inWpt) {
                                currentWptDesc = parser.nextText()
                            }
                        }
                        "time" -> {
                            if (inTrkpt) {
                                currentTimestamp = parseTimestamp(parser.nextText())
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "metadata" -> inMetadata = false
                        "trk" -> inTrk = false
                        "trkpt" -> {
                            if (currentLat != 0.0 || currentLng != 0.0) {
                                points.add(
                                    ParsedPoint(
                                        lat = currentLat,
                                        lng = currentLng,
                                        timestampMs = currentTimestamp
                                    )
                                )
                            }
                            inTrkpt = false
                        }
                        "wpt" -> {
                            val desc = currentWptDesc
                                ?: currentWptName
                                ?: "Waypoint"
                            waypoints.add(
                                ParsedWaypoint(
                                    lat = currentLat,
                                    lng = currentLng,
                                    description = desc
                                )
                            )
                            inWpt = false
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return ParsedGpx(routeName, points, waypoints)
    }

    private fun parseTimestamp(raw: String): Long? {
        if (raw.isBlank()) return null
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        )
        for (pattern in patterns) {
            try {
                val formatter = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = formatter.parse(raw)
                if (date != null) return date.time
            } catch (_: Exception) {
                // Try next pattern.
            }
        }
        return null
    }

    private fun calculateDistance(points: List<PuntoRuta>): Double {
        var distance = 0.0
        for (i in 1 until points.size) {
            val prev = LatLng(points[i - 1].lat, points[i - 1].lng)
            val next = LatLng(points[i].lat, points[i].lng)
            distance += calculateDistance(prev, next)
        }
        return distance
    }

    private fun calculateDuration(points: List<PuntoRuta>): Long {
        val firstTime = points.minOfOrNull { it.timestamp } ?: return 0L
        val lastTime = points.maxOfOrNull { it.timestamp } ?: return 0L
        return max(0L, lastTime - firstTime)
    }

    private fun calculateDistance(p1: LatLng, p2: LatLng): Double {
        val r = 6371000.0
        val lat1 = Math.toRadians(p1.latitude)
        val lat2 = Math.toRadians(p2.latitude)
        val deltaLat = Math.toRadians(p2.latitude - p1.latitude)
        val deltaLng = Math.toRadians(p2.longitude - p1.longitude)

        val a = sin(deltaLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(deltaLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

    private fun calculateAverageSpeed(distanceMeters: Double, durationMs: Long): Double {
        if (durationMs <= 0L) return 0.0
        return (distanceMeters / 1000.0) / (durationMs / 3600000.0)
    }
}

data class RecordingState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedMs: Long = 0L,
    val distanceMeters: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val routeId: Long? = null
)

data class RouteSaveRequest(
    val routeId: Long,
    val suggestedName: String
)

data class RouteDisplayState(
    val route: Ruta? = null,
    val points: List<PuntoRuta> = emptyList(),
    val waypoints: List<Waypoint> = emptyList()
)

data class WaypointDetails(
    val description: String,
    val photoPath: String?
)