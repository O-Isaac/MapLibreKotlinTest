package io.github.isaac.rutas.ui.map.orchestrators

import android.net.Uri
import androidx.compose.runtime.Composable
import io.github.isaac.rutas.ui.map.components.dialogs.AccuracyDialog
import io.github.isaac.rutas.ui.map.components.dialogs.MarkerDialog
import io.github.isaac.rutas.ui.map.components.dialogs.RouteNameDialog
import io.github.isaac.rutas.ui.map.components.dialogs.WaypointCreationDialog
import io.github.isaac.rutas.ui.map.components.dialogs.WaypointInfoDialog
import io.github.isaac.rutas.ui.map.components.dialogs.WaypointListDialog

/**
 * Orquesta todos los diálogos de la pantalla del mapa.
 * Cada diálogo decide por sí mismo si se muestra leyendo el ViewModel via locals.
 *
 * @param showAccuracyDialog     flag local para mostrar el diálogo de precisión GPS
 * @param showWaypointsDialog    flag local para mostrar la lista de waypoints
 * @param pendingPhotoPath       ruta de la foto pendiente de confirmar en un waypoint
 * @param onAccuracyDialogDismiss callback para cerrar el diálogo de precisión
 * @param onWaypointsDialogDismiss callback para cerrar la lista de waypoints
 * @param onPhotoTaken           callback cuando la cámara devuelve una foto con su path
 * @param onPhotoClear           callback para limpiar la foto pendiente
 * @param onPhotoLaunch          callback para lanzar la cámara con la Uri de destino
 */
@Composable
fun MapDialogsOrchestrator(
    showAccuracyDialog: Boolean,
    showWaypointsDialog: Boolean,
    pendingPhotoPath: String?,
    onAccuracyDialogDismiss: () -> Unit,
    onWaypointsDialogDismiss: () -> Unit,
    onPhotoTaken: (path: String) -> Unit,
    onPhotoClear: () -> Unit,
    onPhotoLaunch: (uri: Uri) -> Unit
) {
    MarkerDialog()

    WaypointCreationDialog(
        pendingPhotoPath = pendingPhotoPath,
        onPhotoTaken = onPhotoTaken,
        onPhotoClear = onPhotoClear,
        onPhotoLaunch = onPhotoLaunch
    )

    RouteNameDialog()

    WaypointInfoDialog()

    if (showWaypointsDialog) {
        WaypointListDialog(onDismiss = onWaypointsDialogDismiss)
    }

    if (showAccuracyDialog) {
        AccuracyDialog(onDismiss = onAccuracyDialogDismiss)
    }
}
