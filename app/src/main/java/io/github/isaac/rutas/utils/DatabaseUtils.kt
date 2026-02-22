package io.github.isaac.rutas.utils

fun dbValueToMs(dbValue: Int): Long =
    if (dbValue == 0) 500L else dbValue * 1000L
