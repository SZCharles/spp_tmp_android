package com.example.myapplication.message

@Suppress("unused")
enum class MessageIDDefine {
}

/**
 * Device Version	0x00
 * Device ID	0x01
 */
@Suppress("unused")
enum class DeviceIDID(val value: Int) {
    DEVICE_VERSION(0),
    DEVICE_ID(1)
}

/**
 * Version	0x00
 * Exchange Public Keys	0x01
 * Verify Secret	0x02
 * Verify Data	0x03
 * Result	0x04
 */
@Suppress("unused")
enum class AuthenticationID(val value: Int) {
    VERSION(0),
    EXCHANGE_PUBLIC_KEYS(1),
    VERIFY_SECRET(2),
    VERIFY_DATA(3),
    RESULT(4)
}

/**
 * Start Speech Stream	0x00
 * Audio Stream	0x01
 * Notify Wake Word Detected 0x02
 * Stop Speech Stream 0x03
 * Notify Start Response 0x04
 * Notify Stop Response	0x05
 * Stop Response 0x06
 */
@Suppress("unused")
enum class AudioID(val value: Int) {
    START(0),
    STREAM(1),
    WAKE_WORD(2),
    STOP(3),
    START_RESPONSE_NOTIFY(4),
    STOP_RESPONSE_NOTIFY(5),
    STOP_RESPONSE(6)
}