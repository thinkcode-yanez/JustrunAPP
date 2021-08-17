package com.example.justrun.ui

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.justrun.R
import com.example.justrun.config.RunApp.Companion.db
import com.example.justrun.config.db.Run
import com.example.justrun.databinding.ActivityMainBinding
import com.example.justrun.services.Polyline
import com.example.justrun.services.Polylines
import com.example.justrun.services.TrackingService
import com.example.justrun.services.TrackingService.Companion.isTracking
import com.example.justrun.services.TrackingService.Companion.pathPoints
import com.example.justrun.stuff.Constants.ACTION_PAUSE_SERVICE
import com.example.justrun.stuff.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.justrun.stuff.Constants.MAP_ZOOM
import com.example.justrun.stuff.Constants.POLYLINE_COLOR
import com.example.justrun.stuff.Constants.POLYLINE_WIDTH
import com.example.justrun.stuff.Constants.REQUEST_CODE_LOCATION
import com.example.justrun.stuff.TrackingUtility
import com.example.justrun.viewmodels.MainViewModel
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MainMapTrackActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    ////MapsVariables
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var map: GoogleMap? = null

    private var currenTimeMillis=0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermissions()

        //Maps Stuff
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
            addAllPolyLines()
        }
        subscribeObservers()




        binding.btnToggleRun.setOnClickListener {
            startTheRun()
        }
    }

    private fun sendComandService(action: String) =
        Intent(this, TrackingService::class.java).also {
            it.action = action
            this.startService(it)
        }

    private fun subscribeObservers(){

        val observer = Observer<Boolean> { data ->
            updateTracking(data)
        }
        TrackingService.isTracking.observeForever(observer)

        val observer2 = Observer<Polylines> {
            pathPoints=it
            addTheLastPolyline()
            getUpdateCameraLocation()
        }
        TrackingService.pathPoints.observeForever(observer2)

        val observer3 = Observer<Long> { data ->
            currenTimeMillis=data
            val formatedTime= TrackingUtility.setFormatOfStopWathch(currenTimeMillis,true)
            binding.tvTimer.text = formatedTime
        }
        TrackingService.runTimeMillis.observeForever(observer3)

    }

    private fun startTheRun() {
        if (isTracking) {
            sendComandService(ACTION_PAUSE_SERVICE)
        } else {
            sendComandService(ACTION_START_OR_RESUME_SERVICE)
        }

    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking

        if (!isTracking) {
            binding.btnToggleRun.text = "START"
            binding.btnFinishRun.visibility = View.VISIBLE

        } else if (isTracking) {
            binding.btnToggleRun.text = "STOP"
            binding.btnFinishRun.visibility = View.GONE
        }

    }

    private fun getUpdateCameraLocation() {

        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(//cambiar a animate camera
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )

        }

    }

    private fun addAllPolyLines() {

        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)

        }
    }

    private fun addTheLastPolyline() {

        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }


    //PERMISIONS SECTION****************************************************************************

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPersmissions(this)) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app",
                REQUEST_CODE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        } else {

            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app",
                REQUEST_CODE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )

        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }

    //LIFECYCLE OF MAPVIEW**************************************************************************
    override fun onResume() {
        super.onResume()
        binding.mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        binding.mapView?.onSaveInstanceState(outState)
    }


}