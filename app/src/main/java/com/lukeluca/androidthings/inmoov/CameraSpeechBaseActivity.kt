package com.lukeluca.androidthings.inmoov

import android.app.Activity
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.util.Size
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * base Activity to handle image capture and speech functionality. What it does with the image
 * (likely image recognition) will be left up to the implementation class
 *
 *
 */
public abstract class CameraSpeechBaseActivity : Activity(), ImageReader.OnImageAvailableListener {

    private val TAG = "CameraSpeechBaseActivity"


    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null

    // is the system ready for capturing an image
    private val mReady = AtomicBoolean(false)
    private var mCameraHandler: CameraHandler? = null
    private var mImagePreprocessor: ImagePreprocessor? = null
    // Matches the images used to train the TensorFlow model
    protected val MODEL_IMAGE_SIZE = Size(480, 480)


    private var mTtsEngine: TextToSpeech? = null
    private var mTtsSpeaker: TtsSpeaker? = null

    private var arduino: Arduino? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        arduino = Arduino()

        mBackgroundThread = HandlerThread("BackgroundThread")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread?.getLooper())
        mBackgroundHandler!!.post(mInitializeOnBackground)
    }


    private val mInitializeOnBackground = Runnable {
        mCameraHandler = CameraHandler.getInstance()
        try {
            mCameraHandler!!.initializeCamera(this,
                    mBackgroundHandler, MODEL_IMAGE_SIZE, this)
            CameraHandler.dumpFormatInfo(this)
        } catch (e: CameraAccessException) {
            throw RuntimeException(e)
        }

        val cameraCaptureSize = mCameraHandler!!.getImageDimensions()

        mImagePreprocessor = ImagePreprocessor(cameraCaptureSize.width, cameraCaptureSize.height,
                MODEL_IMAGE_SIZE.getWidth(), MODEL_IMAGE_SIZE.getHeight())

        mTtsSpeaker = TtsSpeaker()
        mTtsSpeaker!!.setHasSenseOfHumor(false)
        mTtsEngine = TextToSpeech(this,
                TextToSpeech.OnInitListener { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        mTtsEngine!!.setLanguage(Locale.US)
                        mTtsEngine!!.setOnUtteranceProgressListener(utteranceListener)
                        mTtsSpeaker!!.speakReady(mTtsEngine)
                    } else {
                        Log.w(TAG, "Could not open TTS Engine (onInit status=" + status
                                + "). Ignoring text to speech")
                        mTtsEngine = null
                    }
                })

        try {

        } catch (e: IOException) {
            throw IllegalStateException("Cannot initialize TFLite Classifier", e)
        }

        captureImage()
    }

    protected fun captureImage() {
        mBackgroundHandler?.post(mBackgroundClickHandler)
    }

    protected fun speakText(text : String) {
        mTtsSpeaker?.speakText(mTtsEngine, text)
    }


    public fun speakText(text : String, utteranceId: String) {
        mTtsSpeaker?.speakText(mTtsEngine, text, utteranceId)
    }

    protected fun speakPause(count : Int) {
        for (i in 0..count) {
            speakText( " ")
        }
    }

    private val mBackgroundClickHandler = Runnable {
        Log.d(TAG, "taking picture")
        mCameraHandler?.takePicture()
    }

    protected val utteranceListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String) {
            setReady(false)
            onSpeakStart(utteranceId)
        }

        override fun onDone(utteranceId: String) {
            setReady(true)
            onSpeakDone(utteranceId)
        }

        override fun onError(utteranceId: String) {
            setReady(true)
        }
    }


    /**
     * Mark the system as ready for a new image capture
     */
    private fun setReady(ready: Boolean) {
        mReady.set(ready)
    }

    // Called when a speech is started
    abstract fun onSpeakStart(utteranceId: String)

    // Called when a speech is finished
    abstract fun onSpeakDone(utteranceId: String)

    override fun onImageAvailable(reader: ImageReader?) {
        Log.i(TAG, "onImageAvailable (found Image)")
        var bitmap: Bitmap? = null
        reader?.acquireLatestImage().use { image -> bitmap = mImagePreprocessor?.preprocessImage(image) }

        onBitmapAvailable(bitmap!!)

    }

    abstract fun onBitmapAvailable(bitmap: Bitmap)

    public fun sendCommand(servoCode: String, value: Int, relative: Boolean) {
        try {
            var padding = "";
            if (relative && value > 0) padding = "+"
            arduino?.write(servoCode + padding + value.toString() + '\n')
            Thread.sleep(100)
            val response = arduino?.read()
            if (response != null) {
                onArduinoResponse(response)
            }
        } catch (e: InterruptedException) {
            throw IllegalStateException("Cannot wait for thread", e)
        }
    }

    public fun sendRawCommand(servoCodes: String) {
        try {
            arduino?.write(servoCodes + '\n')
            Thread.sleep(100)
            val response = arduino?.read()
            if (response != null) {
                onArduinoResponse(response)
            }
        } catch (e: InterruptedException) {
            throw IllegalStateException("Cannot wait for thread", e)
        }
    }

    abstract fun onArduinoResponse(response: String)

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (mBackgroundThread != null) mBackgroundThread?.quit()
        } catch (t: Throwable) {
            // close quietly
        }

        mBackgroundThread = null
        mBackgroundHandler = null

        try {
            if (mCameraHandler != null) mCameraHandler?.shutDown()
        } catch (t: Throwable) {
            // close quietly
        }

        mTtsEngine?.stop()
        mTtsEngine?.shutdown()
    }


}
