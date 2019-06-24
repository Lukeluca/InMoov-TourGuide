package com.lukeluca.androidthings.inmoov

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.android.things.contrib.driver.apa102.Apa102
import java.lang.Exception
import java.util.*


/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For lukeluca, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class ChooserActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooser)


        val manual: View = findViewById(R.id.btn_mode_manual)
        manual.setOnClickListener {
            val intent = Intent(this, ManualControlActivity::class.java)
            startActivity( intent )
        }

        val tour_guide : View = findViewById(R.id.btn_mode_tour_guide)
        tour_guide.setOnClickListener {
            val intent = Intent(this, TourGuideActivity::class.java)
            startActivity( intent )
        }

        val image_classifier: View = findViewById(R.id.btn_image_classifier)
        image_classifier.setOnClickListener {
            val intent = Intent ( this, ImageClassifierActivity::class.java)
            startActivity( intent )
        }

        try {
            val on = false
            val ledstrip = RainbowHat.openLedStrip()
            ledstrip.setBrightness(1)
            val rainbow = IntArray(RainbowHat.LEDSTRIP_LENGTH)
            for (i in rainbow.indices) {
                rainbow[i] = if (!on) 0 else Color.HSVToColor(254, arrayOf(i * 360f / RainbowHat.LEDSTRIP_LENGTH , 1f, 1f ).toFloatArray() )
            }
            ledstrip.write(rainbow)
            // Close the device when done.
            ledstrip.close()
        } catch (e : Exception) {

        }
    }
}
