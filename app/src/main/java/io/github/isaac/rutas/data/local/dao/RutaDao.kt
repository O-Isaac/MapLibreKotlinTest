package io.github.isaac.rutas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.isaac.rutas.data.local.entities.PuntoRuta
import io.github.isaac.rutas.data.local.entities.Ruta
import io.github.isaac.rutas.data.local.entities.Waypoint
import kotlinx.coroutines.flow.Flow

@Dao
interface RutaDao {
    @Query("DELETE FROM ruta")
    suspend fun clearRutas()

    @Upsert
    suspend fun upsertRuta(ruta: Ruta)

    @Query("DELETE FROM ruta WHERE id = :rutaId")
    suspend fun deleteRuta(rutaId: Long)

    @Upsert
    suspend fun upsertPuntoRuta(punto: PuntoRuta)

    @Upsert
    suspend fun upsertWaypoint(waypoint: Waypoint)

    @Query("SELECT * FROM ruta ORDER BY id DESC")
    fun getAllRutas(): Flow<List<Ruta>>

    @Query("SELECT * FROM punto_ruta WHERE rutaId = :rutaId ORDER BY timestamp ASC")
    fun getPuntosByRuta(rutaId: Long): Flow<List<PuntoRuta>>

    @Query("SELECT * FROM waypoint WHERE rutaId = :rutaId ORDER BY id ASC")
    fun getWaypointsByRuta(rutaId: Long): Flow<List<Waypoint>>

    @Query("SELECT * FROM punto_ruta WHERE rutaId = :rutaId ORDER BY timestamp ASC")
    suspend fun getPuntosByRutaOnce(rutaId: Long): List<PuntoRuta>

    @Query("SELECT * FROM waypoint WHERE rutaId = :rutaId ORDER BY id ASC")
    suspend fun getWaypointsByRutaOnce(rutaId: Long): List<Waypoint>

    @Query("SELECT * FROM ruta WHERE id = :rutaId")
    suspend fun getRutaById(rutaId: Long): Ruta?
}