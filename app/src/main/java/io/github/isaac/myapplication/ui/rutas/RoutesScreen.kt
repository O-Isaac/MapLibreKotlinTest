package io.github.isaac.myapplication.ui.rutas

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.isaac.myapplication.data.local.entities.Ruta
import io.github.isaac.myapplication.ui.map.MapViewModel
import io.github.isaac.myapplication.ui.rutas.components.EmptyRoutes
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesScreen(
    viewModel: MapViewModel,
    onNavigateBack: () -> Unit
) {
    val routes by viewModel.routes.collectAsState()
    val actions = rememberRouteActions(viewModel)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rutas guardadas") },
                actions = {
                    IconButton(onClick = { actions.onImportClick() }) {
                        Icon(Icons.Default.FileOpen, contentDescription = "Importar")
                    }
                }
            )
        }
    ) { padding ->
        CompositionLocalProvider(LocalRouteActions provides actions) {
            if (routes.isEmpty()) {
                EmptyRoutes(modifier = Modifier.padding(padding))
            } else {
                RouteList(
                    modifier = Modifier.padding(padding),
                    routes = routes,
                    onNavigateBack = onNavigateBack
                )
            }
        }

    }
}