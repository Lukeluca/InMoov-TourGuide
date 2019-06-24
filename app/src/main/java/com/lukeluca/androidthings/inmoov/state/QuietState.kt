package com.lukeluca.androidthings.inmoov.state

import com.google.firebase.ml.vision.face.FirebaseVisionFace

class QuietState : State {

    private val TAG = this.javaClass.name
    private var timeSinceQuietStarted : Long = System.currentTimeMillis()


    override fun onDetectFaces(c: InMoovContext, faces: List<FirebaseVisionFace>) {
        if (System.currentTimeMillis() > timeSinceQuietStarted + 5000)
            c.setState(WelcomeState())
    }

}