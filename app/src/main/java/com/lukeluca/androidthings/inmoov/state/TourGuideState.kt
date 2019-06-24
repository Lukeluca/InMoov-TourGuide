package com.lukeluca.androidthings.inmoov.state

import com.google.firebase.ml.vision.face.FirebaseVisionFace

class TourGuideState : State {

    private val TAG = this.javaClass.name

    private val tourText = arrayOf(
            "Hello, and welcome to Bottle Rocket",
            "This is the garage, where many people work in their off time on personal projects.",
            "Behind you, you can see our 3D printers.",
            "Those were used to print my entire body, except for the servo motors, and some bolts.",
            "My Maker, Luke Wallace, has worked on me for several years.",
            "He worked with a team of people during Rocket Science to create my body.     ",
            "You may now move on with your tour, please come up and smile to hear this message again"
    )

    private val tourCommands = arrayOf(
            "RB100",
            "RT100 RI100 RM100 RR100 RP100 RB100",
            "RB50 RT0 RI100 RM0 RR0 RP0", //point
            "RB100",
            "RT100 RI100 RM100 RR100 RP100",
            "",
            "RB25 RT0 RI0 RM0 RR0 RP0"
    )

    var givenTour = false
    override fun onDetectFaces(c: InMoovContext, faces: List<FirebaseVisionFace>) {

        if (faces.size > 0) {
            if (!givenTour) {
                for (text in tourText) {
                    c.speakText(text, text)
                }
            }
            givenTour = true
        }

    }

    override fun onSpeakStart(c : InMoovContext, utteranceId: String) {
        // as we start saying something, move to the next position.
        val i = tourText.indexOf(utteranceId)
        if (i >= 0) {
            c.sendRawCommand(tourCommands[i])
        }
    }

    override fun onSpeakDone(c: InMoovContext, utteranceId: String) {

        val i = tourText.indexOf(utteranceId)
        if (i == tourText.size -1) {
            c.setState(QuietState())
        }
    }


}