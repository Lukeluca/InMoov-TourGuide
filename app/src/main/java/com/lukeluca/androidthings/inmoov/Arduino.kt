package com.lukeluca.androidthings.inmoov

import android.util.Log
import com.google.android.things.pio.PeripheralManager
import com.google.android.things.pio.UartDevice
import com.google.android.things.pio.UartDeviceCallback

class Arduino(uartDevice: String = "UART6"): AutoCloseable {
    val peripheralManager by lazy {
        PeripheralManager.getInstance()
    }

    private val TAG = "Arduino"
    private val uart: UartDevice by lazy {
        peripheralManager.openUartDevice("UART6").apply {
            // Configure the UART
            setBaudrate(BoardDefaults.BAUD_RATE)
            setDataSize(BoardDefaults.DATA_BITS)
            setParity(UartDevice.PARITY_NONE)
            setStopBits(BoardDefaults.STOP_BITS)
        }
    }

    fun read(): String {
        val maxCount = 8
        val buffer = ByteArray(maxCount)
        var output = ""
        do {
            val count = uart.read(buffer, buffer.size)
            output += buffer.toReadableString()
            if(count == 0) break
            Log.d(TAG, "Read ${buffer.toReadableString()} $count bytes from peripheral")
        } while (true)
        return output
    }

    private fun ByteArray.toReadableString() = filter { it > 0.toByte() }
            .joinToString(separator = "") { it.toChar().toString() }


    fun write(value: String) {
        val count = uart.run {
            value.toByteArray().let { buffer ->
            write(buffer, buffer.size)
        }
        }
        Log.d(TAG, "Wrote $count bytes to peripheral")
    }

    override fun close() {
        uart.close()
    }

    fun registerUartDeviceCallback(callback: UartDeviceCallback) {
        uart.registerUartDeviceCallback(callback)
    }

    fun unregisterUartDeviceCallback(callback: UartDeviceCallback) {
        uart.unregisterUartDeviceCallback(callback)
    }
}