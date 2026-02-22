package io.github.isaac.rutas.ui.rutas

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.isaac.rutas.data.local.entities.PuntoRuta
import io.github.isaac.rutas.data.local.entities.Ruta
import io.github.isaac.rutas.data.local.entities.Waypoint
import io.github.isaac.rutas.ui.rutas.components.RouteMapHero
import io.github.isaac.rutas.ui.rutas.components.RouteStatsSection
import io.github.isaac.rutas.ui.rutas.components.WaypointItem
import io.github.isaac.rutas.ui.rutas.components.WaypointsSectionHeader
import io.github.isaac.rutas.ui.rutas.viewmodels.RouteDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(
    rutaId: Long,
    viewModel: RouteDetailViewModel,
    onBack: () -> Unit,
) {
    val puntos by viewModel.getPuntos(rutaId).collectAsState(initial = emptyList())
    val waypoints by viewModel.getWaypoints(rutaId).collectAsState(initial = emptyList())
    val todasLasRutas by viewModel.getRutas().collectAsState(initial = emptyList())
    val ruta = todasLasRutas.find { it.id == rutaId }

    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 10 } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = isScrolled,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Text(
                            text = ruta?.nombre ?: "Resumen de Ruta",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isScrolled)
                        MaterialTheme.colorScheme.surface
                    else
                        Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (ruta == null) {
            RouteLoadingState(padding)
        } else {
            // El mapa NUNCA puede estar dentro de un LazyColumn/ScrollView:
            // el sistema resetea requestDisallowInterceptTouchEvent en cada scroll,
            // rompiendo la cadena de eventos multi-dedo del MapView.
            // Solución: mapa fijo arriba, el resto en Column con verticalScroll.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
            ) {
                // ── MAPA FIJO (fuera del scroll) ─────────────────────────
                RouteMapHero(
                    ruta = ruta,
                    puntos = puntos,
                    waypoints = waypoints,
                    topPadding = padding.calculateTopPadding()
                )

                // ── CONTENIDO SCROLLABLE ─────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    RouteStatsSection(ruta = ruta, waypointCount = waypoints.size)

                    if (waypoints.isNotEmpty()) {
                        WaypointsSectionHeader(count = waypoints.size)

                        waypoints.forEach { waypoint ->
                            WaypointItem(waypoint)
                        }
                    }

                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

// ─── Loading ────────────────────────────────────────────────────────────────

@Composable
private fun RouteLoadingState(padding: PaddingValues) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
            Text(
                "Cargando ruta...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}