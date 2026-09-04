package com.example.smriti.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TextToSpeechHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private val pendingSpeechQueue = mutableListOf<String>()

    init {
        initTts(context.applicationContext)
    }

    private fun initTts(appContext: Context) {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureHinglishVoice()
                isReady = true
                // Flush any speech requested while engine was initializing
                synchronized(pendingSpeechQueue) {
                    pendingSpeechQueue.forEach { speakInternal(it) }
                    pendingSpeechQueue.clear()
                }
            } else {
                Log.e("TextToSpeechHelper", "TTS Initialization failed with status: $status")
            }
        }
    }

    private fun configureHinglishVoice() {
        val ttsInstance = tts ?: return

        // 1. Prioritize Indian English (en-IN) or Hindi (hi-IN)
        val indianEnglish = Locale("en", "IN")
        val hindiLocale = Locale("hi", "IN")

        var langResult = ttsInstance.setLanguage(indianEnglish)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            langResult = ttsInstance.setLanguage(hindiLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to device default locale so voice is never silent
                ttsInstance.setLanguage(Locale.getDefault())
            }
        }

        // 2. Scan available voices for an Indian English / Hindi voice
        try {
            val voices = ttsInstance.voices
            if (!voices.isNullOrEmpty()) {
                val bestVoice = voices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    val lang = voice.locale.language.lowercase()
                    val country = voice.locale.country.lowercase()
                    (name.contains("en-in") || name.contains("hi-in") || country == "in" || lang == "hi") && !voice.isNetworkConnectionRequired
                } ?: voices.firstOrNull { voice ->
                    voice.locale.country.equals("IN", ignoreCase = true)
                } ?: voices.firstOrNull { voice ->
                    voice.locale.language.equals("hi", ignoreCase = true)
                }

                if (bestVoice != null) {
                    ttsInstance.voice = bestVoice
                }
            }
        } catch (e: Exception) {
            Log.w("TextToSpeechHelper", "Could not customize voice object: ${e.message}")
        }

        // 3. Set friendly pitch and relaxed, clear speaking rate for elderly comprehension
        ttsInstance.setPitch(1.02f)
        ttsInstance.setSpeechRate(0.88f)
    }

    fun speak(text: String) {
        if (text.isBlank()) return

        if (!isReady || tts == null) {
            synchronized(pendingSpeechQueue) {
                pendingSpeechQueue.add(text)
            }
            return
        }
        speakInternal(text)
    }

    private fun speakInternal(text: String) {
        val ttsInstance = tts ?: return
        try {
            val utteranceId = "smriti_tts_${System.currentTimeMillis()}"
            ttsInstance.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e("TextToSpeechHelper", "Error speaking text: ${e.message}")
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    fun shutdown() {
        try {
            stop()
            tts?.shutdown()
            tts = null
            isReady = false
        } catch (_: Exception) {}
    }
}
