package io.github.isaac.rutas.data.local.repositories

import io.github.isaac.rutas.data.local.dao.RutaDao
import io.github.isaac.rutas.data.local.dao.SettingsDao
import io.github.isaac.rutas.data.local.entities.MapSettingsEntity

/**
 * Podria haber creado un sharedPrefs pero he usado como escusa para
 * crear mas tablas, para testear room
 */
class SettingsRepository(private val settingsDao: SettingsDao) {
    fun getRecordingInterval() = settingsDao.getRecordingInterval()
    suspend fun saveSettings(settings: MapSettingsEntity) = settingsDao.saveSettings(settings)
    suspend fun clearSettings() = settingsDao.clearSettings()
}