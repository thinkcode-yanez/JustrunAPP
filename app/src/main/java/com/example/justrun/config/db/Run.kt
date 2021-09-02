package com.example.justrun.config.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity (tableName = "running_table")
data class Run(
    var img: Bitmap? =null, //Se crea una class Coverter para manejar bitmap en Room. Check Converters
    var timestamp: Long=0L,
    var avgSpeedKmh:Float=0F,
    var distanceMeters:Int=0,
    var timeInMillis:Long=0L,
    //var caloriesBurned: Int=0,


){
    @PrimaryKey(autoGenerate = true)
    var id:Int?=null

}
