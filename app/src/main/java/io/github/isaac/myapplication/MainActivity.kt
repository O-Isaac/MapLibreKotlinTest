package io.github.isaac.myapplication

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
import io.github.isaac.myapplication.ui.map.components.Navigation
import io.github.isaac.myapplication.ui.configuracion.SettingsScreen
import io.github.isaac.myapplication.ui.map.MapLibreScreen
import io.github.isaac.myapplication.ui.map.MapViewModel
import io.github.isaac.myapplication.ui.marcadores.MarkersScreen
import io.github.isaac.myapplication.ui.rutas.RoutesScreen
import io.github.isaac.myapplication.ui.theme.MyApplicationTheme
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
                            RoutesScreen(viewModel = viewModel) {
                                navController.navigate("mapa")
                            }
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
                    }
                }
            }
        }
    }
}