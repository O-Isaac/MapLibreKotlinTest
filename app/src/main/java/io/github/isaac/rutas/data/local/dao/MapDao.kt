package io.github.isaac.rutas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.isaac.rutas.data.local.entities.MapSettingsEntity
import io.github.isaac.rutas.data.local.entities.MarkerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MapDao {
    @Query("SELECT * FROM markers")
    fun getAllMarkers(): Flow<List<MarkerEntity>>

    @Upsert
    suspend fun upsertMarker(marker: MarkerEntity)

    @Query("DELETE FROM markers WHERE id = :id")
    suspend fun deleteMarker(id: String)

    // Función faltante para actualizar solo el nombre
    @Query("UPDATE markers SET name = :newName WHERE id = :id")
    suspend fun updateMarkerName(id: String, newName: String)

    @Query("SELECT selectedStyleUrl FROM map_settings WHERE id = 0")
    fun getMapStyle(): Flow<String?>

    @Query("DELETE FROM markers")
    suspend fun clearAllMarkers()
}