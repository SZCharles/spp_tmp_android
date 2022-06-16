package com.example.myapplication.message.observers

import com.example.myapplication.protocol.DeviceMessagePayload

interface DeviceInfoObserver {

    fun deviceVersionCallback(deviceVersion: DeviceMessagePayload.DeviceVersion)
    fun deviceIdCallback(deviceID: DeviceMessagePayload.DeviceID)

}