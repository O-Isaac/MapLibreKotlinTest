package io.github.isaac.myapplication.ui.rutas

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.isaac.myapplication.data.local.entities.Ruta
import io.github.isaac.myapplication.ui.map.MapViewModel


// Contexto
// Parecido a context de react
val LocalRouteActions = staticCompositionLocalOf<RouteActionState> {
    error("No se proporcionó RouteActionsState")
}

// Implementaciones

class RouteActionState(
    private val viewModel: MapViewModel,
    private val context: Context,
    private val importLauncher: ManagedActivityResultLauncher<Array<String>, Uri?>
) {

    private fun createSendDialog(uri: Uri, route: Ruta, context: Context) {
        val intent =  Intent(Intent.ACTION_SEND).apply {
            type = "application/gpx+xml"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, route.nombre.ifBlank { "Ruta" })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Compartir GPX"))
    }

    fun createToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun onImportClick() {
        importLauncher.launch(
            arrayOf(
                "application/gpx+xml",
                "application/octet-stream",
                "text/xml",
                "*/*"
            )
        )
    }

    fun onShareRoute(route: Ruta) {
        viewModel.exportRouteGpx(
            routeId = route.id,
            onReady = { uri, _ -> createSendDialog(uri, route, context)},
            onError = { error -> createToast(error, context)}
        )
    }

    fun getViewMapModel(): MapViewModel {
        return viewModel
    }
}

// Remembers

@Composable
fun rememberRouteActions(viewModel: MapViewModel) : RouteActionState {
    val context = LocalContext.current
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {

            }

            viewModel.importRouteGpx(
                it,
                onSuccess = { name -> Toast.makeText(context, "Ruta: $name", Toast.LENGTH_SHORT).show() },
                onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
            )
        }
    }

    return remember(viewModel, context, importLauncher) {
        RouteActionState(viewModel, context, importLauncher)
    }
}