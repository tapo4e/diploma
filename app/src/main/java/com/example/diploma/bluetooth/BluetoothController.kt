package com.example.diploma.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.example.diploma.bluetooth.data.BlData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@SuppressLint("MissingPermission")
class BluetoothController(private val context: Context) {

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val _scannedDevices = MutableStateFlow<List<BlData>>(emptyList())
    val scannedDevices: StateFlow<List<BlData>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BlData>>(emptyList())
    val pairedDevices: StateFlow<List<BlData>>
        get() = _pairedDevices.asStateFlow()

    private val foundDeviceReceiver = DeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.
            if(newDevice in devices) devices else devices + newDevice
        }
    }

    init {
        updatePairedDevices()

    }



     private fun updatePairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        bluetoothAdapter?.bondedDevices?.map{
            BlData(listOf(1,2),it.name)}?.also { device -> _pairedDevices.update { device} }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}