package io.github.isaac.rutas.ui.configuracion.utils

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings

/**
 * Utilidades de sistema relacionadas con ubicación.
 * Sin estado, sin Compose — puras funciones de plataforma.
 */

internal fun isHighAccuracyEnabled(context: Context): Boolean {
    val manager = context.getSystemService(LocationManager::class.java)
    val gpsEnabled = manager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    val networkEnabled = manager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    return gpsEnabled && networkEnabled
}

internal fun openLocationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}