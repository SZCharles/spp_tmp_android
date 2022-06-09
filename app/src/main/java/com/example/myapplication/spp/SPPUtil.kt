package com.example.myapplication.spp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import java.io.OutputStream
import java.util.*

/**
 * SPP Bluetooth Singleton
 *
 */
@SuppressLint("MissingPermission")
class SPPUtil private constructor(context: Context) {

    private val mAdapter: BluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    private val sppUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//    private val sppUUID: UUID = UUID.fromString("78DB4F90-DDF7-4A83-92E9-3CE422C89975")

    private var connecting = false

    private var sppInterface: SPPInterface? = null

    private var outputStream: OutputStream? = null

    private var mBluetoothSocket: BluetoothSocket? = null

    companion object {
        private var instance: SPPUtil? = null

        fun getInstance(context: Context): SPPUtil {
            if (instance == null)
                instance = SPPUtil(context)
            return instance!!
        }
    }

    fun setListen(callback: SPPInterface) {
        sppInterface = callback
    }

    fun enableBluetooth(): Boolean {
        if (!mAdapter.isEnabled) {
            mAdapter.enable()
            return false
        }
        return true

    }

    fun startScan() {
        stopScan()
        mAdapter.startDiscovery()
    }

    fun stopScan() {
        if (mAdapter.isDiscovering)
            mAdapter.cancelDiscovery()
    }

    fun startConnect(device: BluetoothDevice) {
        if (connecting) {
            return
        }
        mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(sppUUID)
        connecting = true
        Thread {
            try {
                mBluetoothSocket!!.connect()
                sppInterface?.connected()
                val inputStream = mBluetoothSocket!!.inputStream
                outputStream = mBluetoothSocket!!.outputStream
                while (true) {
                    if (inputStream.available() == 0) {
                        val buffer = ByteArray(256)
                        val num = inputStream.read(buffer)
                        if (num > 0) {
                            sppInterface?.inputData(buffer.copyOf(num))
                        }
                    } else {
                        Thread.sleep(200)
                    }
                }
            } catch (e: Exception) {
                connecting = false
                e.message?.let { sppInterface?.connectFail(it) }
            }

        }.start()

    }

    fun sendData(bytes: ByteArray) {
        outputStream?.write(bytes)
        sppInterface?.outputData(bytes)
    }

    fun disconnect() {
        mBluetoothSocket?.close()
    }

    fun boundDevices():Set<BluetoothDevice>{
        return mAdapter.bondedDevices
    }

}
