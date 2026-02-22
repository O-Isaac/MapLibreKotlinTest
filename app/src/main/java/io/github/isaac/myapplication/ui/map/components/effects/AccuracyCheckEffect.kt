package io.github.isaac.myapplication.ui.map.components.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import io.github.isaac.myapplication.ui.map.isHighAccuracyEnabled

/**
 * Al arrancar la pantalla, comprueba si el GPS tiene alta precisión activada.
 * Si no, notifica para mostrar el diálogo correspondiente.
 */
@Composable
fun AccuracyCheckEffect(onAccuracyDialogRequired: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (!isHighAccuracyEnabled(context)) {
            onAccuracyDialogRequired()
        }
    }
}