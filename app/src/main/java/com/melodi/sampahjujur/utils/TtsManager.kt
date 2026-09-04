package com.melodi.sampahjujur.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Utility helper for Android native Text-To-Speech (TTS).
 * Vocalizes current prices, weight, and estimated value in English, Hindi, or Marathi.
 */
class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setLanguage(Locale.getDefault())
        } else {
            Log.e("TtsManager", "TextToSpeech initialization failed with status: $status")
        }
    }

    fun setLanguage(locale: Locale) {
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w("TtsManager", "Requested locale $locale not supported, falling back to English")
            tts?.setLanguage(Locale.ENGLISH)
        }
    }

    /**
     * Speaks the price details and estimated value.
     */
    fun speakPrice(material: String, pricePerKg: Double, weight: Double = 0.0, estimatedValue: Double = 0.0) {
        if (!isInitialized || tts == null) {
            Log.w("TtsManager", "TTS not initialized yet")
            return
        }

        val textToSpeak = if (weight > 0 && estimatedValue > 0) {
            "$material current buying price is ${pricePerKg.toInt()} rupees per kg. For $weight kg, estimated value is ${estimatedValue.toInt()} rupees."
        } else {
            "$material current buying price is ${pricePerKg.toInt()} rupees per kg."
        }

        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "price_speak_id")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
