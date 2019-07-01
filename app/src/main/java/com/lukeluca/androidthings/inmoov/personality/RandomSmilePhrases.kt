package com.lukeluca.androidthings.inmoov.personality

import java.util.*

class RandomSmilePhrases {

    private val PHRASES = arrayOf(
            "That is a great smile",
            "Thank you for smiling",
            "I like to see you smile"
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