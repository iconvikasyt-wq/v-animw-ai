package com.example.animeai.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.animeai.model.HindiVoiceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Locale
import java.util.UUID

class HindiVoiceSynthesizer(private val context: Context) : TextToSpeech.OnInitListener {

  private var tts: TextToSpeech? = null
  private val _isReady = MutableStateFlow(false)
  val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _currentUtteranceId = MutableStateFlow<String?>(null)
  val currentUtteranceId: StateFlow<String?> = _currentUtteranceId.asStateFlow()

  private var isHindiSupported = false

  init {
    try {
      tts = TextToSpeech(context.applicationContext, this)
    } catch (e: Exception) {
      Log.e("HindiTTS", "Failed to initialize TTS", e)
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      val hindiLocale = Locale("hi", "IN")
      val result = tts?.setLanguage(hindiLocale)
      if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        // Fallback to default or English while keeping Hindi phonetic text
        Log.w("HindiTTS", "Hindi not directly supported, using default locale")
        tts?.setLanguage(Locale.getDefault())
        isHindiSupported = false
      } else {
        isHindiSupported = true
      }

      tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
          _isSpeaking.value = true
          _currentUtteranceId.value = utteranceId
        }

        override fun onDone(utteranceId: String?) {
          _isSpeaking.value = false
          _currentUtteranceId.value = null
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
          _isSpeaking.value = false
          _currentUtteranceId.value = null
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
          _isSpeaking.value = false
          _currentUtteranceId.value = null
          Log.e("HindiTTS", "TTS Utterance error code: $errorCode")
        }
      })

      _isReady.value = true
    } else {
      Log.e("HindiTTS", "TTS Initialization failed with status $status")
    }
  }

  fun speak(
    text: String,
    voiceType: HindiVoiceType,
    onComplete: (() -> Unit)? = null
  ) {
    if (!_isReady.value || tts == null) return

    tts?.setPitch(voiceType.pitch)
    tts?.setSpeechRate(voiceType.speechRate)

    val utteranceId = UUID.randomUUID().toString()
    val params = Bundle().apply {
      putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
    }

    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
  }

  fun synthesizeDialogueToFile(
    text: String,
    voiceType: HindiVoiceType,
    outputFile: File,
    onComplete: (Boolean) -> Unit
  ) {
    if (!_isReady.value || tts == null) {
      onComplete(false)
      return
    }

    tts?.setPitch(voiceType.pitch)
    tts?.setSpeechRate(voiceType.speechRate)

    val utteranceId = UUID.randomUUID().toString()
    val params = Bundle().apply {
      putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
    }

    // Set temporary listener for this file
    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
      override fun onStart(id: String?) {}
      override fun onDone(id: String?) {
        if (id == utteranceId) {
          onComplete(true)
        }
      }
      override fun onError(id: String?) {
        if (id == utteranceId) {
          onComplete(false)
        }
      }
    })

    val res = tts?.synthesizeToFile(text, params, outputFile, utteranceId)
    if (res != TextToSpeech.SUCCESS) {
      onComplete(false)
    }
  }

  fun stop() {
    try {
      tts?.stop()
      _isSpeaking.value = false
    } catch (e: Exception) {
      Log.e("HindiTTS", "Error stopping TTS", e)
    }
  }

  fun shutdown() {
    try {
      tts?.stop()
      tts?.shutdown()
    } catch (e: Exception) {
      Log.e("HindiTTS", "Error shutting down TTS", e)
    }
  }
}
