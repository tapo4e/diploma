package com.example.diploma.bluetooth

import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class BluetoothDataTransferService(
    private val socket: BluetoothSocket
) {
    fun listenForIncomingMessages(): Flow<String> {
        return flow {
            Log.d("listen","ok")
            if (!socket.isConnected) {
                return@flow
            }
            val buffer = ByteArray(1024)
            while (true) {
                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    throw IOException("Reading incoming data failed")
                }
                emit(buffer.decodeToString(endIndex = byteCount))
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessage(byte: Int){
         withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write("1".encodeToByteArray())
                Log.d("send",byte.toString())
            } catch(e: IOException) {
                e.printStackTrace()

            }

        }
    }
}