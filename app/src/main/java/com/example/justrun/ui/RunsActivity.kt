package com.example.justrun.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.justrun.R
import com.example.justrun.viewmodels.MainViewModel

class RunsActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_runs)
    }
}