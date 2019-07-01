package com.lukeluca.androidthings.inmoov.personality

import java.util.*

class RandomUnknownPersonPhrases {

    private val PHRASES = arrayOf(
            "You seem nice, but I don't think I know you",
            "I wish I knew who you were",
            "You're very important, aren't you",
            "I think we may have met, but I don't remember",
            "I am very bad at names, but you look familiar",
            "Is that Calvin behind you?",
            "Are you new here? I don't think we've met.",
            "Maybe I'll remember you next time",
            "Tell Luke that I should know who you are",
            "I would feel bad saying your name out loud",
            "I'm not sure I know how to pronounce your name"
    )

    private val SECONDS = 1000;

    private val TIME_BETWEEN_PHRASES = 12*SECONDS;

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