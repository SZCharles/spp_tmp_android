package com.example.myapplication.message

import com.example.myapplication.message.bean.NVMessage
import com.example.myapplication.protocol.BLEMessagePayload

/**
 * Message controller, exposed to APP for data interaction
 */
@Suppress("unused")
class MessageController {
    companion object {

        private val observers: ArrayList<MessageObserver> = ArrayList()

        fun addObserver(observer: MessageObserver) {
            observers.add(observer)
        }

        fun removeObserver(observer: MessageObserver) {
            observers.remove(observer)
        }

        fun putBytes(bytes: ByteArray) {
            val message = MessageUtil.decodeMessage(bytes)
            if (message != null) {
                when (message.header.nvClass) {
                    NVClass.DEVICE_INFO -> handleDeviceInfo(message)
                    NVClass.VOICE_SESSION -> TODO("Not yet supported")
                    NVClass.AUTH -> handleAuth(message)
                }
            }
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
                            for (observer in observers) {
                                observer.deviceVersionCallback(
                                    BLEMessagePayload.DeviceVersion.parseFrom(
                                        message.body
                                    )
                                )
                            }
                        }
                        else -> TODO("Not yet supported")
                    }

                1 -> when (message.header.nvOperator) {
                    NVOperators.RESULT -> {
                        for (observer in observers) {
                            observer.deviceIdCallback(
                                BLEMessagePayload.DeviceID.parseFrom(
                                    message.body
                                )
                            )
                        }
                    }
                    else -> TODO("Not yet supported")
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
                0 -> {
                    for (observer in observers) {
                        observer.authVersionCallback(BLEMessagePayload.AuthVersion.parseFrom(message.body))
                    }
                }
                else -> TODO("Not yet supported")
            }
        }
    }

}