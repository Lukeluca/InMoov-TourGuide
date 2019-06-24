package com.lukeluca.androidthings.inmoov.state

import android.util.Log
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.lukeluca.androidthings.inmoov.personality.RandomLonelyPhrases
import com.lukeluca.androidthings.inmoov.personality.RandomSearchPhrases

class WelcomeState : State {

    private val TAG = this.javaClass.name
    private var timeLastWelcomed = 0L
    private var hasPromptedSmile = false
    private var hasSeenSomeone = false
    private var searchPhrases = RandomSearchPhrases()
    private var lonelyPhrases = RandomLonelyPhrases()

    override fun onDetectFaces(c: InMoovContext, faces: List<FirebaseVisionFace>) {

        if (faces.size > 0) {

            hasSeenSomeone = true

            for (face in faces) {

                if (face.boundingBox.height() in 1..39) {
                    val randomPhrase = searchPhrases.getRandomPhrase()
                    if (randomPhrase.isNotEmpty()) {
                        c.speakText(randomPhrase, "searching")
                    }
                }

                if (face.boundingBox.height() > 39) {
                    if (timeLastWelcomed < System.currentTimeMillis() - 15000L) {
                        c.speakText("Welcome, if you'd like to hear about the garage, just smile.", "Welcome")
                        timeLastWelcomed = System.currentTimeMillis()
                    }

                    if (face.smilingProbability in 0.3..0.6 && !hasPromptedSmile ) {
                        c.speakText("Smile a little more please", "Smile")
                        hasPromptedSmile = true
                    }
                    if (face.smilingProbability > 0.6) {
                        Log.i(TAG, "Starting Tour")

                        c.setState(TourGuideState())
                    }
                }


            }

        } else { // size = 0 === don't see anyone
            if (hasSeenSomeone) {

                val randomPhrase = lonelyPhrases.getRandomPhrase()
                if (randomPhrase.isNotEmpty()) {
                    c.speakText(randomPhrase, "lonely")
                }

                hasSeenSomeone = false
            }
        }

    }

}