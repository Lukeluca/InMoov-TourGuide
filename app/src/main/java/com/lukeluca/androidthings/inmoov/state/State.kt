package com.lukeluca.androidthings.inmoov.state

import com.google.firebase.ml.vision.face.FirebaseVisionFace

interface State {
    fun onDetectFaces(c : InMoovContext, faces : List<FirebaseVisionFace>)
    fun onSpeakDone(c : InMoovContext, utteranceId: String) {}
    fun onSpeakStart(c : InMoovContext, utteranceId: String) {}
}