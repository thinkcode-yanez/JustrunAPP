package com.example.justrun.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.justrun.config.RunApp.Companion.db
import com.example.justrun.config.db.Run
import com.example.justrun.config.repositories.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel:ViewModel() {

    val mainRepository=MainRepository()
    val alltrackList=MutableLiveData<List<Run>>()


    fun iniciar(){

        viewModelScope.launch {

            alltrackList.value= withContext(Dispatchers.IO){
               // db.getRunDao().getAll()
                db.getRunDao().getAllRunsByDate()
            }!!
        }
    }


    fun insertRun(run: Run)=viewModelScope.launch {
        mainRepository.insertRun(run)
    }
    fun getAllRunsByDate()=viewModelScope.launch {
        mainRepository.getAllRunsByDate()
    }
    fun deleteById(id:Int)=viewModelScope.launch {
        mainRepository.deleteByID(id)
    }
   suspend fun getById(id:Int)=viewModelScope.launch {
        mainRepository.getById(id)
    }






}