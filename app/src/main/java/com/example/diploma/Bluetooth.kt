package com.example.diploma

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import androidx.compose.runtime.mutableStateListOf
import java.util.UUID

class Bluetooth(){
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothGatt: BluetoothGatt? = null

    private val SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    private val CHARACTERISTIC_UUID = UUID.fromString("00001102-0000-1000-8000-00805f9b34fb")

    val devices = mutableStateListOf<BluetoothDevice>()
    val receivedData = mutableStateListOf<String>()
}