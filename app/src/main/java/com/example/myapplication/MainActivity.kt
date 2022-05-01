package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.impl.SPPInterface
import com.example.myapplication.util.SPPUtil

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity(), View.OnClickListener, SPPInterface {
    private lateinit var mTvContent: TextView
    private lateinit var mEdName: EditText
    private lateinit var mEdContent: EditText
    private lateinit var deviceReceiver: DeviceReceiver
    private var isStarting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initRegister()
        initListen()
    }

    private fun initListen() {
        findViewById<Button>(R.id.btn_main_trigger).setOnClickListener(this)
        findViewById<Button>(R.id.btn_main_send).setOnClickListener(this)
        SPPUtil.getInstance(this).setListen(this)
    }

    private fun initView() {
        mTvContent = findViewById(R.id.tv_main_content)
        mTvContent.setText(R.string.wait_for_start)
        mEdName = findViewById(R.id.ed_main_name)
        mEdContent = findViewById(R.id.ed_main_content)
    }

    private fun initRegister() {
        val foundFilter = IntentFilter()
        foundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        foundFilter.addAction(BluetoothDevice.ACTION_FOUND)
        deviceReceiver = DeviceReceiver()
        registerReceiver(deviceReceiver, foundFilter)
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), 1024
        )
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_main_send -> sendText()
            R.id.btn_main_trigger -> clickTrigger()
        }
    }

    // start or end
    private fun clickTrigger() {
        val enable = SPPUtil.getInstance(this).enableBluetooth()
        if (!enable) {
            return
        }
        findViewById<Button>(R.id.btn_main_trigger).visibility = View.GONE
        findViewById<View>(R.id.row_main_send).visibility = View.VISIBLE
        mEdName.visibility = View.GONE

        if (!isStarting) {
            SPPUtil.getInstance(this).startScan()
        }
    }

    private fun sendText() {
        val content: String = mEdContent.text.toString()
        mEdContent.text.clear()
        SPPUtil.getInstance(this).sendData(content)
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
                    if (device?.name != null) {
                        mTvContent.text = String.format(getString(R.string.found_hint), device.name)
                        if (device.name.contains(mEdName.text)) {
                            SPPUtil.getInstance(this@MainActivity).startConnect(device)
                            SPPUtil.getInstance(this@MainActivity).stopScan()
                            mTvContent.text = getString(R.string.connecting)
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                }
            }
        }
    }

    // Get from SPP Data
    override fun inputData(content: String) {
        mTvContent.text = String.format(getString(R.string.latest_data), content)
    }

    override fun connected() {
        mTvContent.text = getString(R.string.connected)
    }

    override fun connectFail() {
        mTvContent.text = getString(R.string.connect_fail)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(deviceReceiver)
        SPPUtil.getInstance(this).disconnect()
    }

}