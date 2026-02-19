package io.github.isaac.myapplication.ui.marcadores.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.isaac.myapplication.data.model.MarkerData
import io.github.isaac.myapplication.ui.map.MapViewModel

@Composable
fun MarkerList(
    modifier: Modifier = Modifier,
    markers: List<MarkerData>,
    viewModel: MapViewModel,
    onNavigateBack: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(markers, key = { it.id }) { marker ->
            MarkerCard(
                marker = marker,
                onDelete = { viewModel.deleteMarker(marker.id) },
                onEdit = { viewModel.updateMarkerName(marker.id, it) },
                onNavigate = {
                    viewModel.navigateToMarker(marker.position)
                    onNavigateBack()
                }
            )
        }
    }
}