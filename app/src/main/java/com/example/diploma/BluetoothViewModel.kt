package com.example.diploma

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import java.util.UUID
import kotlin.coroutines.jvm.internal.CompletedContinuation.context

    class BluetoothViewModel (
        private val bluetoothController: BluetoothController<Any?>
    ): ViewModel() {

        private val _state = MutableStateFlow(BluetoothUiState())
        val state = combine(
            bluetoothController.scannedDevices,
            bluetoothController.pairedDevices,
            _state
        ) { scannedDevices, pairedDevices, state ->
            state.copy(
                scannedDevices = scannedDevices,
                pairedDevices = pairedDevices,
                messages = if(state.isConnected) state.messages else emptyList()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

        private var deviceConnectionJob: Job? = null

        init {
            bluetoothController.isConnected.onEach { isConnected ->
                _state.update { it.copy(isConnected = isConnected) }
            }.launchIn(viewModelScope)

            bluetoothController.errors.onEach { error ->
                _state.update { it.copy(
                    errorMessage = error
                ) }
            }.launchIn(viewModelScope)
        }

        fun connectToDevice(device: BluetoothDeviceDomain) {
            _state.update { it.copy(isConnecting = true) }
            deviceConnectionJob = bluetoothController
                .connectToDevice(device)
                .listen()
        }

        fun disconnectFromDevice() {
            deviceConnectionJob?.cancel()
            bluetoothController.closeConnection()
            _state.update { it.copy(
                isConnecting = false,
                isConnected = false
            ) }
        }

        fun waitForIncomingConnections() {
            _state.update { it.copy(isConnecting = true) }
            deviceConnectionJob = bluetoothController
                .startBluetoothServer()
                .listen()
        }

        fun sendMessage(message: String) {
            viewModelScope.launch {
                val bluetoothMessage = bluetoothController.trySendMessage(message)
                if(bluetoothMessage != null) {
                    _state.update { it.copy(
                        messages = it.messages + bluetoothMessage
                    ) }
                }
            }
        }

        fun startScan() {
            bluetoothController.startDiscovery()
        }

        fun stopScan() {
            bluetoothController.stopDiscovery()
        }

        private fun Flow<ConnectionResult>.listen(): Job {
            return onEach { result ->
                when(result) {
                    ConnectionResult.ConnectionEstablished -> {
                        _state.update { it.copy(
                            isConnected = true,
                            isConnecting = false,
                            errorMessage = null
                        ) }
                    }
                    is ConnectionResult.TransferSucceeded -> {
                        _state.update { it.copy(
                            messages = it.messages + result.message
                        ) }
                    }
                    is ConnectionResult.Error -> {
                        _state.update { it.copy(
                            isConnected = false,
                            isConnecting = false,
                            errorMessage = result.message
                        ) }
                    }
                }
            }
                .catch { throwable ->
                    bluetoothController.closeConnection()
                    _state.update { it.copy(
                        isConnected = false,
                        isConnecting = false,
                    ) }
                }
                .launchIn(viewModelScope)
        }

        override fun onCleared() {
            super.onCleared()
            bluetoothController.release()
        }
}