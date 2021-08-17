package com.example.justrun.viewmodels

import androidx.lifecycle.ViewModel
import com.example.justrun.config.repositories.MainRepository

class MainViewModel:ViewModel() {

    val mainRepository=MainRepository()


}