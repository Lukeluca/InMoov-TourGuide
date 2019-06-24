package com.lukeluca.androidthings.inmoov.state

import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.lukeluca.androidthings.inmoov.CameraSpeechBaseActivity

// Keeps track of the context of a inmoov state
class InMoovContext {
    private lateinit var myState: State
    private lateinit var myActivity: CameraSpeechBaseActivity // Will be used for calling motors and speech

    constructor(activity: CameraSpeechBaseActivity) {
        myActivity = activity
        setState(WelcomeState())
    }

    public fun setState(s: State) {
        myState = s
    }

    public fun onDetectFaces(faces: List<FirebaseVisionFace>) {
        myState.onDetectFaces(this, faces)
    }

    fun onSpeakStart(utteranceId: String) {
        myState.onSpeakStart(this, utteranceId)
    }

    fun onSpeakDone(utteranceId: String) {
        myState.onSpeakDone(this, utteranceId)
    }

    // methods to call back to Activity to perform actions
    fun speakText(string: String, utteranceId: String) {
        myActivity.speakText(string, utteranceId)
    }

    fun sendCommend(servoCode: String, value: Int, relative: Boolean) {
        myActivity.sendCommand(servoCode, value, relative)
    }

    fun sendRawCommand(servoCodes: String) {
        myActivity.sendRawCommand(servoCodes)
    }


}