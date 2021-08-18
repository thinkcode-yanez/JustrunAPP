package com.example.justrun.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.justrun.R
import com.example.justrun.adapters.RunAdapter
import com.example.justrun.databinding.ActivityRunsBinding
import com.example.justrun.viewmodels.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class RunsActivity : AppCompatActivity() {

    lateinit var binding:ActivityRunsBinding
    lateinit var runAdapter: RunAdapter
    private val mainViewModel: MainViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRunsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel.iniciar()

        binding.myMainRecycler.apply {
            layoutManager = LinearLayoutManager(applicationContext)
        }

        mainViewModel.alltrackList.observe(this, Observer {

            binding.myMainRecycler.adapter = RunAdapter(it)

        })

        binding.btnAddRun.setOnClickListener {
            val intent= Intent(this,MainMapTrackActivity::class.java)
            this.startActivity(intent)
        }


    }

    override fun onResume() {
        super.onResume()
        mainViewModel.iniciar()
    }


}