package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class QuoteTtsHelper(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady: Boolean = false
    private var pendingText: String? = null
    private var onStateChanged: ((Boolean) -> Unit)? = null

    fun speakQuote(text: String, author: String, onSpeakingChanged: (Boolean) -> Unit) {
        onStateChanged = onSpeakingChanged
        val spokenText = if (author.isNotBlank()) "$text. $author." else text

        if (tts == null) {
            pendingText = spokenText
            try {
                tts = TextToSpeech(context.applicationContext) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        val tr = Locale("tr", "TR")
                        val langResult = tts?.setLanguage(tr)
                        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                            tts?.setLanguage(Locale.getDefault())
                        }
                        tts?.setSpeechRate(0.92f)
                        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                onStateChanged?.invoke(true)
                            }

                            override fun onDone(utteranceId: String?) {
                                onStateChanged?.invoke(false)
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                onStateChanged?.invoke(false)
                            }
                        })
                        isReady = true
                        pendingText?.let { queued ->
                            pendingText = null
                            speakInternal(queued)
                        }
                    } else {
                        isReady = false
                        onStateChanged?.invoke(false)
                    }
                }
            } catch (e: Exception) {
                Log.w("QuoteTtsHelper", "TTS init failed: ${e.message}")
                onStateChanged?.invoke(false)
            }
        } else if (isReady) {
            speakInternal(spokenText)
        } else {
            pendingText = spokenText
        }
    }

    private fun speakInternal(text: String) {
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "quote_${System.currentTimeMillis()}")
            onStateChanged?.invoke(true)
        } catch (e: Exception) {
            Log.w("QuoteTtsHelper", "TTS speak failed: ${e.message}")
            onStateChanged?.invoke(false)
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        onStateChanged?.invoke(false)
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        } finally {
            tts = null
            isReady = false
        }
    }
}
