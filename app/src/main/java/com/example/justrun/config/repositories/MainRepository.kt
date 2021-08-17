package com.example.justrun.config.repositories

import com.example.justrun.config.RunApp.Companion.db
import com.example.justrun.config.db.Run
import com.example.justrun.config.db.RunDao

class MainRepository(

) {

    fun insertRun(run: Run)= db.getRunDao().insertRun(run)

    fun deleteRun(run: Run) = db.getRunDao().deleteRun(run)

    suspend fun deleteByID(id:Int) = db.getRunDao().deleteByID(id)

    suspend fun getAll():List<Run> = db.getRunDao().getAll()

    fun getAllRunsByDate()=db.getRunDao().getAllRunsByDate()

    fun getAllRunsByDistance()=db.getRunDao().getAllRunsByDistance()

    fun getAllRunsByTime()=db.getRunDao().getAllRunsByTime()

    fun getAllRunsBySpeed()=db.getRunDao().getAllRunsByAvrSpeed()
}