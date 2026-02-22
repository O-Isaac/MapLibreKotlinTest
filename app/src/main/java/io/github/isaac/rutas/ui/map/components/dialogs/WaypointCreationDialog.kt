package io.github.isaac.rutas.ui.map.components.dialogs

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel
import java.io.File
import java.util.UUID

/**
 * Diálogo para crear un waypoint durante la grabación de una ruta.
 * Gestiona la toma de foto y la confirmación/cancelación del waypoint.
 *
 * @param pendingPhotoPath ruta local de la foto tomada, si la hay
 * @param onPhotoTaken callback con la ruta del archivo al tomar la foto
 * @param onPhotoClear callback para limpiar la foto (al confirmar o cancelar)
 * @param onPhotoLaunch callback para abrir la cámara con la Uri del archivo destino
 */
@Composable
fun WaypointCreationDialog(
    pendingPhotoPath: String?,
    onPhotoTaken: (path: String) -> Unit,
    onPhotoClear: () -> Unit,
    onPhotoLaunch: (uri: Uri) -> Unit
) {
    val viewModel = LocalMapViewModel.current
    val context = LocalContext.current
    val pendingWaypointLocation by viewModel.pendingWaypointLocation.collectAsState()

    pendingWaypointLocation ?: return

    WaypointDialog(
        description = "",
        photoPath = pendingPhotoPath,
        onTakePhoto = {
            val (uri, path) = createWaypointPhotoUri(context)

            onPhotoTaken(path)
            onPhotoLaunch(uri)
        },
        onConfirm = { description ->
            viewModel.confirmWaypoint(description, pendingPhotoPath)
            onPhotoClear()
        },
        onDismiss = {
            onPhotoClear()
            viewModel.dismissWaypointDialog()
        }
    )
}

private fun createWaypointPhotoUri(context: Context): Pair<Uri, String> {
    val imagesDir = File(context.filesDir, "waypoints").apply { mkdirs() }
    val file = File(imagesDir, "${UUID.randomUUID()}.jpg")
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    return uri to file.absolutePath
}