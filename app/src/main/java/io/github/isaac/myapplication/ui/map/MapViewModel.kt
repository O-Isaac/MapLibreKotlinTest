package io.github.isaac.myapplication.ui.map

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import io.github.isaac.myapplication.BaseApplication
import io.github.isaac.myapplication.Constants
import io.github.isaac.myapplication.data.local.entities.MapSettingsEntity
import io.github.isaac.myapplication.data.local.entities.MarkerEntity
import io.github.isaac.myapplication.data.model.MarkerData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import java.util.UUID

class MapViewModel(application: Application) : AndroidViewModel(application) {

    // Acceso al DAO a través de la instancia de la base de datos en BaseApplication
    private val dao = (application as BaseApplication).database.mapDao()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // --- PERSISTENCIA: MARCADORES ---
    // Convertimos automáticamente los MarkerEntity (DB) a MarkerData (UI)
    val markers: StateFlow<List<MarkerData>> = dao.getAllMarkers()
        .map { entities ->
            entities.map { entity ->
                MarkerData(
                    id = entity.id,
                    name = entity.name,
                    position = LatLng(entity.latitude, entity.longitude)
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- ESTADO DE LA UI ---
    val mapState: MapState by lazy {
        MapState(application.applicationContext, this, ::onLongClick)
    }

    private val _pendingMarker = MutableStateFlow<LatLng?>(null)
    val pendingMarker = _pendingMarker.asStateFlow()

    private val _targetLocation = MutableStateFlow<LatLng?>(null)
    val targetLocation = _targetLocation.asStateFlow()

    // Para mantener la posición de la cámara tras rotaciones o cambios de pantalla
    var lastCameraPosition: LatLng = LatLng(40.4167, -3.7033)
    var lastZoom: Double = 10.0

    // --- OPERACIONES DE BASE DE DATOS (CRUD) ---

    fun confirmMarker(name: String) {
        _pendingMarker.value?.let { latLng ->
            viewModelScope.launch {
                dao.upsertMarker(
                    MarkerEntity(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        latitude = latLng.latitude,
                        longitude = latLng.longitude
                    )
                )
            }
        }
        _pendingMarker.value = null
    }

    fun deleteMarker(id: String) {
        viewModelScope.launch {
            dao.deleteMarker(id)
        }
    }

    fun updateMarkerName(id: String, newName: String) {
        // Aprovechamos el upsert de Room si la entidad tiene el mismo ID
        // O podemos usar un método específico en el DAO si lo prefieres
        viewModelScope.launch {
            // Nota: Aquí necesitarías recuperar la entidad completa o tener un método UPDATE en el DAO
            // Por simplicidad, si tu DAO tiene @Query UPDATE, úsalo:
            dao.updateMarkerName(id, newName)
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            dao.clearAllMarkers()
            dao.clearMapSettings()
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)

                // Actualizamos estados globales
                lastCameraPosition = currentLatLng
                lastZoom = 15.0

                // Navegamos visualmente
                navigateToMarker(currentLatLng)
            }
        }
    }

    // MapViewModel.kt

    fun centerCameraOnUser() {
        // Encendemos el componente de ubicación
        mapState.toggleLocationUpdates(true)

        // Accedemos al mapa a través de nuestra propiedad pública en MapState
        val locationComponent = mapState.map?.locationComponent
        val lastLoc = locationComponent?.lastKnownLocation

        if (lastLoc != null) {
            val currentLatLng = LatLng(lastLoc.latitude, lastLoc.longitude)
            navigateToMarker(currentLatLng)
        } else {
            // Si el mapa no sabe dónde está, usamos el GPS del sistema como respaldo
            fetchCurrentLocation()
        }
    }

    // --- EVENTOS DE INTERACCIÓN ---

    fun onLongClick(latLng: LatLng) {
        _pendingMarker.value = latLng
    }

    fun dismissDialog() {
        _pendingMarker.value = null
    }

    fun navigateToMarker(position: LatLng) {
        _targetLocation.value = position
    }

    fun navigationDone() {
        _targetLocation.value = null
    }
}