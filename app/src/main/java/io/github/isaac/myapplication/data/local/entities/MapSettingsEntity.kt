package io.github.isaac.myapplication.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_settings")
data class MapSettingsEntity(
    @PrimaryKey val id: Int = 0, // Solo una fila para la configuración global
    val selectedStyleUrl: String,
    val recordingIntervalSec: Int = 10
)

