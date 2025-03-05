package com.example.diploma.presentation

import android.app.Application
import com.example.diploma.bluetooth.BluetoothController

class App : Application() {
    val blueToothController by lazy { BluetoothController(applicationContext) }
}