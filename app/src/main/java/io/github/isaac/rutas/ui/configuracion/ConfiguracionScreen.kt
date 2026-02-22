package io.github.isaac.rutas.ui.configuracion

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.isaac.rutas.ui.configuracion.components.RecordingIntervalSlider
import io.github.isaac.rutas.ui.configuracion.components.SectionHeader
import io.github.isaac.rutas.ui.configuracion.components.SettingItem
import io.github.isaac.rutas.ui.configuracion.components.dialogs.AccuracyDialog
import io.github.isaac.rutas.ui.configuracion.components.effects.HighAccuracyCheckEffect
import io.github.isaac.rutas.ui.configuracion.utils.isHighAccuracyEnabled
import io.github.isaac.rutas.ui.map.viewmodels.MapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MapViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showAccuracyDialog by remember { mutableStateOf(false) }
    var isHighAccuracyEnabled by remember { mutableStateOf(isHighAccuracyEnabled(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.fetchCurrentLocation()
            Toast.makeText(context, "Ubicación base actualizada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    HighAccuracyCheckEffect(onAccuracyChanged = { isHighAccuracyEnabled = it })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item { SectionHeader("Localización") }

            item {
                SettingItem(
                    icon = Icons.Default.MyLocation,
                    title = "Ubicación por defecto",
                    subtitle = "Usa tu posición actual como centro del mapa",
                    modifier = Modifier.clickable {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.MyLocation,
                    title = "Ubicación precisa",
                    subtitle = "Activa la ubicación precisa para mejor exactitud",
                    status = if (isHighAccuracyEnabled) "Activada" else "Desactivada",
                    modifier = Modifier.clickable { showAccuracyDialog = true }
                )
            }

            item { SectionHeader("Grabación de ruta") }

            item { RecordingIntervalSlider(viewModel) }

            item { SectionHeader("Privacidad y datos") }

            item {
                SettingItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Borrar base de datos",
                    subtitle = "Elimina todos los marcadores y ajustes",
                    isDestructive = true,
                    modifier = Modifier.clickable {
                        viewModel.clearDatabase()
                        Toast.makeText(context, "Datos eliminados", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    AccuracyDialog(
        visible = showAccuracyDialog,
        onDismiss = { showAccuracyDialog = false }
    )
}