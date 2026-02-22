package io.github.isaac.rutas.ui.rutas

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.isaac.rutas.data.local.entities.Ruta
import io.github.isaac.rutas.ui.map.locals.LocalMapViewModel
import io.github.isaac.rutas.ui.rutas.components.RouteCard

@Composable
fun RouteList(
    modifier: Modifier = Modifier,
    routes: List<Ruta>,
    onSelect: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val actions = LocalRouteActions.current
    val viewMode = actions.getViewMapModel()
    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {
        if (routes.isEmpty()) {
            RouteEmptyState()
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cabecera con conteo
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Mis rutas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${routes.size}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                itemsIndexed(
                    items = routes,
                    key = { _, route -> route.id }
                ) { index, route ->
                    // Entrada escalonada por índice
                    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
                    AnimatedVisibility(
                        visibleState = visible,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = (index * 50).coerceAtMost(300)
                            )
                        ) + slideInVertically(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = (index * 50).coerceAtMost(300)
                            ),
                            initialOffsetY = { it / 4 }
                        )
                    ) {
                        RouteCard(
                            route = route,
                            onSelect = { onSelect(route.id) },
                            onShare = { actions.onShareRoute(route) },
                            onDelete = { viewMode.deleteRuta(it) }
                        )
                    }
                }
            }
        }
    }
}

// ── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun RouteEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Outlined.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Sin rutas aún",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Sal a explorar y tus rutas\naparecerán aquí.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}