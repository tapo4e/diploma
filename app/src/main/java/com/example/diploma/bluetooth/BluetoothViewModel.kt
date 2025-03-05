package com.example.diploma.bluetooth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.diploma.presentation.App
import kotlinx.coroutines.launch

class BluetoothViewModel(private val controller: BluetoothController) : ViewModel() {
    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>, extras: CreationExtras
            ): T {
                val bluetooth =
                    (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).blueToothController
                return BluetoothViewModel(bluetooth) as T
            }
        }
    }
    val data = controller.readDataStateFlow
    val scannedDev = controller.scannedDevices
    val isConnected = controller.isConnected
    fun startDiscovery(){
        viewModelScope.launch {
            controller.startDiscovery()
        }
    }
    fun send(){
        viewModelScope.launch { controller.sendMessage() }
    }

    override fun onCleared() {
        super.onCleared()
        controller.release()
    }
}