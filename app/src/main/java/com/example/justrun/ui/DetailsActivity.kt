package com.example.justrun.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.example.justrun.R
import com.example.justrun.config.RunApp.Companion.db
import com.example.justrun.databinding.ActivityDetailsBinding
import com.example.justrun.stuff.TrackingUtility
import com.example.justrun.viewmodels.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DetailsActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    lateinit var binding:ActivityDetailsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Recuperamos id
        val idAborrar= intent.getStringExtra("dataId")
        val id= idAborrar?.toInt()
        cargarDatos(id!!)


        binding.btnBorrar.setOnClickListener {

            deleteRunDialog(id!!)
        }


    }



    private fun deleteRunDialog(id:Int) {
        val dialog = this.let { MaterialAlertDialogBuilder(it) }
            .setTitle("Delete Run?")
            .setMessage("Are you sure to delete the current run?")
            .setIcon(R.drawable.ic_baseline_delete_forever_24)
            .setPositiveButton("YES") { _, _ ->
                //stopRun()
                mainViewModel.deleteById(id)
               /* val intent= Intent(this,RunsActivity::class.java)
                this.startActivity(intent)*/
                finish()

            }
            .setNegativeButton("NO") { dialogInterface, _ ->
                dialogInterface.cancel()

            }
            .create()
        dialog.show()

    }

    private fun cargarDatos(id: Int){

            lifecycleScope.launch {
                var run = withContext(Dispatchers.IO){
                  db.getRunDao().selectByID(id)
                }
                Glide.with(applicationContext).load(run.img).into(binding.imMAP)

                val calendar = Calendar.getInstance().apply {
                    timeInMillis = run.timestamp
                }
                val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
                binding.tvDate.text = dateFormat.format(calendar.time)

                val avgspeed = "${run.avgSpeedKmh}km/h"
                binding.tvVelocity.text = avgspeed

                val distanceKm = "${run.distanceMeters / 1000f}km"
                binding.tvDistance.text = distanceKm

                binding.tvTime.text = TrackingUtility.setFormatOfStopWathch(run.timeInMillis)

            }




    }


}