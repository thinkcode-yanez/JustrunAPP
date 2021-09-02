package com.example.justrun.config.repositories

import com.example.justrun.config.RunApp.Companion.db
import com.example.justrun.config.db.Run
import com.example.justrun.config.db.RunDao

class MainRepository(

) {

    suspend fun insertRun(run: Run)= db.getRunDao().insertRun(run)
    suspend fun getAllRunsByDate()=db.getRunDao().getAllRunsByDate()
    suspend fun deleteByID(id:Int) = db.getRunDao().deleteByID(id)
    suspend fun getById(id:Int)=db.getRunDao().selectByID(id)

    fun deleteRun(run: Run) = db.getRunDao().deleteRun(run)

    suspend fun getAll():List<Run> = db.getRunDao().getAll()
    fun getAllRunsByDistance()=db.getRunDao().getAllRunsByDistance()

    fun getAllRunsByTime()=db.getRunDao().getAllRunsByTime()

    fun getAllRunsBySpeed()=db.getRunDao().getAllRunsByAvrSpeed()
}