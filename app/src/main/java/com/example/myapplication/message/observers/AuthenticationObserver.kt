package com.example.myapplication.message.observers

import com.example.myapplication.protocol.DeviceMessagePayload

interface AuthenticationObserver {
    fun authChallengeResultCallback(result: DeviceMessagePayload.ChallengeResult)
}