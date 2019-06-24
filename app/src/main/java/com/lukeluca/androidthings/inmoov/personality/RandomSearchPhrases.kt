package com.lukeluca.androidthings.inmoov.personality

import java.util.*

class RandomSearchPhrases {

    private val PHRASES = arrayOf(
            "Hello?",
            "What?",
            "Hi there"
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