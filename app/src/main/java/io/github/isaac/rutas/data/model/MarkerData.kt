package io.github.isaac.rutas.data.model

import io.github.isaac.rutas.data.local.entities.MarkerEntity
import org.maplibre.android.geometry.LatLng

data class MarkerData(
    val id: String,
    val name: String,
    val position: LatLng
) {
    companion object {
        fun from(entity: MarkerEntity): MarkerData {
            return MarkerData(
                id = entity.id,
                name = entity.name,
                position = LatLng(entity.latitude, entity.longitude)
            )
        }
    }
}

