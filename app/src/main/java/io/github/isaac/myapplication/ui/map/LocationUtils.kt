package io.github.isaac.myapplication.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Utilidades de sistema relacionadas con ubicación y permisos.
 * Sin estado, sin Compose — puras funciones de plataforma.
 */

internal fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

internal fun isHighAccuracyEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val networkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    val locationMode = try {
        Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
    } catch (_: Settings.SettingNotFoundException) {
        Settings.Secure.LOCATION_MODE_OFF
    }
    return locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY ||
            (gpsEnabled && networkEnabled)
}

internal fun openLocationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}