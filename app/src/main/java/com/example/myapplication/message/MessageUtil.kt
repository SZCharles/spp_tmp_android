package com.example.myapplication.message

import NVMessageHeader
import com.example.myapplication.message.bean.NVMessage

class MessageUtil {

    companion object {
        private var data: ByteArray = ByteArray(0)

        fun decodeMessage(bytes: ByteArray): NVMessage? {
            data += bytes
            // Not long enough to header parse
            if (data.size < 5) {
                return null
            }
            val header = NVMessageHeader(data)
            if (header.nvPayloadLength > (data.size - header.nvHeaderLength)) {
                // Not long enough to body parse
                return null
            }
            val bodyData = data.copyOfRange(
                header.nvHeaderLength,
                header.nvPayloadLength + header.nvHeaderLength
            )
//            data = data.copyOfRange(header.nvPayloadLength+header.nvHeaderLength, data.size)
            //TODO： TMP, Because the length of the data sent by the firmware is incorrect,
            // it is temporarily processed.
            data = ByteArray(0)
            return NVMessage(header, bodyData)
        }
    }


}