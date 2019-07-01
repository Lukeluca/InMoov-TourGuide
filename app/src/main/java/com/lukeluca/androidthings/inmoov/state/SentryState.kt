package com.lukeluca.androidthings.inmoov.state

import android.util.Log
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.lukeluca.androidthings.inmoov.personality.RandomLonelyPhrases
import com.lukeluca.androidthings.inmoov.personality.RandomSearchPhrases
import com.lukeluca.androidthings.inmoov.personality.RandomSmilePhrases

class SentryState : State {

    private val TAG = this.javaClass.name
    private var timeLastWelcomed = 0L
    private var timeLastSawSomeone = 0L
    private var timeLastNotifiedOfSmiling = 0L
    private var hasPromptedSmile = false
    private var hasSeenSomeone = false
    private var searchPhrases = RandomSearchPhrases()
    private var lonelyPhrases = RandomLonelyPhrases()
    private var smilePhrases = RandomSmilePhrases()

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
                    if (timeLastWelcomed < System.currentTimeMillis() - 24000L) {
                        c.speakText("Hello there.", "Welcome")
                        timeLastWelcomed = System.currentTimeMillis()
                    }

                    if (face.smilingProbability > 0.6 && timeLastNotifiedOfSmiling < System.currentTimeMillis() - 10000L) {
                        Log.i(TAG, "Saw a big smile")

                        val randomPhrase = smilePhrases.getRandomPhrase()
                        if (randomPhrase.isNotEmpty()) {
                            c.speakText(randomPhrase, "big smile")
                        }

                        c.sendCommand("RT", 100, false)
                    }
                }

                timeLastSawSomeone = System.currentTimeMillis()

            }

        } else { // size = 0 === don't see anyone
            if (hasSeenSomeone) {
                c.sendCommand("RT", 0, false)

                if (timeLastSawSomeone < System.currentTimeMillis() - 10000L) {
                    val randomPhrase = lonelyPhrases.getRandomPhrase()
                    if (randomPhrase.isNotEmpty()) {
                        c.speakText(randomPhrase, "lonely")
                    }

                    hasSeenSomeone = false
                }
            }
        }

    }

}