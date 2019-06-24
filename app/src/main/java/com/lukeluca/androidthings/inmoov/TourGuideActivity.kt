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
import com.google.firebase.ml.vision.common.FirebaseVisionPoint
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import com.lukeluca.androidthings.inmoov.state.InMoovContext


/**
 * Tour Guide activity.
 *
 * Built on top of a camera & speech base activity, which also has Arduino connections
 *
 *
 */
public class TourGuideActivity : CameraSpeechBaseActivity(), ImageReader.OnImageAvailableListener {

    private val TAG = "TourGuideActivity"

    var detector: FirebaseVisionFaceDetector? = null
    var options: FirebaseVisionFaceDetectorOptions? = null

    private var mImage: ImageView? = null
    private var mArduinoText: TextView? = null
    private val isFollowFaceOn = false // turn face if it's not pointing at the person

    private var robotContext : InMoovContext = InMoovContext(this)

    val p = Paint()

//    private val tourText = arrayOf(
//            "Hello, and welcome to Bottle Rocket",
//            "This is the garage, where many people work in their off time on personal projects.",
//            "Behind you, you can see our 3D printers.",
//            "Those were used to print my entire body, except for the servo motors, and some bolts.",
//            "My Maker, Luke Wallace, has worked on me for several years.",
//            "He worked with a team of people during Rocket Science to create my body.     ",
//            "You may now move on with your tour, please come up and smile to hear this message again"
//    )
//
//    private val tourCommands = arrayOf(
//            "",
//            "RB100 RT100 RI100 RM100 RR100 RP100",
//            "RB50 RT0 RI100 RM0 RR0 RP0", //point
//            "",
//            "RB100 RT100 RI100 RM100 RR100 RP100",
//            "",
//            "RB25 RT0 RI0 RM0 RR0 RP0"
//    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_guide)

        val close: View = findViewById(R.id.btn_close)
        close.setOnClickListener { finish() }

        mImage = findViewById(R.id.iv_camera)
        mArduinoText = findViewById(R.id.tv_arduino)


        p.style = Paint.Style.STROKE // don't fill
        p.isAntiAlias = true
        p.isFilterBitmap = true
        p.isDither = true
        p.color = Color.GREEN

        options = FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.1f) // default is 0.1
                .enableTracking()
                .build()

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options!!)

    }


    // Anything that needs to be initialized on startup
    private val mInitializeOnBackground = Runnable {

    }

    private var hasBeckoned = false

    private fun onImageCapture(image: FirebaseVisionImage) {
        val result = detector?.detectInImage(image)!!
                .addOnSuccessListener {
                    faces ->

                    if (faces.size > 0) {

                        val bitmap = (mImage?.getDrawable() as BitmapDrawable).bitmap

                        val c = Canvas(bitmap)
                        var trackingFaceAlready = false

                        for (face in faces) {

                            Log.i(TAG, "face height: " + face.boundingBox.height().toString())
                            //Log.i(TAG, "Smiling probability: " + face.smilingProbability.toString());

//                            if (face.boundingBox.height() in 1..59) {
//                                if (!hasBeckoned) {
//                                    speakText("Why don't you come a little closer?")
//                                    hasBeckoned = true
//                                }
//                            }

                            val nose = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE)
                            if (nose != null) {
                                val point : FirebaseVisionPoint = nose.position
                                c.drawPoint(point.x, point.y, p)

                                 if (!trackingFaceAlready) {

                                     if (isFollowFaceOn) {
                                         val center = c.width / 2
                                         if (point.x < center - 10) {
                                             sendCommand("HH", 5, true)
                                         }
                                         if (point.x > center + 10) {
                                             sendCommand("HH", -5, true)
                                         }
                                     }
                                     trackingFaceAlready = true
                                 }
                            }

                            val boundingBox = face.boundingBox

                            c.drawRect(boundingBox.left.toFloat(), boundingBox.top.toFloat(), boundingBox.right.toFloat(),
                                    boundingBox.bottom.toFloat(), p)
//
//                            if (face.smilingProbability > 0.6) {
//                                Log.i(TAG, "Starting Tour")
//
//                                startTour()
//
//                                hasBeckoned = false
//
//                                return@addOnSuccessListener;
//                            }
                        }

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
        runOnUiThread { mImage!!.setImageBitmap(bitmap) }

        bitmap?.let { FirebaseVisionImage.fromBitmap(it) }?.let { onImageCapture(it) };
    }

    override fun onArduinoResponse(response: String) {
        runOnUiThread { mArduinoText?.setText(response) }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
