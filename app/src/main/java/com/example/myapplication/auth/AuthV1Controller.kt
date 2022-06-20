package com.example.myapplication.auth

import com.example.myapplication.message.*
import com.example.myapplication.message.bean.NVMessage
import com.example.myapplication.message.observers.AuthenticationObserver
import com.example.myapplication.message.observers.DeviceInfoObserver
import com.example.myapplication.protocol.DeviceMessagePayload
import com.google.gson.Gson
import com.google.protobuf.ByteString
import kotlin.random.Random


/**
 * step 0: Get Device ID
 * step 1: Query Device List
 *         Send PlainText and Nonce to Device
 * step 2: Get cipher from Result
 *         Encode cipher and compare it
 *         Send notify result to Device
 */
class AuthV1Controller private constructor() : DeviceInfoObserver, AuthenticationObserver {

    companion object {

        private var instance: AuthV1Controller? = null

        fun getInstance(): AuthV1Controller {
            if (instance == null) {
                instance = AuthV1Controller()
            }
            return instance!!
        }
    }

    private var deviceID: DeviceMessagePayload.DeviceID? = null
    private var text: ByteArray? = null
    private var iv: ByteArray? = null
    private var product: Product? = null

    override fun deviceVersionCallback(deviceVersion: DeviceMessagePayload.DeviceVersion) {

    }


    /**
     *     step: 0
     */
    fun startAuth() {
        MessageController.addDeviceInfoObserver(this)
        MessageController.addAuthenticationObserver(this)
        val message = NVMessage(NVClass.DEVICE_INFO, DeviceIDID.DEVICE_ID.value, NVOperators.GET)
        MessageController.outputBytes(message.toBytes())
    }

    /**
     *     step: 1
     */
    override fun deviceIdCallback(deviceID: DeviceMessagePayload.DeviceID) {
        //TODO: Get supported devices from the server
        val devices = Gson().fromJson(TmpJson.devicesJsonStr, NVDevices::class.java)
        for (product in devices.products) {
            if (product.vendorId == deviceID.vendorID && product.modelId == deviceID.modelID) {
                this.deviceID = deviceID
                //Send text messages that need to be encrypted
                this.product = product
                val challengeBuilder = DeviceMessagePayload.Challenge.newBuilder()
                text = Random.nextBytes(AESEncrypt.TEXT_LENGTH)
                iv = Random.nextBytes(AESEncrypt.IV_LENGTH)
                challengeBuilder.plaintext = ByteString.copyFrom(text)
                challengeBuilder.nonce = ByteString.copyFrom(iv)
                val message = NVMessage(
                    NVClass.AUTH,
                    AuthenticationID.VERIFY_SECRET.value,
                    NVOperators.RUN,
                    body = challengeBuilder.build().toByteArray()
                )
                MessageController.outputBytes(message.toBytes())
                return
            }
        }
        endAuth()
    }

    /**
     *     step: 2
     */
    override fun authChallengeResultCallback(result: DeviceMessagePayload.ChallengeResult) {
        if (text != null && product != null) {
            val resultText = AESEncrypt.decrypt(
                key = product!!.aesKey.decodeHex(),
                iv = iv!!,
                encryptedData = result.chiperAndTag.toByteArray()
            )
            if (resultText.contentEquals(text)) {
                endAuth(success = true)
                return
            }
        }
        endAuth()

    }

    private fun endAuth(success:Boolean = false) {
        val authResultMessage = DeviceMessagePayload.AuthResult.newBuilder()
        authResultMessage.isSuccess = success
        val message = NVMessage(
            NVClass.AUTH,
            AuthenticationID.RESULT.value,
            NVOperators.NOTIFY,
            body = authResultMessage.build().toByteArray()
        )
        MessageController.outputBytes(message.toBytes())
        MessageController.removeDeviceInfoObserver(this)
        MessageController.removeAuthenticationObserver(this)
        deviceID = null
        text = null
        iv = null
        product = null
    }

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) {

        }
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}