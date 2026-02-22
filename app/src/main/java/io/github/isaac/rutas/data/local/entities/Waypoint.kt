package io.github.isaac.rutas.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "waypoint",
    foreignKeys = [
        ForeignKey(
            entity = Ruta::class,
            parentColumns = ["id"],
            childColumns = ["rutaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("rutaId")]
)
data class Waypoint(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val rutaId: Long,
    val lat: Double,
    val lng: Double,
    val descripcion: String,
    val fotoPath: String? = null // solo aquí
)