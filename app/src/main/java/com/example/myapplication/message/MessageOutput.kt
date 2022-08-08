package com.example.myapplication.message

interface MessageOutput {

    fun outputBytes(data: ByteArray)
    fun print(content: String)

}