package com.example.justrun.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem


import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
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
import com.example.justrun.services.TrackingService.Companion.runTimeMillis
import com.example.justrun.stuff.Constants.ACTION_PAUSE_SERVICE
import com.example.justrun.stuff.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.justrun.stuff.Constants.ACTION_STOP_SERVICE
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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import kotlin.math.round

class MainMapTrackActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    ////MapsVariables
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var map: GoogleMap? = null
    private var currenTimeMillis = 0L


    private var menu: Menu? = null
    var gpsStatus: Boolean = false


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
            isLocationEnabled()
            if(gpsStatus) {
                startTheRun()
            }
        }
        binding.btnFinishRun.setOnClickListener {

            if(TrackingService.flagData){
                zoomWholeTrack()
                finishRunAndSaveTheWholeData()
            }else{
                Toast.makeText(this, "Data not available to save.\n Start New Run or Wait.", Toast.LENGTH_SHORT)
                    .show()

            }


        }
        binding.btnRUNS.setOnClickListener{
            val intent=Intent(this,RunsActivity::class.java)
            this.startActivity(intent)
        }
    }

    private fun sendComandService(action: String) =
        Intent(this, TrackingService::class.java).also {
            it.action = action
            this.startService(it)
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_cancel_menu, menu)
        this.menu = menu
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cancelTracking -> cancelRunDialog()
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)

        if (currenTimeMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
        return true
    }


    private fun cancelRunDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Cancel Run?")
            .setMessage("Are you sure to cancel the current run and delete the data?")
            .setIcon(R.drawable.ic_baseline_delete_forever_24)
            .setPositiveButton("YES") { _, _ ->
                stopRun()
                val intent=Intent(this,RunsActivity::class.java)
                this.startActivity(intent)


              //  binding.tvTimer.text="00:00:00:00"
               // map?.clear()

            }
            .setNegativeButton("NO") { dialogInterface, _ ->
                dialogInterface.cancel()

            }
            .create()
        dialog.show()

    }



    private fun stopRun() {
        binding.tvTimer.text="00:00:00:00"
        map?.clear()
        sendComandService(ACTION_STOP_SERVICE)

    }


    private fun subscribeObservers() {

        val observer = Observer<Boolean> { data ->
            updateTracking(data)
        }
        TrackingService.isTracking.observeForever(observer)

        val observer2 = Observer<Polylines> {
            pathPoints = it
            addTheLastPolyline()
            getUpdateCameraLocation()
        }
        TrackingService.pathPoints.observeForever(observer2)

        val observer3 = Observer<Long> { data ->
            currenTimeMillis = data
            val formatedTime = TrackingUtility.setFormatOfStopWathch(currenTimeMillis, true)
            binding.tvTimer.text = formatedTime
        }
        TrackingService.runTimeMillis.observeForever(observer3)

    }

    private fun startTheRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendComandService(ACTION_PAUSE_SERVICE)
        } else {
            sendComandService(ACTION_START_OR_RESUME_SERVICE)
        }

    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking

        if (!isTracking && currenTimeMillis>0L) {
            binding.btnToggleRun.text = "START"
            binding.btnFinishRun.visibility = View.VISIBLE

        } else if(isTracking ) {
            binding.btnToggleRun.text = "STOP"
            menu?.getItem(0)?.isVisible = true
           binding.btnFinishRun.visibility = View.GONE
        }

    }

    private fun zoomWholeTrack(){
        val bounds = LatLngBounds.builder()

        for (polilyne in pathPoints) {
            for (pos in polilyne) {
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )

    }
    private fun finishRunAndSaveTheWholeData() {

        map?.snapshot { bmp ->

            var distanceMts = 0 //VALOR DE DISTANCIA OBTENIDO PARA LA DB
            for (polyline in pathPoints) {
                distanceMts += TrackingUtility.calculateDistanceLength(polyline).toInt()
            }
            //VALOR DE VELOCIDAD PROMEDIO OBTENIDO
            val avgSpeed =
                round((distanceMts / 1000f) / (currenTimeMillis / 1000f / 60 / 60) * 10) / 10f
           // round((distanceMts / 1000f) / (currenTimeMillis / 1000f / 60 / 60) * 10) / 10f //km/h
            //Valor de la Fecha
            val date = Calendar.getInstance().timeInMillis

            //Guardamos en base de datos via viewmodel Room
            val run = Run(bmp, date, avgSpeed, distanceMts, currenTimeMillis)

            mainViewModel.insertRun(run)

            Toast.makeText(this, "GOOD JOB, RUN SAVED SUCCESSFULLY", Toast.LENGTH_SHORT).show()
            stopRun()//Reiniciamos todas las variables
            val intent=Intent(this,RunsActivity::class.java)
            this.startActivity(intent)// NOS VAMOS AL RUNS ACTIIVTY

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

    private fun isLocationEnabled() {

        val locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!gpsStatus) {
            Toast.makeText(this, "Turn ON the GPS location", Toast.LENGTH_SHORT).show()
        }
    }
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
        if(pathPoints.isEmpty()) {
            binding.btnFinishRun.isVisible = false
        }
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