package io.github.isaac.rutas.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.isaac.rutas.data.model.MarkerData

@Entity(tableName = "markers")
data class MarkerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

// Extensiones para convertir
fun List<MarkerEntity>.toMarkerData(): List<MarkerData> {
    return this.map { MarkerData.from(it) }
}