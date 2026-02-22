package io.github.isaac.rutas.ui.rutas

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.isaac.rutas.Constants
import io.github.isaac.rutas.data.local.entities.PuntoRuta
import io.github.isaac.rutas.data.local.entities.Waypoint
import io.github.isaac.rutas.ui.rutas.utils.registerMapIcons
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.LineOptions
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.utils.ColorUtils


@Composable
fun RoutePreviewMap(
    puntos: List<PuntoRuta>,
    waypoints: List<Waypoint>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember { MapView(context) }
    var mapReady by remember { mutableStateOf(false) }

    // Ciclo de vida
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            mapView.visibility = android.view.View.GONE
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    Box {
        // El mapa ocupa todo
        AndroidView(
            factory = {
                mapView.setOnTouchListener { v, event ->
                    when (event.actionMasked) {
                        // Primer dedo: bloqueamos la intercepción del padre
                        // para que el LazyColumn no robe ningún gesto posterior
                        // (scroll, pinch, rotate…)
                        android.view.MotionEvent.ACTION_DOWN -> {
                            v.parent?.requestDisallowInterceptTouchEvent(true)
                        }
                        // Segundo dedo (inicio de pinch): reforzamos el bloqueo
                        // porque algunos ViewGroups lo resetean entre eventos
                        android.view.MotionEvent.ACTION_POINTER_DOWN -> {
                            v.parent?.requestDisallowInterceptTouchEvent(true)
                        }
                        // Al soltar todos los dedos devolvemos el control al padre
                        android.view.MotionEvent.ACTION_UP,
                        android.view.MotionEvent.ACTION_CANCEL -> {
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                    }

                    v.onTouchEvent(event)

                    true
                }

                mapView
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.getMapAsync { map ->
                    map.uiSettings.apply {
                        isLogoEnabled = false
                        isAttributionEnabled = false
                        isCompassEnabled = true
                        isScrollGesturesEnabled = true
                        isZoomGesturesEnabled = true
                        isRotateGesturesEnabled = true
                        isTiltGesturesEnabled = true
                        isDoubleTapGesturesEnabled = true
                    }

                    map.setStyle(Constants.STYLES_MAP) { style ->
                        mapReady = true

                        registerMapIcons(context, style)

                        val lineManager = LineManager(view, map, style)
                        val symbolManager = SymbolManager(view, map, style).apply {
                            iconAllowOverlap = true
                            textAllowOverlap = true
                        }
                        lineManager.deleteAll()
                        symbolManager.deleteAll()

                        val latLngs = puntos.map { LatLng(it.lat, it.lng) }

                        if (latLngs.isNotEmpty()) {
                            // Línea principal de la ruta
                            lineManager.create(
                                LineOptions()
                                    .withLatLngs(latLngs)
                                    .withLineColor(ColorUtils.colorToRgbaString(android.graphics.Color.parseColor("#3D7EF6")))
                                    .withLineWidth(5f)
                                    .withLineOpacity(0.9f)
                            )

                            // Marcador de inicio (verde)
                            symbolManager.create(
                                SymbolOptions()
                                    .withLatLng(latLngs.first())
                                    .withIconImage("route_start")
                                    .withIconSize(1f)
                                    .withIconColor(ColorUtils.colorToRgbaString(android.graphics.Color.parseColor("#22C55E")))
                                    .withTextSize(3f)
                                    .withTextOffset(arrayOf(0f, -2f))
                            )

                            // Marcador de fin (rojo)
                            if (latLngs.size > 1) {
                                symbolManager.create(
                                    SymbolOptions()
                                        .withLatLng(latLngs.last())
                                        .withIconImage("route_end")
                                        .withIconSize(1f)
                                        .withIconColor(ColorUtils.colorToRgbaString(android.graphics.Color.parseColor("#EF4444")))
                                        .withTextSize(3f)
                                        .withTextOffset(arrayOf(0f, -2f))
                                )

                                // Encuadre con padding generoso
                                val bounds = LatLngBounds.Builder().includes(latLngs).build()
                                map.easeCamera(
                                    CameraUpdateFactory.newLatLngBounds(bounds, 72),
                                    1200
                                )
                            } else {
                                map.easeCamera(
                                    CameraUpdateFactory.newLatLngZoom(latLngs.first(), 15.0),
                                    1000
                                )
                            }
                        }

                        // Waypoints de interés
                        waypoints.forEach { wp ->
                            symbolManager.create(
                                SymbolOptions()
                                    .withLatLng(LatLng(wp.lat, wp.lng))
                                    .withIconImage("default_marker")
                                    .withIconSize(.4f)
                                    .withIconColor(ColorUtils.colorToRgbaString(android.graphics.Color.parseColor("#F59E0B")))
                                    .withTextField(wp.descripcion)
                                    .withTextSize(3f)
                                    .withTextOffset(arrayOf(0f, -2f))
                                    .withTextHaloColor(ColorUtils.colorToRgbaString(android.graphics.Color.WHITE))
                                    .withTextHaloWidth(1.5f)
                            )
                        }
                    }
                }
            }
        )

        // Skeleton / loading indicator mientras el mapa carga
        AnimatedVisibility(
            visible = !mapReady,
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Columna de controles (bottom-end)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Botón centrar ruta
            if (puntos.isNotEmpty()) {
                SmallFloatingActionButton(
                    onClick = {
                        mapView.getMapAsync { map ->
                            val latLngs = puntos.map { LatLng(it.lat, it.lng) }
                            if (latLngs.size > 1) {
                                val bounds = LatLngBounds.Builder().includes(latLngs).build()
                                map.easeCamera(
                                    CameraUpdateFactory.newLatLngBounds(bounds, 72),
                                    800
                                )
                            } else if (latLngs.isNotEmpty()) {
                                map.easeCamera(
                                    CameraUpdateFactory.newLatLngZoom(latLngs.first(), 15.0),
                                    800
                                )
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MyLocation,
                        contentDescription = "Centrar ruta",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Badge de puntos
            if (puntos.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3D7EF6))
                        )
                        Text(
                            text = "${puntos.size} pts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}