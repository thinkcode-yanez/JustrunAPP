package com.example.justrun.config

import android.app.Application
import androidx.room.Room
import com.example.justrun.config.db.RunDataBase

class RunApp : Application() {

    companion object {

        lateinit var db: RunDataBase
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            this,
            RunDataBase::class.java,
            "tareas"
        ).build()
    }
}