package io.github.isaac.rutas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.isaac.rutas.ui.Navigation
import io.github.isaac.rutas.ui.configuracion.SettingsScreen
import io.github.isaac.rutas.ui.map.MapLibreScreen
import io.github.isaac.rutas.ui.map.viewmodels.MapViewModel
import io.github.isaac.rutas.ui.marcadores.MarkersScreen
import io.github.isaac.rutas.ui.rutas.RouteDetailScreen
import io.github.isaac.rutas.ui.rutas.RoutesScreen
import io.github.isaac.rutas.ui.rutas.viewmodels.RouteDetailViewModel
import io.github.isaac.rutas.ui.theme.MyApplicationTheme
import org.maplibre.android.MapLibre

// ... otros imports

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MapLibre.getInstance(this)

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val viewModel: MapViewModel = viewModel()
                val routesRouteDetailViewModel: RouteDetailViewModel = viewModel()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { Navigation(navController) },
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "mapa",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("mapa") {
                            MapLibreScreen(viewModel = viewModel)
                        }

                        composable("rutas") {
                            RoutesScreen(
                                viewModel = viewModel,
                                onSelect = { rutaId -> navController.navigate("detalle_ruta/$rutaId") },
                                onNavigateBack = { navController.navigate("mapa") }
                            )
                        }

                        composable("marcadores") {
                            MarkersScreen(viewModel = viewModel) {
                                navController.navigate("mapa")
                            }
                        }

                        composable("settings") {
                            SettingsScreen(viewModel = viewModel) {
                                navController.popBackStack()
                            }
                        }

                        composable("detalle_ruta/{rutaId}") { backStackEntry ->
                            val rutaId = backStackEntry.arguments?.getString("rutaId")?.toLongOrNull() ?: 0L
                            RouteDetailScreen(
                                rutaId = rutaId,
                                viewModel = routesRouteDetailViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}