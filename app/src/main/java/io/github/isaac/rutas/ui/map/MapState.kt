package io.github.isaac.rutas.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import io.github.isaac.rutas.R
import io.github.isaac.rutas.BuildConfig
import io.github.isaac.rutas.data.model.MarkerData
import io.github.isaac.rutas.ui.map.viewmodels.MapViewModel
import io.github.isaac.rutas.ui.map.viewmodels.WaypointDetails
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Line
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.LineOptions
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

class MapState(
    private val context: Context,
    private val viewModel: MapViewModel,
    private val onLongClick: (LatLng) -> Unit,
    private val onWaypointClick: (WaypointDetails) -> Unit,
) {
    // Exponemos el mapView para el AndroidView de Compose
    val mapView = MapView(context).apply {
        getMapAsync(::onMapReady)
    }

    private var markerManager: SymbolManager? = null
    private var waypointManager: SymbolManager? = null
    private var routeMarkerManager: SymbolManager? = null
    private var lineManager: LineManager? = null
    private var routeLine: Line? = null
    private var routeStartSymbol: org.maplibre.android.plugins.annotation.Symbol? = null
    private var routeEndSymbol: org.maplibre.android.plugins.annotation.Symbol? = null

    private var pendingRoutePoints: List<LatLng> = emptyList()
    private var pendingWaypoints: List<WaypointMarker> = emptyList()
    private var pendingLocationEnable: Boolean = false
    private val waypointBySymbolId = mutableMapOf<Long, WaypointDetails>()
    private var waypointClickListenerSet: Boolean = false

    var map: MapLibreMap? = null
        private set

    // Callback cuando mapa carga
    private fun onMapReady(map: MapLibreMap) {
        this.map = map

        map.setStyle("https://api.maptiler.com/maps/streets/style.json?key=${BuildConfig.MAP_TILES}") { style ->
            onStyleReady(style, map)
        }
    }

    // Callback cuando el estilo carga
    @SuppressLint("MissingPermission")
    private fun onStyleReady(style: Style, map: MapLibreMap) {
        lineManager = LineManager(mapView, map, style)

        markerManager = SymbolManager(mapView, map, style).apply {
            iconAllowOverlap = true
            textAllowOverlap = true
        }

        waypointManager = SymbolManager(mapView, map, style).apply {
            iconAllowOverlap = true
            textAllowOverlap = true
            iconIgnorePlacement = true
            textIgnorePlacement = true
        }

        routeMarkerManager = SymbolManager(mapView, map, style).apply {
            iconAllowOverlap = true
            textAllowOverlap = true
            iconIgnorePlacement = true
            textIgnorePlacement = true
        }

        map.uiSettings.apply {
            isLogoEnabled = false
            isAttributionEnabled = false
        }

        loadMarkerFromAsset(
            R.drawable.default_marker,
            "default_marker",
            style
        )

        loadMarkerFromAsset(
            R.drawable.ic_waypoint,
            "waypoint_marker",
            style
        )

        loadMarkerFromAsset(
            R.drawable.ic_route_start,
            "route_start",
            style
        )

        loadMarkerFromAsset(
            R.drawable.ic_route_end,
            "route_end",
            style
        )

        map.addOnMapLongClickListener { latLng ->
            onLongClick(latLng)
            true
        }

        if (hasLocationPermission() || pendingLocationEnable) {
            enableLocationComponent(style)
            pendingLocationEnable = false
        }

        // Posición inicial
        map.cameraPosition = CameraPosition.Builder()
            .target(viewModel.lastCameraPosition)
            .zoom(viewModel.lastZoom)
            .build()

        map.addOnCameraIdleListener {
            map.cameraPosition.target?.let { position ->
                viewModel.lastCameraPosition = position
            }

            viewModel.lastZoom = map.cameraPosition.zoom
        }

        updateRouteLine(pendingRoutePoints)
        updateWaypoints(pendingWaypoints)
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun enableLocationComponent(style: Style) {
        val map = map ?: return
        val locationComponent = map.locationComponent

        val options = LocationComponentActivationOptions.builder(context, style).build()
        locationComponent.activateLocationComponent(options)

        // Habilitar visualización del punto azul
        locationComponent.isLocationComponentEnabled = true

        // Modo Seguimiento: La cámara sigue al usuario y muestra dirección
        locationComponent.cameraMode = CameraMode.TRACKING
        locationComponent.renderMode = RenderMode.COMPASS
    }


    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun toggleLocationUpdates(enable: Boolean) {
        if (!hasLocationPermission()) return
        map?.locationComponent?.let {
            if (it.isLocationComponentActivated) {
                it.isLocationComponentEnabled = enable
            }
        }
    }

    fun onLocationPermissionGranted() {
        if (hasLocationPermission()) {
            map?.getStyle { style ->
                enableLocationComponent(style)
            } ?: run {
                pendingLocationEnable = true
            }
        }
    }

    fun isLocationComponentActive(): Boolean {
        val component = map?.locationComponent ?: return false
        return component.isLocationComponentActivated && component.isLocationComponentEnabled
    }

    fun updateLocationComponent(location: Location) {
        val component = map?.locationComponent ?: return
        if (component.isLocationComponentActivated && component.isLocationComponentEnabled) {
            component.forceLocationUpdate(location)
        }
    }

    // Cargamos todos los marcadores en el mapa
    fun updateMarkers(markers: List<MarkerData>) {
        val manager = markerManager ?: return // Si no está listo, salimos

        manager.deleteAll()

        markers.forEach { marker ->
            manager.create(
                SymbolOptions()
                    .withLatLng(marker.position)
                    .withIconImage("default_marker")
                    .withTextField(marker.name)
                    .withTextSize(14f)
                    .withTextOffset(arrayOf(0f, 1.5f))
                    .withTextAnchor("top")
            )
        }
    }

    // Carga los assets de la aplicacion dentro de los estilos del mapa
    fun loadMarkerFromAsset(@DrawableRes resId: Int, assetId: String, style: Style) {
        val bitmap = decodeMarkerBitmap(resId) ?: return
        style.addImage(assetId, bitmap)
    }

    private fun decodeMarkerBitmap(@DrawableRes resId: Int): Bitmap? {
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        if (bitmap != null) {
            return bitmap
        }

        val drawable = AppCompatResources.getDrawable(context, resId) ?: return null
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }

        val density = context.resources.displayMetrics.density
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else (48 * density).toInt()
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else (48 * density).toInt()

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return output
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine
    }

    fun animateToLocation(position: LatLng) {
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0))
    }

    fun updateRouteLine(points: List<LatLng>) {
        pendingRoutePoints = points
        val manager = lineManager ?: return

        routeLine?.let { manager.delete(it) }
        routeLine = null

        if (points.size < 2) {
            updateRouteMarkers(points)
            return
        }

        routeLine = manager.create(
            LineOptions()
                .withLatLngs(points)
                .withLineColor("#1E88E5")
                .withLineWidth(4f)
        )

        updateRouteMarkers(points)
    }

    fun updateWaypoints(waypoints: List<WaypointMarker>) {
        pendingWaypoints = waypoints
        val manager = waypointManager ?: return

        manager.deleteAll()
        waypointBySymbolId.clear()

        waypoints.forEach { waypoint ->
            val symbol = manager.create(
                SymbolOptions()
                    .withLatLng(waypoint.position)
                    .withIconImage("waypoint_marker")
                    .withTextField(waypoint.description)
                    .withTextSize(12f)
                    .withTextOffset(arrayOf(0f, 1.5f))
                    .withTextAnchor("top")
            )

            waypointBySymbolId[symbol.id] = WaypointDetails(
                description = waypoint.description,
                photoPath = waypoint.photoPath
            )
        }

        if (!waypointClickListenerSet) {
            manager.addClickListener { symbol ->
                waypointBySymbolId[symbol.id]?.let { onWaypointClick(it) }
                true
            }
            waypointClickListenerSet = true
        }
    }

    private fun updateRouteMarkers(points: List<LatLng>) {
        val manager = routeMarkerManager ?: return

        routeStartSymbol?.let { manager.delete(it) }
        routeEndSymbol?.let { manager.delete(it) }
        routeStartSymbol = null
        routeEndSymbol = null

        if (points.isEmpty()) return

        val start = points.first()
        val end = points.last()

        routeStartSymbol = manager.create(
            SymbolOptions()
                .withLatLng(start)
                .withIconImage("route_start")
                .withIconSize(1.1f)
        )

        if (points.size > 1) {
            routeEndSymbol = manager.create(
                SymbolOptions()
                    .withLatLng(end)
                    .withIconImage("route_end")
                    .withIconSize(1.1f)
            )
        }
    }

}

data class WaypointMarker(
    val position: LatLng,
    val description: String,
    val photoPath: String?
)