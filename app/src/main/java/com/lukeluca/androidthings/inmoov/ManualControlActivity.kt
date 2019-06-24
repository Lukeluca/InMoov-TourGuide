package com.lukeluca.androidthings.inmoov

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.things.pio.UartDevice
import com.google.android.things.pio.UartDeviceCallback
import java.io.IOException

private val TAG = ManualControlActivity::class.java.simpleName

class ManualControlActivity : Activity() {

    private var arduino: Arduino? = null
    private var mMessageText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_manual_controls)

        arduino = Arduino()
        mMessageText = findViewById(R.id.message)

//        val manager = PeripheralManager.getInstance()
//        val deviceList: List<String> = manager.uartDeviceList
//        if (deviceList.isEmpty()) {
//            Log.i(TAG, "No UART port available on this device.")
//        } else {
//            Log.i(TAG, "List of available devices: $deviceList")
//        }

        val close: View = findViewById(R.id.btn_close)
        close.setOnClickListener { finish() }

        val head_left_full: View = findViewById(R.id.button_full_left)
        head_left_full.setOnClickListener { sendCommand("HH", 100, false) }

        val left: View = findViewById(R.id.button_left)
        left.setOnClickListener { turnHeadLeft(it) }

        val center:View = findViewById(R.id.button_center)
        center.setOnClickListener { centerHead(it) }

        val right: View = findViewById(R.id.button_right)
        right.setOnClickListener { turnHeadRight(it) }

        val head_right_full: View = findViewById(R.id.button_full_right)
        head_right_full.setOnClickListener { sendCommand("HH", 0, false) }

        val mouth_close:View = findViewById(R.id.button_mouth_closed)
        mouth_close.setOnClickListener { sendCommand( "HM", 0, relative = false) }

        val mouth_open:View = findViewById(R.id.button_mouth_open)
        mouth_open.setOnClickListener { sendCommand( "HM", 100, false) }

        val arm_right_twist_left_full: View = findViewById(R.id.button_right_arm_left_full)
        arm_right_twist_left_full.setOnClickListener { sendCommand( "RB", 0, false) }

        val arm_right_twist_left: View = findViewById(R.id.button_right_arm_left)
        arm_right_twist_left.setOnClickListener { sendCommand( "RB", -10, true) }

        val arm_right_twist_center: View = findViewById(R.id.button_right_arm_center)
        arm_right_twist_center.setOnClickListener { sendCommand( "RB", 50, false) }

        val arm_right_twist_right: View = findViewById(R.id.button_right_arm_right)
        arm_right_twist_right.setOnClickListener { sendCommand( "RB", 10, true) }

        val arm_right_twist_right_full: View = findViewById(R.id.button_right_arm_right_full)
        arm_right_twist_right_full.setOnClickListener { sendCommand( "RB", 100, false) }


        val thumb_close:View = findViewById(R.id.button_thumb_close)
        thumb_close.setOnClickListener { sendCommand("RT", 0, false) }

        val thumb_open:View = findViewById(R.id.button_thumb_open)
        thumb_open.setOnClickListener { sendCommand("RT", 100, false) }

        val right_index_close:View = findViewById(R.id.button_index_close)
        right_index_close.setOnClickListener { sendCommand("RI", 0, false) }

        val right_index_open:View = findViewById(R.id.button_index_open)
        right_index_open.setOnClickListener { sendCommand("RI", 100, false) }

    }

    private fun turnHeadLeft(it: View?) {
        sendCommand("HH", +10, true)
    }

    private fun turnHeadRight(it: View?) {
        sendCommand("HH", -10, true)
    }

    private fun centerHead(it: View?) {
        sendCommand("HH", 50, false)
    }

    override fun onStart() {
        super.onStart()
        // Begin listening for interrupt events
        arduino?.registerUartDeviceCallback(mUartCallback)
    }

    override fun onStop() {
        super.onStop()
        // Interrupt events no longer necessary
        arduino?.unregisterUartDeviceCallback(mUartCallback)
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            arduino?.close()
            arduino = null
        } catch (e: IOException) {
            Log.w(TAG, "Unable to close UART device", e)
        }
    }

    public fun sendCommand(servoCode: String, value: Int, relative: Boolean) {
        try {
            var padding = "";
            if (relative && value > 0) padding = "+"
            arduino?.write(servoCode + padding + value.toString() + '\n')
            Thread.sleep(100)
            val response = arduino?.read()
            runOnUiThread { mMessageText?.setText(response) }
        } catch (e: InterruptedException) {
            throw IllegalStateException("Cannot wait for thread", e)
        }

    }

    @Throws(IOException::class)
    fun readUartBuffer(uart: UartDevice) {
        // Maximum amount of data to read at one time
        val maxCount = 128

        uart.apply {
            ByteArray(maxCount).also { buffer ->
                var count: Int = read(buffer, buffer.size)
                while (count > 0) {
                    Log.d(TAG, "Read $count bytes from peripheral")
                    count = read(buffer, buffer.size)
                }
            }
        }
    }

    private val mUartCallback = object : UartDeviceCallback {
        override fun onUartDeviceDataAvailable(uart: UartDevice): Boolean {
            // Read available data from the UART device
            try {
                readUartBuffer(uart)
            } catch (e: IOException) {
                Log.w(TAG, "Unable to access UART device", e)
            }

            // Continue listening for more interrupts
            return true
        }

        override fun onUartDeviceError(uart: UartDevice?, error: Int) {
            Log.w(TAG, "$uart: Error event $error")
        }
    }
}
