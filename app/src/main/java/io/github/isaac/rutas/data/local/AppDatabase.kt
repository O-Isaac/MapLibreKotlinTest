package io.github.isaac.rutas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.isaac.rutas.data.local.entities.MapSettingsEntity
import io.github.isaac.rutas.data.local.entities.MarkerEntity
import io.github.isaac.rutas.data.local.entities.PuntoRuta
import io.github.isaac.rutas.data.local.entities.Ruta
import io.github.isaac.rutas.data.local.entities.Waypoint

@Database(
    entities = [
        MarkerEntity::class,
        MapSettingsEntity::class,
        Ruta::class,
        PuntoRuta::class,
        Waypoint::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mapDao(): MapDao
}