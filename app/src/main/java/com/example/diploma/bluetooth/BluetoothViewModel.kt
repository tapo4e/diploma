package com.example.diploma.bluetooth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.diploma.App
import kotlinx.coroutines.launch

class BluetoothViewModel(private val controller: BluetoothController) : ViewModel() {
    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>, extras: CreationExtras
            ): T {
                val dataBase =
                    (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).blueToothController
                return BluetoothViewModel(dataBase) as T
            }
        }
    }
    val mes = controller.readDataStateFlow
    val scannedData = controller.scannedDevices
    val data = controller.pairedDevices

    fun startDiscovery(){
        controller.startDiscovery()
    }
    fun connect(){
        viewModelScope.launch { controller.connectToDevice(if(data.value.address!="") data.value else scannedData.value) }
    }
    fun send(){
        viewModelScope.launch { controller.sendMessage() }
    }
}