package com.example.justrun.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.example.justrun.R
import com.example.justrun.stuff.Constants.ACTION_PAUSE_SERVICE
import com.example.justrun.stuff.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.justrun.stuff.Constants.ACTION_STOP_SERVICE
import com.example.justrun.stuff.Constants.NOTIFICATION_CHANNEL_ID
import com.example.justrun.stuff.Constants.NOTIFICATION_ID
import com.example.justrun.stuff.Constants.NOTIFICATOIN_CHANNEL_NAME
import com.example.justrun.ui.MainMapTrackActivity

class TrackingService : Service() {

    var isFirstRun=true



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRun){
                        startForegroundService()
                        isFirstRun=false
                    }else{
                        Log.d("Service","Resuming service.....")
                    }

                }
                ACTION_PAUSE_SERVICE -> {
                    Log.d("Service", "Service paused")
                }
                ACTION_STOP_SERVICE -> {
                    Log.d("Service", "Service stoped")
                }
                else -> {
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        //super.onBind(intent)
        return null
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false) //para que siempre este visible si lo tocamos
            .setOngoing(true) //swipe away
            .setSmallIcon(R.drawable.ic_run_directions)
            .setContentTitle("JustRun by thinkCode")
            .setContentText("Keep Going......!!!")
            .setContentIntent(getMainActivity())


        startForeground(NOTIFICATION_ID, notificationBuilder.build())

    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getMainActivity() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainMapTrackActivity::class.java),
        FLAG_UPDATE_CURRENT
    )

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATOIN_CHANNEL_NAME,
            IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)


    }
}