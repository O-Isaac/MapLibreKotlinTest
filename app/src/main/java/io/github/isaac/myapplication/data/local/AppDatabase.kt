package io.github.isaac.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.isaac.myapplication.data.local.entities.MapSettingsEntity
import io.github.isaac.myapplication.data.local.entities.MarkerEntity

@Database(
    entities = [MarkerEntity::class, MapSettingsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mapDao(): MapDao
}