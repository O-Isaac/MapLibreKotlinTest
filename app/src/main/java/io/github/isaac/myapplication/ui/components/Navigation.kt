package io.github.isaac.myapplication.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun Navigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "mapa",
            onClick = { navController.navigate("mapa") },
            label = { Text("Mapa") },
            icon = { Icon(Icons.Default.Place, "") }
        )
        NavigationBarItem(
            selected = currentRoute == "rutas",
            onClick = { navController.navigate("rutas") },
            label = { Text("Rutas") },
            icon = { Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null) }
        )
        NavigationBarItem(
            selected = currentRoute == "marcadores",
            onClick = { navController.navigate("marcadores") },
            label = { Text("Marcadores") },
            icon = { Icon(Icons.Default.Place, null) }
        )
        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = { navController.navigate("settings") },
            label = { Text("Ajustes") },
            icon = { Icon(Icons.Default.Settings, null) }
        )
    }
}
