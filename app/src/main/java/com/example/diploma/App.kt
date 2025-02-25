package com.example.diploma

import android.app.Application
import com.example.diploma.bluetooth.BluetoothController

class App : Application() {
    val blueToothController by lazy { BluetoothController(applicationContext) }
}