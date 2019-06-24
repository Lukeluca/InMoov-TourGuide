package com.lukeluca.androidthings.inmoov.personality

import java.util.*

class RandomLonelyPhrases {

    private val PHRASES = arrayOf(
            "Are you still there?",
            "Target Lost.",
            "Can I help you?",
            "Searching"
    )

    private val SECONDS = 1000;

    private val TIME_BETWEEN_PHRASES = 60*SECONDS;

    private var lastRequestedTime : Long = 0

    public fun getRandomPhrase() : String {
        if (System.currentTimeMillis() > lastRequestedTime + TIME_BETWEEN_PHRASES) {
            lastRequestedTime = System.currentTimeMillis()
            return PHRASES.get(Random(System.currentTimeMillis()).nextInt(PHRASES.size))
        } else {
            return ""
        }
    }

}