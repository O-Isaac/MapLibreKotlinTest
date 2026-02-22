package io.github.isaac.rutas.ui.rutas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.isaac.rutas.data.local.entities.PuntoRuta
import io.github.isaac.rutas.data.local.entities.Ruta
import io.github.isaac.rutas.data.local.entities.Waypoint
import io.github.isaac.rutas.ui.rutas.RoutePreviewMap

@Composable
fun RouteMapHero(
    ruta: Ruta,
    puntos: List<PuntoRuta>,
    waypoints: List<Waypoint>,
    topPadding: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp + topPadding)
    ) {
        RoutePreviewMap(
            puntos = puntos,
            waypoints = waypoints,
            modifier = Modifier.fillMaxSize()
        )

        // Degradado inferior para legibilidad del texto
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.97f)
                        )
                    )
                )
        )

        // Badge + nombre de la ruta sobre el mapa
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 24.dp, end = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = "RUTA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    letterSpacing = 1.5.sp
                )
            }
            Text(
                text = ruta.nombre,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}