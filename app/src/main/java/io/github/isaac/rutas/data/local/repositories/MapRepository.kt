package io.github.isaac.rutas.data.local.repositories

import io.github.isaac.rutas.data.local.dao.MapDao
import io.github.isaac.rutas.data.local.entities.MarkerEntity
import kotlinx.coroutines.flow.Flow


class MapRepository(private val mapDao: MapDao) {
    // Obtenemos un grifo para obtener datos en tiempo real
    fun getAllMarkers(): Flow<List<MarkerEntity>> = mapDao.getAllMarkers()
    fun getMapStyle(): Flow<String?> = mapDao.getMapStyle()

    // Solo pedimos una vez
    suspend fun updateMarkerName(id: String, newName: String) = mapDao.updateMarkerName(id, newName)
    suspend fun upsertMarker(marker: MarkerEntity) = mapDao.upsertMarker(marker)
    suspend fun deleteMarker(id: String) = mapDao.deleteMarker(id)

    suspend fun clearMarkers() = mapDao.clearAllMarkers();
}