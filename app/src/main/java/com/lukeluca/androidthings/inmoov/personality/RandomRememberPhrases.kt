package com.lukeluca.androidthings.inmoov.personality

import java.util.*

class RandomRememberPhrases {

    private val PHRASES = arrayOf(
            "I will remember you",
            "You are unforgettable",
            "I've added you to my list",
            "I'll never forget you",
            "Database entry added",
            "That is an unforgettable face"
    )

    private val SECONDS = 1000;

    private val TIME_BETWEEN_PHRASES = 30*SECONDS;

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