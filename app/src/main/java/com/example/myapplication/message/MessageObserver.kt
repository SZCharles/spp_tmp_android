package com.example.myapplication.message

import com.example.myapplication.protocol.BLEMessagePayload

interface MessageObserver {

    fun authVersionCallback(authVersion: BLEMessagePayload.AuthVersion)
    fun deviceVersionCallback(deviceVersion: BLEMessagePayload.DeviceVersion)
    fun deviceIdCallback(deviceID: BLEMessagePayload.DeviceID)

}