package io.github.isaac.rutas.ui.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.isaac.rutas.ui.map.components.fabs.AddWaypointFab
import io.github.isaac.rutas.ui.map.components.fabs.CenterLocationFab
import io.github.isaac.rutas.ui.map.components.fabs.ShowWaypointsFab

/**
 * Orquesta todos los overlays flotantes sobre el mapa.
 * Recibe [BoxScope] para que los hijos puedan usar [Modifier.align].
 */
@Composable
fun BoxScope.MapOverlays(
    hasLocationPermission: Boolean,
    onShowWaypointsDialog: () -> Unit,
    onStartRecordingRequest: () -> Unit
) {
    if (!hasLocationPermission) return

    RecordingStatsCard(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(12.dp)
    )

    RouteSummaryCard(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 84.dp, start = 12.dp, end = 12.dp)
    )

    Column(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End
    ) {
        ShowWaypointsFab(onShowWaypointsDialog)
        AddWaypointFab()
        RecordingControls(onStartRequest = onStartRecordingRequest)
        CenterLocationFab(hasLocationPermission)
    }
}