package com.example.justrun.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity.apply
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.justrun.R
import com.example.justrun.services.TrackingService.Companion.isTracking
import com.example.justrun.services.TrackingService.Companion.pathPoints
import com.example.justrun.stuff.Constants.ACTION_PAUSE_SERVICE
import com.example.justrun.stuff.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.justrun.stuff.Constants.ACTION_STOP_SERVICE
import com.example.justrun.stuff.Constants.LOCATION_FAST_INTERAL
import com.example.justrun.stuff.Constants.LOCATION_UPDATE_INTERVAL
import com.example.justrun.stuff.Constants.NOTIFICATION_CHANNEL_ID
import com.example.justrun.stuff.Constants.NOTIFICATION_ID
import com.example.justrun.stuff.Constants.NOTIFICATOIN_CHANNEL_NAME
import com.example.justrun.stuff.TrackingUtility
import com.example.justrun.ui.MainMapTrackActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationResult.create
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {

    var isFirstRun = true
    var serviceKilled=false
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var runTimeSeconds = MutableLiveData<Long>()


    companion object {
        var runTimeMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        var flagData=false

    }

    private fun postInitValues() {
        isTracking.postValue(false)//No estamos corriendo al principio
        pathPoints.postValue(mutableListOf())//Insertamos una lista en blanco pues no hay polilynes aun
        runTimeSeconds.postValue(0L)
        runTimeMillis.postValue(0L)

    }

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        postInitValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)


        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })

    }




    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        startTimer() //para test por now
                        Log.d("Service", "Resuming service.....")
                    }

                }
                ACTION_PAUSE_SERVICE -> {
                    Log.d("Service", "Service paused")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {

                    Log.d("Service", "Service stoped")
                    killService()
                }
                else -> {
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun killService(){//METODO QUE RESETEA ALL DESPUES DE CANCELAR
        serviceKilled=true
        isFirstRun=true
        pauseService()
        postInitValues()
        stopForeground(true)
        stopSelf()
    }

    private var isTimerEnabled = false
    private var laptime=0L
    private var timeRun = 0L
    private var timeStart = 0L
    private var lastSecondTimestamp = 0L

    private fun startTimer(){
        addEmptyPolyline() //Al iniciar el servicio requerimos que nos inicialice con una empy polilyne
        isTracking.postValue(true)
        timeStart=System.currentTimeMillis()
        isTimerEnabled=true
        CoroutineScope(Dispatchers.Main).launch {

            while (isTracking.value!!) {

                //Diferencia entre el tiempo actual y el tiempo en que se inicio
                laptime = System.currentTimeMillis() - timeStart
                runTimeMillis.postValue(timeRun + laptime)
                if (runTimeMillis.value!! >= lastSecondTimestamp + 1000L) {
                    runTimeMillis.postValue(runTimeMillis.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(50L)
            }
            timeRun += laptime
        }
    }



    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled=false
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {

        if (isTracking) {
            if (TrackingUtility.hasLocationPersmissions(this)) {
                val request = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = LOCATION_FAST_INTERAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )

            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }


    }


    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            if (isTracking.value!!) {
                p0?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoints(location)
                        Log.d("Coor", "New Location: ${location.latitude},${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoints(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
            flagData = true //Al menos un dato ya fue insertado en el mapa
        }
    }

    private fun addEmptyPolyline() {
        pathPoints.value?.apply {
            add(mutableListOf())
            pathPoints.postValue(this)
        } ?: pathPoints.postValue(mutableListOf(mutableListOf()))
    }

    private fun startForegroundService() {

        startTimer()
        isTracking.postValue(true)//test

        val notificationManager = getSystemService(NOTIFICATION_SERVICE)
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