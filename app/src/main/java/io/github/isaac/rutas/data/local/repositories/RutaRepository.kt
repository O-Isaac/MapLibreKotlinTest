package io.github.isaac.rutas.data.local.repositories

import io.github.isaac.rutas.data.local.dao.RutaDao
import io.github.isaac.rutas.data.local.entities.PuntoRuta
import io.github.isaac.rutas.data.local.entities.Ruta
import io.github.isaac.rutas.data.local.entities.Waypoint
import kotlinx.coroutines.flow.Flow

class RutaRepository(private val rutaDao: RutaDao) {
    // Flujos de datos en tiempo real para las listas
    fun getAllRutas(): Flow<List<Ruta>> =
        rutaDao.getAllRutas()
    fun getPuntosRuta(rutaId: Long): Flow<List<PuntoRuta>> =
        rutaDao.getPuntosByRuta(rutaId)
    fun getWaypointsRuta(rutaId: Long): Flow<List<Waypoint>> =
        rutaDao.getWaypointsByRuta(rutaId)

    suspend fun getRunta(rutaId: Long): Ruta? = rutaDao.getRutaById(rutaId);

    suspend fun getWaypointsRutaOnce(rutaId: Long): List<Waypoint> =
        rutaDao.getWaypointsByRutaOnce(rutaId)

    suspend fun getPuntosRutaOnce(rutaId: Long): List<PuntoRuta> =
        rutaDao.getPuntosByRutaOnce(rutaId)

    // Operaciones de escritura y eliminación
    suspend fun upsertRuta(ruta: Ruta) = rutaDao.upsertRuta(ruta)
    suspend fun deleteRuta(rutaId: Long) = rutaDao.deleteRuta(rutaId)
    suspend fun upsertPunto(punto: PuntoRuta) = rutaDao.upsertPuntoRuta(punto)
    suspend fun upsertWaypoint(waypoint: Waypoint) = rutaDao.upsertWaypoint(waypoint)
    suspend fun clearRutas() = rutaDao.clearRutas()

}