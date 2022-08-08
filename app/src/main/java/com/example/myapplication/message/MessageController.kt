package com.example.myapplication.message

import com.example.myapplication.audio.SPPAudioController
import com.example.myapplication.auth.AuthV1Controller
import com.example.myapplication.message.bean.NVMessage
import com.example.myapplication.message.observers.AuthenticationObserver
import com.example.myapplication.message.observers.DeviceInfoObserver
import com.example.myapplication.protocol.DeviceMessagePayload

/**
 * Message controller, exposed to APP for data interaction
 */
@Suppress("unused")
class MessageController {
    companion object {

        private val deviceInfoObservers: ArrayList<DeviceInfoObserver> = ArrayList()
        private val authChallengeObservers: ArrayList<AuthenticationObserver> = ArrayList()
        private var output: MessageOutput? = null

        fun addDeviceInfoObserver(observer: DeviceInfoObserver) {
            deviceInfoObservers.add(observer)
        }

        fun removeDeviceInfoObserver(observer: DeviceInfoObserver) {
            deviceInfoObservers.remove(observer)
        }

        fun addAuthenticationObserver(observer: AuthenticationObserver) {
            authChallengeObservers.add(observer)
        }

        fun removeAuthenticationObserver(observer: AuthenticationObserver) {
            authChallengeObservers.remove(observer)
        }

        // input data entry
        fun inputBytes(bytes: ByteArray) {
            val messages = MessageUtil.decodeMessages(bytes)
            for (message in messages) {
                when (message.header.nvClass) {
                    NVClass.DEVICE_INFO -> handleDeviceInfo(message)
                    NVClass.VOICE_SESSION -> handleAudio(message)
                    NVClass.AUTH -> handleAuth(message)
                }
            }
        }

        fun outputBytes(bytes: ByteArray) {
            output?.outputBytes(bytes)
        }

        fun setOutputListen(output: MessageOutput) {
            this.output = output
        }

        fun start() {
            val message = NVMessage(NVClass.AUTH, AuthenticationID.VERSION.value, NVOperators.GET)
            outputBytes(message.toBytes())
        }

        /**
         * Device Version	0x00
         *  Device ID	0x01
         */
        private fun handleDeviceInfo(message: NVMessage) {
            when (message.header.nvId) {
                0 ->
                    when (message.header.nvOperator) {
                        NVOperators.RESULT -> {
                            for (observer in deviceInfoObservers) {
                                observer.deviceVersionCallback(
                                    DeviceMessagePayload.DeviceVersion.parseFrom(
                                        message.body
                                    )
                                )
                            }
                        }
                        else -> {}
                    }

                1 -> when (message.header.nvOperator) {
                    NVOperators.RESULT -> {
                        for (observer in deviceInfoObservers) {
                            observer.deviceIdCallback(
                                DeviceMessagePayload.DeviceID.parseFrom(
                                    message.body
                                )
                            )
                        }
                    }
                    else -> {}
                }
            }
        }

        /**
         * Version	0x00
         * Exchange Public Keys	0x01
         * Verify Secret	0x02
         * Verify Data	0x03
         * Result	0x04
         */
        private fun handleAuth(message: NVMessage) {
            when (message.header.nvId) {
                AuthenticationID.VERSION.value -> {
                    val authVersion = DeviceMessagePayload.AuthVersion.parseFrom(
                        message.body
                    )
                    if (authVersion.authVer == DeviceMessagePayload.AuthVersion.AuthVer.AUTH_V1) {
                        AuthV1Controller.getInstance().startAuth()
                    }
                    // TODO: "Auth_V2 has not been implemented yet"
//                    else if (authVersion.authVer == DeviceMessagePayload.AuthVersion.AuthVer.AUTH_V2) {
//                    }
                }
                AuthenticationID.VERIFY_SECRET.value -> {
                    for (observer in authChallengeObservers) {
                        observer.authChallengeResultCallback(
                            DeviceMessagePayload.ChallengeResult.parseFrom(
                                message.body
                            )
                        )
                    }
                }
            }
        }

        private fun handleAudio(message: NVMessage) {
            when (message.header.nvId) {
                AudioID.START.value -> {
                    SPPAudioController.getInstance().startSpeech()
                }
                AudioID.STREAM.value -> {
                    println("handle" + message.header.nvIndex)
                    SPPAudioController.getInstance().notifyStream(message.body)
                }
                AudioID.START_RESPONSE_NOTIFY.value -> {
//                    val response = DeviceMessagePayload.StartResponse.parseFrom(message.body)
//                    SPPAudioController.getInstance().responseData(response)
                }
            }
        }
    }
}