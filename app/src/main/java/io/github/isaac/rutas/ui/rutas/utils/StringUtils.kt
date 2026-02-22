package io.github.isaac.rutas.ui.rutas.utils


object StringUtils {
    fun formatDistance(m: Double) =
        if (m >= 1000) "${"%.2f".format(m / 1000)} km" else "${m.toInt()} m"

    fun formatDuration(ms: Long): String {
        val sec = ms / 1000
        return String.format("%02d:%02d:%02d", sec / 3600, (sec % 3600) / 60, sec % 60)
    }
}