package com.example.myapplication.message.bean

import NVMessageHeader
import com.example.myapplication.message.NVClass
import com.example.myapplication.message.NVOperators
import com.example.myapplication.message.ReturnCode

class NVMessage {
    val header: NVMessageHeader
    val body: ByteArray

    constructor(header: NVMessageHeader, body: ByteArray) {
        this.header = header
        this.body = body
    }

    constructor(
        nvClass: NVClass,
        nvId: Int,
        nvOperator: NVOperators,
        body: ByteArray = ByteArray(0),
        encrypted: Boolean = false,
        nvResultCode: ReturnCode = ReturnCode.SUCCESS
    ) {
        this.body = body
        this.header = NVMessageHeader(nvClass, nvId, nvOperator, body.size, encrypted,nvResultCode)
    }

    fun toBytes(): ByteArray {
        return header.toBytes() + body
    }

}