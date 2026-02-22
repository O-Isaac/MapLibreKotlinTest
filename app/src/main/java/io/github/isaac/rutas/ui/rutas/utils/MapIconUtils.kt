package io.github.isaac.rutas.ui.rutas.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import org.maplibre.android.maps.Style

/**
 * Carga un drawable como icono en el estilo de MapLibre.
 * Compartido entre MapState y RoutePreviewMap.
 */
fun loadMarkerFromAsset(
    context: Context,
    @DrawableRes resId: Int,
    assetId: String,
    style: Style
) {
    val bitmap = decodeMarkerBitmap(context, resId) ?: return
    if (style.getImage(assetId) == null) {
        style.addImage(assetId, bitmap)
    }
}

/**
 * Convierte un drawable vectorial o raster a Bitmap.
 */
fun decodeMarkerBitmap(context: Context, @DrawableRes resId: Int): Bitmap? {
    return try {
        ContextCompat.getDrawable(context, resId)?.toBitmap()
    } catch (e: Exception) {
        null
    }
}

/**
 * Registra todos los iconos estándar de la app en el estilo dado.
 * Llamar una sola vez al cargar el estilo, desde cualquier mapa.
 */
fun registerMapIcons(context: Context, style: Style) {
    val icons = listOf(
        io.github.isaac.rutas.R.drawable.ic_route_start  to "route_start",
        io.github.isaac.rutas.R.drawable.ic_route_end    to "route_end",
        io.github.isaac.rutas.R.drawable.ic_waypoint     to "waypoint_marker",
        io.github.isaac.rutas.R.drawable.default_marker  to "default_marker",
    )

    icons.forEach { (resId, id) ->
        loadMarkerFromAsset(context, resId, id, style)
    }
}