package com.example.diploma.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diploma.bluetooth.data.BlData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothController(private val context: Context) {

    private var dataTransferService: BluetoothDataTransferService? = null
    private val _readDataStateFlow = MutableStateFlow("")
    val readDataStateFlow: StateFlow<String>
        get() = _readDataStateFlow.asStateFlow()

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()
    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null

    private val _scannedDevices = MutableStateFlow(BlData())
    val scannedDevices: StateFlow<BlData>
        get() = _scannedDevices.asStateFlow()



    private val bluetoothStateReceiver = StateReceiver { isConnected, bluetoothDevice ->
        if(bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
            _isConnected.update { isConnected }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.emit("Can't connect to a non-paired device.")
            }
        }
    }

    private val foundDeviceReceiver = DeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = BlData(listOf(1, 2), device.name, device.address)
            if (newDevice.name == "HC-05") {
                CoroutineScope(Dispatchers.IO).launch {
                    connectToDevice(newDevice)
                }
                newDevice
            } else devices
        }
    }

    private val _errors = MutableSharedFlow<String>()
    val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()



    init {
//        CoroutineScope(Dispatchers.IO).launch {
//            updatePairedDevices()
//        }
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
    }

    suspend fun startDiscovery() {

        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN))
            return

//        updatePairedDevices()
//        if (_scannedDevices.value.address != "") {
//            connectToDevice(_scannedDevices.value)
//            return
//        }
        Log.d("startDiscovery", "ok")
        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )
        bluetoothAdapter?.startDiscovery()
    }

    private suspend fun stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        Log.d("stopDiscovery", "ok")
        bluetoothAdapter?.cancelDiscovery()
    }

    private suspend fun connectToDevice(device: BlData) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            throw SecurityException("No BLUETOOTH_CONNECT permission")
        }

        currentClientSocket = bluetoothAdapter
            ?.getRemoteDevice(device.address)
            ?.createRfcommSocketToServiceRecord(
                UUID.fromString(SERVICE_UUID)
            )
        stopDiscovery()

        currentClientSocket?.let { socket ->
            try {
                socket.connect()
                val service = BluetoothDataTransferService(socket)
                dataTransferService = service
                service.listenForIncomingMessages().collect { device ->
                    Log.d("data", device)
                    _readDataStateFlow.update { it + device }
                }

            } catch (e: IOException) {
                socket.close()
                currentClientSocket = null
            }
        }

    }

    suspend fun sendMessage(data: Int = 1) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        dataTransferService?.sendMessage(data)
    }

    private fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
    }

    fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
    }

//     private suspend fun updatePairedDevices() {
//        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
//            Log.d("updatePairedDevices", "cancel")
//            return
//        }
//        Log.d("updatePairedDevices", "ok")
//        bluetoothAdapter?.bondedDevices?.forEach { device ->
//            if (device.name == "HC-05") _scannedDevices.update {
//                BlData(
//                    name = device.name,
//                    address = device.address
//                )
//            }
//        }
//    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val SERVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    }
}