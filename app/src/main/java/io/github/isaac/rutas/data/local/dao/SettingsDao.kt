package io.github.isaac.rutas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.isaac.rutas.data.local.entities.MapSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT recordingIntervalSec FROM map_settings WHERE id = 0")
    fun getRecordingInterval(): Flow<Int?>

    @Upsert
    suspend fun saveSettings(settings: MapSettingsEntity)

    @Query("DELETE FROM map_settings")
    suspend fun clearSettings()
}