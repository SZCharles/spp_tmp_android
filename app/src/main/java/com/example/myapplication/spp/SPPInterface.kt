package com.example.myapplication.spp

interface SPPInterface {
    fun inputData(content: ByteArray)
    fun outputData(content: ByteArray)
    fun connected()
    fun connectFail(errInfo: String)
    fun boundDeviceFound(name: String, mac: String)
}