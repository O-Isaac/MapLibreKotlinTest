package io.github.isaac.rutas.ui.rutas.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.github.isaac.rutas.BaseApplication
import io.github.isaac.rutas.data.local.dao.RutaDao
import io.github.isaac.rutas.data.local.repositories.RutaRepository

class RouteDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RutaRepository = (application as BaseApplication).let {
        RutaRepository(it.database.rutaDao())
    }

    fun getPuntos(rutaId: Long) = repository.getPuntosRuta(rutaId)
    fun getWaypoints(rutaId: Long) = repository.getWaypointsRuta(rutaId)
    fun getRutas() = repository.getAllRutas()
}