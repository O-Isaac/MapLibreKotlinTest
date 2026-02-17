package io.github.isaac.myapplication.data.model

import org.maplibre.android.geometry.LatLng

data class MarkerData(
    val id: String,
    val name: String,
    val position: LatLng
)
