/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lukeluca.androidthings.inmoov

import android.os.Build

object BoardDefaults {
    /** supported device names */
    private val DEVICE_RPI3 = "rpi3"
    private val DEVICE_IMX6UL_PICO = "imx6ul_pico"
    private val DEVICE_IMX7D_PICO = "imx7d_pico"

    /** UART Configuration Parameters */
    const val BAUD_RATE = 115200
    const val DATA_BITS = 8
    const val STOP_BITS = 1
    const val CHUNK_SIZE = 512

    val uartName = when (Build.DEVICE) {
        DEVICE_RPI3 -> "UART0"
        DEVICE_IMX6UL_PICO -> "UART3"
        DEVICE_IMX7D_PICO -> "UART6"
        else -> throw IllegalStateException("Unknown Build.DEVICE ${Build.DEVICE}")
    }

    /**
     * Return the GPIO pin that the LED is connected on.
     */
    val gpioForLED: String
        get() {
            when (Build.DEVICE) {
                DEVICE_RPI3 -> return "BCM6"
                DEVICE_IMX6UL_PICO -> return "GPIO4_IO20"
                DEVICE_IMX7D_PICO -> return "GPIO2_IO02"
                else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
            }
        }

    /**
     * Return the GPIO pin that the Button is connected on.
     */
    val gpioForButton: String
        get() {
            when (Build.DEVICE) {
                DEVICE_RPI3 -> return "BCM21"
                DEVICE_IMX6UL_PICO -> return "GPIO4_IO20"
                DEVICE_IMX7D_PICO -> return "GPIO6_IO14"
                else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
            }
        }

}
