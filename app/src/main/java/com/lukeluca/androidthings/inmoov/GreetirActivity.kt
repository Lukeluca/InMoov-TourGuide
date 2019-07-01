package com.lukeluca.androidthings.inmoov

import android.graphics.Bitmap
import android.media.ImageReader
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import android.graphics.drawable.BitmapDrawable
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.HandlerThread
import com.google.firebase.ml.vision.common.FirebaseVisionPoint
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import com.google.firebase.storage.FirebaseStorage
import com.lukeluca.androidthings.inmoov.classifier.TensorFlowEmployeeClassifier
import com.lukeluca.androidthings.inmoov.personality.RandomUnknownPersonPhrases
import com.lukeluca.androidthings.inmoov.state.InMoovContext
import com.lukeluca.androidthings.inmoov.state.SentryState
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


/**
 * Greetir activity. To use a pre-defined model of known people, to greet those it knows.
 *
 * Built on top of a camera & speech base activity, which also has Arduino connections
 *
 *
 *
 */
public class GreetirActivity : CameraSpeechBaseActivity(), ImageReader.OnImageAvailableListener {

    private val TAG = "GreetirActivity"

    var detector: FirebaseVisionFaceDetector? = null
    var options: FirebaseVisionFaceDetectorOptions? = null

    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null


    private var mImage: ImageView? = null
    private var mImageFace: ImageView? = null
    private var mArduinoText: TextView? = null
    private val isFollowFaceOn = true // turn face if it's not pointing at the person
    private val isPreviewOn: Boolean = true

    private var robotContext : InMoovContext = InMoovContext(this)

    lateinit var storage: FirebaseStorage
    private var timeLastUploaded = 0L

    private var mTensorFlowClassifier: TensorFlowEmployeeClassifier? = null
    private var mRandomUnknownPersonPhrase = RandomUnknownPersonPhrases()

