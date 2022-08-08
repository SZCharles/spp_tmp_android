package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.message.*
import com.example.myapplication.message.bean.NVMessage
import com.example.myapplication.spp.SPPInterface
import com.example.myapplication.protocol.DeviceMessagePayload
import com.example.myapplication.spp.SPPUtil
import com.theeasiestway.opus.Constants
import com.theeasiestway.opus.Opus
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity(), View.OnClickListener, SPPInterface, MessageOutput {
    private lateinit var mEdName: EditText
    private lateinit var mEdContent: EditText
    private lateinit var deviceReceiver: DeviceReceiver
    private lateinit var mLv: ListView
    private val mData: ArrayList<String> = ArrayList()
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private val messageAddContent = 12580
    private val messageDataKey = "DATA_KEY"

    private val mHandler: Handler = Handler(Handler.Callback {
        if (it.what == messageAddContent) {
            it.data.getString(messageDataKey)?.let { it1 -> mData.add(it1) }
            arrayAdapter.notifyDataSetChanged()
        }
        true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initRegister()
        initListen()
    }

    private fun initListen() {
        findViewById<Button>(R.id.btn_test_scan_main).setOnClickListener(this)
        findViewById<Button>(R.id.btn_main_send).setOnClickListener(this)
        findViewById<Button>(R.id.btn_test_bound).setOnClickListener(this)
        findViewById<Button>(R.id.btn_get_device_version).setOnClickListener(this)
        findViewById<Button>(R.id.btn_get_auth_version).setOnClickListener(this)
        findViewById<Button>(R.id.btn_get_device_id).setOnClickListener(this)
        findViewById<Button>(R.id.btn_start_auth).setOnClickListener(this)
        findViewById<Button>(R.id.btn_test_opus).setOnClickListener(this)
        SPPUtil.getInstance(this).setListen(this)
        MessageController.setOutputListen(this)
    }

    private fun initView() {
        mEdName = findViewById(R.id.ed_main_name)
        mEdContent = findViewById(R.id.ed_main_content)
        mLv = findViewById(R.id.lv_main)
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mData)
        mLv.adapter = arrayAdapter
    }

    private fun initRegister() {
        val foundFilter = IntentFilter()
        foundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        foundFilter.addAction(BluetoothDevice.ACTION_FOUND)
        foundFilter.addAction(BluetoothDevice.ACTION_UUID)
        deviceReceiver = DeviceReceiver()
        registerReceiver(deviceReceiver, foundFilter)
        val arrayData: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH
            )
        }
        requestPermissions(
            arrayData, 1024
        )
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_main_send -> sendText()
            R.id.btn_test_scan_main -> testScan()
            R.id.btn_test_bound -> testBound()
            R.id.btn_get_device_version -> getDeviceVersion()
            R.id.btn_get_auth_version -> getAuthVersion()
            R.id.btn_get_device_id -> getDeviceID()
            R.id.btn_start_auth -> MessageController.start()
