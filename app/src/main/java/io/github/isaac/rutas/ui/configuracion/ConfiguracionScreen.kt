package io.github.isaac.rutas.ui.configuracion

import android.Manifest
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.github.isaac.rutas.ui.map.MapViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MapViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val recordingInterval by viewModel.recordingIntervalSec.collectAsState()
    var showAccuracyDialog by remember { mutableStateOf(false) }
    var isHighAccuracyEnabled by remember { mutableStateOf(isHighAccuracyEnabled(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                isHighAccuracyEnabled = isHighAccuracyEnabled(context)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Gestor de permisos de ubicación
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Si cualquier permiso de ubicación es aceptado
        if (permissions.values.any { it }) {
            viewModel.fetchCurrentLocation()
            Toast.makeText(context, "Ubicación base actualizada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

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
            item {
                SectionHeader("Localización")
            }
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
            item {
                SectionHeader("Grabación de ruta")
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Intervalo máximo",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        StatusChip("${recordingInterval} s")
                    }
                    Text(
                        "Mínimo 0.5 s, máximo ${recordingInterval} s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                    Slider(
                        value = recordingInterval.toFloat(),
                        onValueChange = { viewModel.updateRecordingInterval(it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 28,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            item {
                SectionHeader("Privacidad y datos")
            }
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

    if (showAccuracyDialog) {
        AlertDialog(
            onDismissRequest = { showAccuracyDialog = false },
            title = { Text("Activa la ubicación precisa") },
            text = { Text("Para el funcionamiento correcto, activa la ubicación precisa (GPS) en ajustes.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        openLocationSettings(context)
                        showAccuracyDialog = false
                    }
                ) {
                    Text("Activar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccuracyDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun openLocationSettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK
    )
    context.startActivity(intent)
}

private fun isHighAccuracyEnabled(context: android.content.Context): Boolean {
    val locationManager = context.getSystemService(LocationManager::class.java)
    val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    val isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    return isGpsEnabled && isNetworkEnabled
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    status: String? = null,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isDestructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (status != null) {
            StatusChip(status)
        }
    }
}

@Composable
private fun StatusChip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}