    val p = Paint()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sentry)

        val close: View = findViewById(R.id.btn_close)
        close.setOnClickListener { finish() }

        mImage = findViewById(R.id.iv_camera)
        mImageFace = findViewById(R.id.ivFace)
        mArduinoText = findViewById(R.id.tv_arduino)

        robotContext.setState(SentryState());

        storage = FirebaseStorage.getInstance()

        p.style = Paint.Style.STROKE // don't fill
        p.isAntiAlias = true
        p.isFilterBitmap = true
        p.isDither = true
        p.color = Color.GREEN

        options = FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.1f) // default is 0.1
                .enableTracking()
                .build()

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options!!)

        mBackgroundThread = HandlerThread("BackgroundThread")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.getLooper())
        mBackgroundHandler!!.post(mInitializeOnBackground)
    }


    // Anything that needs to be initialized on startup
    private val mInitializeOnBackground = Runnable {
        try {
            mTensorFlowClassifier = TensorFlowEmployeeClassifier(this@GreetirActivity,
                    224, 224)

        } catch (e: IOException) {
            throw IllegalStateException("Cannot initialize TFLite Employee Classifier", e)
        }

    }

    private var hasBeckoned = false

    private fun onImageCapture(image: FirebaseVisionImage, bitmap: Bitmap) {
        val result = detector?.detectInImage(image)!!
                .addOnSuccessListener {
                    faces ->

                    if (faces.size > 0) {

                        val c = Canvas(bitmap)
                        var trackingFaceAlready = false

                        for (face in faces) {

                            val boundingBox = face.boundingBox

                            val faceBitmap = FaceExtractor().getFaceBitmap(bitmap, face)
                            if (faceBitmap != null) {
                                if (isPreviewOn) mImageFace?.setImageBitmap(faceBitmap)


                                // Send faceBitmap to second model to be evaluated
                                Log.i(TAG, "face size: " + faceBitmap.width.toString() + " x " + faceBitmap.height.toString())
                                // RS2019 hack, not sure why this breaks it, so I'll break it instead
                                if (faceBitmap.width > 224 || faceBitmap.height > 224) {
                                    break
                                }

                                val results = mTensorFlowClassifier?.doRecognize(faceBitmap)
                                Log.d(TAG, "Got the following results from Tensorflow: " + results!!)

                                runOnUiThread {
                                    if (results == null || results!!.isEmpty()) {
                                        mArduinoText?.setText("I don't understand what I see")
                                    } else {
                                        val sb = StringBuilder()
                                        val it = results!!.iterator()
                                        var counter = 0
                                        while (it.hasNext()) {
                                            val r = it.next()
                                            sb.append(r.getTitle())
                                            counter++
                                            if (counter < results!!.size - 1) {
                                                sb.append(", ")
                                            } else if (counter == results!!.size - 1) {
                                                sb.append(" or ")
                                            }
                                        }
                                        mArduinoText?.setText(sb.toString())
                                    }
                                }

                                val SINGLE_ANSWER_CONFIDENCE_THRESHOLD = 0.6f

                                if (results.isEmpty()) {
                                    speakText("I don't know who you are.")
                                } else {
                                    val it = results.iterator()

                                    val first = if (it.hasNext()) it.next() else null
                                    val second = if (it.hasNext()) it.next() else null
                                    if (results.size == 1 || first!!.confidence > SINGLE_ANSWER_CONFIDENCE_THRESHOLD) {
                                        speakText("I'm pretty sure you are " + first!!.title, "definitely")
                                    } else {
                                        if (first!!.confidence > (SINGLE_ANSWER_CONFIDENCE_THRESHOLD / 2)) {
                                            speakText(String.format(Locale.getDefault(), "I think you might be %s",
                                                    first!!.title), "maybe")
                                        } else {
                                            speakText(mRandomUnknownPersonPhrase.getRandomPhrase())
                                        }
                                    }
                                }


                            }

                            Log.i(TAG, "face height: " + face.boundingBox.height().toString())

                            val leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)
                            if (leftEye != null) {
                                val point : FirebaseVisionPoint = leftEye.position
                                c.drawPoint(point.x, point.y, p)

                                 if (!trackingFaceAlready) {

                                     if (isFollowFaceOn) {
                                         val centerW = c.width / 2
                                         val centerH = c.height / 2
                                         if (point.x < centerW - 10) {
                                             sendCommand("HH", 5, true)
                                         }
                                         if (point.x > centerW + 10) {
                                             sendCommand("HH", -5, true)
                                         }
                                         // y is down from the top, so if it's less than center, look up
                                         if (point.y < centerH - 5) {
                                             sendCommand( "HV", 5, true)
                                         }
                                         // if greater, look down
                                         if (point.y > centerH + 5) {
                                             sendCommand( "HV", -5, true)
                                         }
                                     }
                                     trackingFaceAlready = true
                                 }
                            }

                            c.drawRect(boundingBox.left.toFloat(), boundingBox.top.toFloat(), boundingBox.right.toFloat(),
                                    boundingBox.bottom.toFloat(), p)


                        }

                        if (isPreviewOn)
                            mImage?.setImageBitmap(bitmap)

                    }

                    robotContext.onDetectFaces(faces)

                    captureImage()
                }
                .addOnFailureListener{
                    // If we don't see something, just keep waiting.
                    Log.i(TAG, "error with detection");

                    captureImage()
                }
                .addOnCompleteListener {

                }
    }

    override fun onSpeakStart(utteranceId: String) {
        robotContext.onSpeakStart(utteranceId)
    }

    override fun onSpeakDone(utteranceId: String) {

        robotContext.onSpeakDone(utteranceId)
    }

    override fun onBitmapAvailable(bitmap: Bitmap) {
        if (isPreviewOn) runOnUiThread { mImage!!.setImageBitmap(bitmap) }

        bitmap?.let { FirebaseVisionImage.fromBitmap(it) }?.let { onImageCapture(it, bitmap) };
    }

    override fun onArduinoResponse(response: String) {
        //runOnUiThread { mArduinoText?.setText(response) }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