//            R.id.btn_test_opus -> decode2PCM()
        }
    }

    private fun getDeviceVersion() {
        val message = NVMessage(NVClass.DEVICE_INFO, 0, NVOperators.GET)
        SPPUtil.getInstance(this).sendData(message.toBytes())
    }

    private fun getDeviceID() {
        val message = NVMessage(NVClass.DEVICE_INFO, 1, NVOperators.GET)
        SPPUtil.getInstance(this).sendData(message.toBytes())
    }

    private fun getAuthVersion() {
        val message = NVMessage(NVClass.AUTH, 0, NVOperators.GET)
        SPPUtil.getInstance(this).sendData(message.toBytes())
    }

    private fun testBound() {
        val enable = SPPUtil.getInstance(this).enableBluetooth()
        if (!enable) {
            return
        }
        changeLayout()
        val devices = SPPUtil.getInstance(this).boundDevices()
        devices.forEach {
            if (it.name.contains(mEdName.text.toString())) {
                SPPUtil.getInstance(this).startConnect(it)
            }
        }
    }

    // start or end
    private fun testScan() {
        val enable = SPPUtil.getInstance(this).enableBluetooth()
        if (!enable) {
            return
        }
        changeLayout()

        SPPUtil.getInstance(this).startScan()
    }

    private fun sendText() {
        val content: String = mEdContent.text.toString()
        mEdContent.text.clear()
        val version =
            DeviceMessagePayload.DeviceVersion.newBuilder().setFirmwareVersion(content).build()
        val message = NVMessage(
            NVClass.DEVICE_INFO, DeviceIDID.DEVICE_VERSION.value,
            NVOperators.GET, version.toByteArray()
        )

        SPPUtil.getInstance(this).sendData(message.toBytes())

        Log.d("Charles", bytesToHex(message.toBytes()))
    }

    /**
     * Get Device BR
     */
    inner class DeviceReceiver : BroadcastReceiver() {
        override fun onReceive(appContext: Context?, newIntent: Intent?) {
            when (newIntent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        newIntent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.fetchUuidsWithSdp()
                    device?.uuids?.forEach {
                        Log.d("Charles", it.uuid.toString())
                    }
                    if (device?.name != null) {
                        addContent(String.format(getString(R.string.found_hint), device.name))

                        if (device.name.contains(mEdName.text)) {
                            SPPUtil.getInstance(this@MainActivity).startConnect(device)
                            SPPUtil.getInstance(this@MainActivity).stopScan()
                            addContent(getString(R.string.connecting))
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                }
                BluetoothDevice.ACTION_UUID -> {
                    val device: BluetoothDevice? =
                        newIntent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device?.name?.contains(mEdName.text) == true) {
                        device.uuids?.forEach {
                            Log.d("Charles", it.uuid.toString())
                        }
                    }
                }

            }
        }
    }

    // Get from SPP Data
    override fun inputData(content: ByteArray) {
        addContent(getString(R.string.from_device, bytesToHex(content)))
        MessageController.inputBytes(content)
//        NativeVoiceIOT.
    }

    override fun outputData(content: ByteArray) {
        addContent(getString(R.string.from_phone, bytesToHex(content)))
    }

    override fun connected() {
        addContent(getString(R.string.connected))
    }

    override fun connectFail(errInfo: String) {
        addContent(getString(R.string.err_hint, errInfo))
    }

    override fun boundDeviceFound(name: String, mac: String) {
        addContent(getString(R.string.bound_device_discover, name, mac))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(deviceReceiver)
        SPPUtil.getInstance(this).disconnect()
    }

    private fun bytesToHex(bytes: ByteArray): String {
        var result = ""
        for (b in bytes) {
            result += " "
            val st = String.format("%02X", b)
            result += st
        }
        return result
    }

    private fun changeLayout(startConnect: Boolean = true) {
        if (startConnect) {
            findViewById<View>(R.id.ll_connect_layout).visibility = View.GONE
            findViewById<View>(R.id.ll_main_send).visibility = View.VISIBLE
        } else {
            findViewById<View>(R.id.ll_connect_layout).visibility = View.VISIBLE
            findViewById<View>(R.id.ll_main_send).visibility = View.GONE
        }
    }

    private fun addContent(content: String) {
        val message = Message()
        message.what = messageAddContent
        message.data.putString(messageDataKey, content)
        mHandler.sendMessage(message)
    }
//
//    override fun authVersionCallback(authVersion: DeviceMessagePayload.AuthVersion) {
//        addContent(getString(R.string.auth_version_info, authVersion.authVer.toString()))
//    }
//
//    override fun deviceVersionCallback(deviceVersion: DeviceMessagePayload.DeviceVersion) {
//        addContent(getString(R.string.device_version_info, deviceVersion.firmwareVersion))
//
//    }
//
//    override fun deviceIdCallback(deviceID: DeviceMessagePayload.DeviceID) {
//        addContent(getString(R.string.device_id_info, deviceID.modelID, deviceID.vendorID))
//    }

    override fun outputBytes(data: ByteArray) {
        SPPUtil.getInstance(this).sendData(data)
    }

    override fun print(content: String) {
        addContent(content)
    }

    private fun decode2PCM() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val opus = Opus()
        opus.decoderInit(Constants.SampleRate._48000(), Constants.Channels.mono())
        runBlocking {
            var fileData = ByteArray(0)
            var pcmData: ByteArray? = ByteArray(0)
            val readFile = async {
                val inputStream = resources.assets.open("sample.opus")
                var reading = true
                while (reading) {
                    val buffer = ByteArray(2048)
                    val readLength = inputStream.read(buffer)
                    if (readLength > 0) {
                        fileData += buffer.copyOfRange(0, readLength)
                    } else {
                        reading = false
                    }
                }
            }
            val pcmDataRequest = async {
                pcmData = opus.decode(fileData, Constants.FrameSize._120())
                Log.d("Charles","opus finish")
                println("来啦")
            }
            val outputToFile = async {
                if(pcmData==null){
                    return@async
                }
                val file = File("/sdcard", "test23.pcm")
                if (!file.exists()) {
                    file.createNewFile()
                }
                val output = FileOutputStream(file, false)
                output.write(pcmData)
                output.close()
            }

            withContext(Dispatchers.IO) {
                readFile.await()
                pcmDataRequest.await()
                outputToFile.await()
                Log.d("Charles", "Finished")
            }

        }

    }
}