package com.example.animeai.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.example.animeai.model.BgmTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class AnimeAudioEngine {

  private var audioTrack: AudioTrack? = null
  private var playbackJob: Job? = null
  private val scope = CoroutineScope(Dispatchers.Default)

  var isPlaying: Boolean = false
    private set

  var currentTrack: BgmTrack? = null
    private set

  private val sampleRate = 44100

  fun playTrack(track: BgmTrack, volume: Float = 0.5f) {
    stop()
    currentTrack = track
    isPlaying = true

    val minBufferSize = AudioTrack.getMinBufferSize(
      sampleRate,
      AudioFormat.CHANNEL_OUT_MONO,
      AudioFormat.ENCODING_PCM_16BIT
    )

    audioTrack = AudioTrack.Builder()
      .setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .build()
      )
      .setAudioFormat(
        AudioFormat.Builder()
          .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
          .setSampleRate(sampleRate)
          .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
          .build()
      )
      .setBufferSizeInBytes(minBufferSize * 2)
      .setTransferMode(AudioTrack.MODE_STREAM)
      .build()

    audioTrack?.setVolume(volume)
    audioTrack?.play()

    playbackJob = scope.launch {
      val baseFreqs = when (track) {
        BgmTrack.ZOMBIE_HORROR -> doubleArrayOf(110.0, 116.54, 130.81, 146.83) // Dark minor 2nd dissonance
        BgmTrack.CYBERPUNK_BATTLE -> doubleArrayOf(130.81, 155.56, 196.00, 261.63) // Cyber arpeggio
        BgmTrack.MELANCHOLY_FLUTE -> doubleArrayOf(220.0, 261.63, 329.63, 392.00) // Shinjuku melancholic chords
        BgmTrack.HERO_AWAKENING -> doubleArrayOf(146.83, 220.00, 293.66, 440.00) // Heroic 5th power
        BgmTrack.TOKYO_NIGHT_PULSE -> doubleArrayOf(164.81, 196.00, 246.94, 329.63) // Neo Tokyo synthwave
      }

      val buffer = ShortArray(2048)
      var phase = 0.0
      var beatCounter = 0
      var currentNoteIndex = 0

      val stepDurationSamples = (sampleRate * 60) / (track.bpm * 4) // 16th note

      while (isActive && isPlaying) {
        val targetFreq = baseFreqs[currentNoteIndex % baseFreqs.size]
        val phaseIncrement = (2.0 * Math.PI * targetFreq) / sampleRate

        for (i in buffer.indices) {
          // Add harmonic layers and low-frequency bass pulse
          val primaryWave = sin(phase)
          val subBass = sin(phase * 0.5) * 0.4
          val overtone = sin(phase * 2.0) * 0.25
          val mixed = (primaryWave + subBass + overtone) * 0.35

          buffer[i] = (mixed * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

          phase += phaseIncrement
          if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI

          beatCounter++
          if (beatCounter >= stepDurationSamples) {
            beatCounter = 0
            currentNoteIndex++
          }
        }

        audioTrack?.write(buffer, 0, buffer.size)
      }
    }
  }

  fun stop() {
    isPlaying = false
    playbackJob?.cancel()
    playbackJob = null
    try {
      audioTrack?.pause()
      audioTrack?.flush()
      audioTrack?.stop()
      audioTrack?.release()
    } catch (e: Exception) {
      Log.e("AnimeAudio", "Error stopping audio track", e)
    }
    audioTrack = null
  }
}
