package com.example.justrun.config.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface RunDao {

    @Insert
    suspend fun insertRun(run:Run) //suspend

    @Delete
    fun deleteRun(run:Run)

    @Query("SELECT * FROM running_table")
    suspend fun getAll():List<Run>

    @Query("DELETE FROM running_table WHERE id = :id")
    suspend fun deleteByID(id:Int)

    @Query("SELECT * FROM running_table WHERE id = :id")
    suspend fun selectByID(id:Int):Run


    @Query("SELECT * FROM running_table ORDER BY timestamp DESC") // ORDEN DESCENDENTE
    suspend fun getAllRunsByDate(): List<Run>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC") // ORDEN DESCENDENTE
    fun getAllRunsByTime(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeedKmh DESC") // ORDEN DESCENDENTE
    fun getAllRunsByAvrSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceMeters DESC") // ORDEN DESCENDENTE
    fun getAllRunsByDistance(): LiveData<List<Run>>

    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalTime(): LiveData<Long>

    // @Query("SELECT SUM(caloriesBurned) FROM running_table")
    // fun getTotalCaloriesBurned():LiveData<Int>

    @Query("SELECT SUM(distanceMeters) FROM running_table")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT AVG(avgSpeedKmh) FROM running_table")
    fun getTotalAvgSpeed(): LiveData<Float>
}