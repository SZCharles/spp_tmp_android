package com.example.myapplication.message

import NVMessageHeader
import com.example.myapplication.message.bean.NVMessage

class MessageUtil {

    companion object {
        private var data: ByteArray = ByteArray(0)

        fun decodeMessages(bytes: ByteArray): List<NVMessage> {
            val messages = ArrayList<NVMessage>()
            data += bytes
            while (true) {
                val message = decodeMessage()
                if (message != null) {
                    messages.add(message)
                } else {
                    return messages
                }
            }
        }

        private fun decodeMessage(): NVMessage? {
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
            data = data.copyOfRange(header.nvPayloadLength + header.nvHeaderLength, data.size)
            return NVMessage(header, bodyData)
        }
    }


}