package io.github.isaac.myapplication.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import io.github.isaac.myapplication.BuildConfig
import io.github.isaac.myapplication.R
import io.github.isaac.myapplication.data.model.MarkerData
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

class MapState(
    private val context: Context,
    private val viewModel: MapViewModel,
    private val onLongClick: (LatLng) -> Unit,
) {
    // Exponemos el mapView para el AndroidView de Compose
    val mapView = MapView(context).apply {
        getMapAsync(::onMapReady)
    }

    private var symbolManager: SymbolManager? = null

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
    private fun onStyleReady(style: Style, map: MapLibreMap) {
        symbolManager = SymbolManager(mapView, map, style).apply {
            iconAllowOverlap = true
            textAllowOverlap = true
        }

        loadMarkerFromAsset(
            R.drawable.default_marker,
            "default_marker",
            style
        )

        map.addOnMapLongClickListener { latLng ->
            onLongClick(latLng)
            true
        }

        enableLocationComponent(style)

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
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
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


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun toggleLocationUpdates(enable: Boolean) {
        map?.locationComponent?.let {
            if (it.isLocationComponentActivated) {
                it.isLocationComponentEnabled = enable
            }
        }
    }

    // Cargamos todos los marcadores en el mapa
    fun updateMarkers(markers: List<MarkerData>) {
        val manager = symbolManager ?: return // Si no está listo, salimos

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
    private fun loadMarkerFromAsset(@DrawableRes resId: Int, assetId: String, style: Style) {
        val bitmap = BitmapFactory.decodeResource(
            context.resources,
            resId
        )

        style.addImage(assetId, bitmap)
    }

    fun animateToLocation(position: LatLng) {
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0))
    }

}