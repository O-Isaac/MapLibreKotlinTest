package io.github.isaac.myapplication.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "punto_ruta",
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
data class PuntoRuta(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val rutaId: Long,
    val lat: Double,
    val lng: Double,
    val timestamp: Long = System.currentTimeMillis()
)