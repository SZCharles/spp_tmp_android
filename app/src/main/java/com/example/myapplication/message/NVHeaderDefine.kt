package com.example.myapplication.message

@Suppress("unused")
enum class NVClass(val value: Int) {
    DEVICE_INFO(0x00),
    VOICE_SESSION(0x01),
    AUTH(0x02)
}

@Suppress("unused")
enum class NVOperators(val value: Int) {
    GET(0x00),
    SET(0x01),
    RUN(0x02),
    RESULT(0x03),
    NOTIFY(0x04)
}

@Suppress("unused")
enum class NVFlags(val value: Int) {
    //If set, the payload length field is 16-bit long instead of the default 8-bit long.
    ZEROTH_LENGTH(0x01),

    //If set, the payload is encrypted. If a request set this bit,
    // the payload of this request's response should be encrypted as well.
    FIRST_ENCRYPTED(0x02)
}

@Suppress("unused")
enum class NVResponseCode(val value: Int) {
    //Valid message and successful operation
    SUCCESS(0x00),

    //Message class is not supported at the receiver
    CLASS_NOT_SUPPORTED(0x01),

    //Message ID is not supported at the receiver
    ID_NOT_SUPPORTED(0x02),

    //Operator not support at the receiver for this message ID
    OPERATOR_NOT_SUPPORTED(0x03),

    //Receiver is busy and cannot perform the requested operation
    BUSY(0x04),

    //Received data is invalid
    INVALID_DATA(0x05),

    //Received data is not supported
    UNSUPPORTED_DATA(0x06)
}

@Suppress("unused")
enum class ReturnCode(val value: Int) {
    SUCCESS(0X00),
    CLASS_NOT_SUPPORTED(0X01),
    ID_NOT_SUPPORTED(0X02),
    OPERATOR_NOT_SUPPORTED(0X03),
    BUSY(0X04),
    INVALID_DATA(0X05),
    UNSUPPORTED_DATA(0X06),
}

