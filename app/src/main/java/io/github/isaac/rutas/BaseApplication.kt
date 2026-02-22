package io.github.isaac.rutas

import android.app.Application
import androidx.room.Room
import io.github.isaac.rutas.data.local.AppDatabase

class BaseApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "mapdb")
            .fallbackToDestructiveMigration(true)
            .build()
    }
}