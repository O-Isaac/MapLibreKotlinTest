package io.github.isaac.myapplication.ui.rutas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.isaac.myapplication.data.local.entities.Ruta
import io.github.isaac.myapplication.ui.rutas.components.RouteCard



@Composable
fun RouteList(modifier: Modifier = Modifier, routes: List<Ruta>, onNavigateBack: () -> Unit) {
    val actions = LocalRouteActions.current
    val viewModel = actions.getViewMapModel()
    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(routes, key = { it.id }) { route ->
                RouteCard(
                    route = route,
                    onSelect = {
                        viewModel.selectRoute(route.id)
                        onNavigateBack()
                    },
                    onShare = {
                        actions.onShareRoute(route)
                    },
                    onDelete = {
                    }
                )
            }
        }
    }

}