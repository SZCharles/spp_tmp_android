package com.example.myapplication.auth

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES Encrypt
 */
object AESEncrypt {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_LENGTH = 16
    const val TEXT_LENGTH = 16
    const val IV_LENGTH = 12

    private fun decrypt(key: ByteArray, iv: ByteArray, encryptedData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_LENGTH * 8, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKeyFromString(key), spec)
        return cipher.doFinal(encryptedData)
    }

    private fun getSecretKeyFromString(key: ByteArray): SecretKey {
        return SecretKeySpec(key, 0, key.size, "AES")
    }

    fun decryptOnlyText(key: ByteArray, iv: ByteArray, encryptedData: ByteArray): ByteArray {
        val result = decrypt(key, iv, encryptedData)
        return result.copyOfRange(0, result.size - TAG_LENGTH)
    }

}