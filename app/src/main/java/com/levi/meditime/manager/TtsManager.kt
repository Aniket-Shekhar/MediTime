package com.levi.meditime.manager

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                isInitialized = true
            } else {
                Log.e("TtsManager", "Initialization failed")
            }
        }
    }

    fun speak(text: String, langCode: String = "en", repeatCount: Int = 1) {
        if (!isInitialized) return
        
        val locale = Locale.forLanguageTag(langCode)
        tts?.language = locale

        repeat(repeatCount) {
            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
    }
}
