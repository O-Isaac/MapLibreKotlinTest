package io.github.isaac.myapplication.utils

import java.util.Locale

object StringUtils {
    fun formatDuration(durationMs: Long): String {
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

    fun formatDistance(distanceMeters: Double): String {
        return if (distanceMeters >= 1000) {
            "${"%.2f".format(distanceMeters / 1000.0)} km"
        } else {
            "${"%.0f".format(distanceMeters)} m"
        }
    }
}