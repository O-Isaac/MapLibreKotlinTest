package io.github.isaac.myapplication.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.isaac.myapplication.data.local.entities.MapSettingsEntity
import io.github.isaac.myapplication.data.local.entities.MarkerEntity
import io.github.isaac.myapplication.data.local.entities.PuntoRuta
import io.github.isaac.myapplication.data.local.entities.Ruta
import io.github.isaac.myapplication.data.local.entities.Waypoint
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

    @Query("SELECT recordingIntervalSec FROM map_settings WHERE id = 0")
    fun getRecordingInterval(): Flow<Int?>

    @Upsert
    suspend fun saveSettings(settings: MapSettingsEntity)

    @Query("DELETE FROM markers")
    suspend fun clearAllMarkers()

    @Query("DELETE FROM map_settings")
    suspend fun clearMapSettings()

    @Query("DELETE FROM ruta")
    suspend fun clearRutas()

    @Query("SELECT * FROM ruta ORDER BY id DESC")
    fun getAllRutas(): Flow<List<Ruta>>

    @Upsert
    suspend fun upsertRuta(ruta: Ruta)

    @Query("DELETE FROM ruta WHERE id = :rutaId")
    suspend fun deleteRuta(rutaId: Long)

    @Upsert
    suspend fun upsertPuntoRuta(punto: PuntoRuta)

    @Upsert
    suspend fun upsertWaypoint(waypoint: Waypoint)

    @Query("SELECT * FROM punto_ruta WHERE rutaId = :rutaId ORDER BY timestamp ASC")
    suspend fun getPuntosByRuta(rutaId: Long): List<PuntoRuta>

    @Query("SELECT * FROM waypoint WHERE rutaId = :rutaId ORDER BY id ASC")
    suspend fun getWaypointsByRuta(rutaId: Long): List<Waypoint>

    @Query("SELECT * FROM ruta WHERE id = :rutaId")
    suspend fun getRutaById(rutaId: Long): Ruta?
}