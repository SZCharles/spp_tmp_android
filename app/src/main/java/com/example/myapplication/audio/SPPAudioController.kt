package com.example.myapplication.audio

import com.example.myapplication.message.*
import com.example.myapplication.message.bean.NVMessage
import com.theeasiestway.opus.Constants
import com.theeasiestway.opus.Opus
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class SPPAudioController {

    companion object {

        private var instance: SPPAudioController? = null
        private var opus: Opus? = null
        private var tmpData: ByteArray = ByteArray(0)
        private var tmpOpusData: ByteArray = ByteArray(0)
        private var index = 0;
        private var indexPCMTEMP = 0;

        fun getInstance(): SPPAudioController {
            if (instance == null) {
                instance = SPPAudioController()
            }
            return instance!!
        }
    }

    fun startSpeech() {
        if (opus == null) {
            opus = Opus()
        }
        opus!!.decoderInit(Constants.SampleRate._16000(), Constants.Channels.mono())
        val message = NVMessage(
            NVClass.VOICE_SESSION,
            AudioID.START.value,
            NVOperators.RESULT,
            nvResultCode = ReturnCode.SUCCESS
        )
        MessageController.outputBytes(message.toBytes())
        saveFile()
    }

    fun notifyStream(data: ByteArray) {
        val opusData = data.copyOfRange(3, data.size)
        tmpOpusData += opusData
        runBlocking {
            val pcmData: ByteArray?
            withContext(Dispatchers.IO) {
                pcmData = decode2PCM(opusData)
            }
            index++
            println("pcm index: " + index.toString())
            if (pcmData == null) {
                indexPCMTEMP++
                println("pcm null: " + indexPCMTEMP)
            }
            // send to device
            tmpData += pcmData!!
//            if(tmpData.size<20480){
//                return@runBlocking
//            }
//            withContext(Dispatchers.IO){
//                val file = File("/sdcard", "test.pcm")
//                val fileOpus = File("/sdcard", "opus_test")
//                if (!fileOpus.exists()) {
//                    file.createNewFile()
//                }
//                if (!file.exists()) {
//                    file.createNewFile()
//                }
//                val output = FileOutputStream(file, false)
//                val outputOpus = FileOutputStream(fileOpus, false)
//                output.write(tmpData)
//                outputOpus.write(tmpOpusData)
//                output.close()
//                outputOpus.close()
//                tmpData = ByteArray(0)
//                tmpOpusData = ByteArray(0)
//            }

        }

    }

    private fun saveFile() {
        object : Thread() {
            override fun run() {
                sleep(10000)
                // 3s后会执行的操作
                val file = File("/sdcard", "test.pcm")
                val fileOpus = File("/sdcard", "opus_test")
                if (!fileOpus.exists()) {
                    file.createNewFile()
                }
                if (!file.exists()) {
                    file.createNewFile()
                }
                val output = FileOutputStream(file, false)
                val outputOpus = FileOutputStream(fileOpus, false)
                output.write(tmpData)
                outputOpus.write(tmpOpusData)
                output.close()
                outputOpus.close()
                tmpData = ByteArray(0)
                tmpOpusData = ByteArray(0)
            }
        }.start()

    }

    private suspend fun decode2PCM(data: ByteArray): ByteArray? = coroutineScope {
        val pcmDataRequest = async {
            opus?.decode(data, Constants.FrameSize._640())
        }
        pcmDataRequest.await()
    }
}
