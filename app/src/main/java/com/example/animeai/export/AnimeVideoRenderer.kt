package com.example.animeai.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import com.example.animeai.model.AnimeCharacter
import com.example.animeai.model.AnimeProject
import com.example.animeai.model.AnimeScene
import com.example.animeai.model.CameraAngle
import com.example.animeai.util.CharacterCropHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

class AnimeVideoRenderer(private val context: Context) {

  data class RenderProgress(
    val progress: Float,
    val currentScene: Int,
    val totalScenes: Int,
    val message: String
  )

  suspend fun renderMp4(
    project: AnimeProject,
    targetWidth: Int = 1280,
    targetHeight: Int = 720,
    fps: Int = 15,
    maxScenesToExport: Int? = null,
    onProgress: (RenderProgress) -> Unit
  ): File = withContext(Dispatchers.Default) {
    val exportDir = File(context.filesDir, "exports").apply { if (!exists()) mkdirs() }
    val safeTitle = project.title.lowercase().replace("[^a-z0-9]".toRegex(), "_")
    val outputFile = File(exportDir, "anime_${safeTitle}_${System.currentTimeMillis()}.mp4")

    val scenes = if (maxScenesToExport != null) {
      project.scenes.take(maxScenesToExport)
    } else {
      project.scenes
    }

    val totalScenes = scenes.size
    val mimeType = "video/avc"

    val format = MediaFormat.createVideoFormat(mimeType, targetWidth, targetHeight).apply {
      setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
      setInteger(MediaFormat.KEY_BIT_RATE, 2_500_000)
      setInteger(MediaFormat.KEY_FRAME_RATE, fps)
      setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
    }

    val encoder = MediaCodec.createEncoderByType(mimeType)
    encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    encoder.start()

    val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    var trackIndex = -1
    var muxerStarted = false

    val bufferInfo = MediaCodec.BufferInfo()
    val frameBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(frameBitmap)

    // Pre-cache background and character reference bitmaps
    val bgBitmap = project.scenes.firstOrNull()?.presetDrawableRes?.let {
      CharacterCropHelper.loadBitmapFromResource(context, it)
    }

    var presentationTimeUs = 0L
    val frameDurationUs = (1_000_000L / fps)

    val yuvBuffer = ByteArray(targetWidth * targetHeight * 3 / 2)
    val argbBuffer = IntArray(targetWidth * targetHeight)

    try {
      var globalFrame = 0
      val totalEstimatedFrames = scenes.sumOf { (it.durationSeconds.coerceIn(3, 8) * fps) }

      scenes.forEachIndexed { sceneIndex, scene ->
        val sceneFrames = scene.durationSeconds.coerceIn(3, 8) * fps
        val activeChar = project.characters.find { it.id in scene.characterIds }

        val charBitmap = activeChar?.imagePath?.let { path ->
          val file = File(path)
          if (file.exists()) android.graphics.BitmapFactory.decodeFile(file.absolutePath) else null
        }

        for (frameInScene in 0 until sceneFrames) {
          val progressRatio = frameInScene.toFloat() / sceneFrames.coerceAtLeast(1)

          // 1. Draw Frame Canvas with Ken Burns motion, character overlay and subtitles
          drawSceneFrame(
            canvas = canvas,
            width = targetWidth,
            height = targetHeight,
            scene = scene,
            character = activeChar,
            characterBitmap = charBitmap,
            backgroundBitmap = bgBitmap,
            sceneProgress = progressRatio,
            sceneIndex = sceneIndex + 1,
            totalScenes = totalScenes
          )

          // 2. Convert ARGB Bitmap to YUV420 Semi-Planar (NV12)
          frameBitmap.getPixels(argbBuffer, 0, targetWidth, 0, 0, targetWidth, targetHeight)
          encodeYuv420sp(argbBuffer, yuvBuffer, targetWidth, targetHeight)

          // 3. Feed to MediaCodec
          val inputBufferIndex = encoder.dequeueInputBuffer(10_000)
          if (inputBufferIndex >= 0) {
            val inputBuffer = encoder.getInputBuffer(inputBufferIndex)
            inputBuffer?.clear()
            inputBuffer?.put(yuvBuffer)
            encoder.queueInputBuffer(
              inputBufferIndex,
              0,
              yuvBuffer.size,
              presentationTimeUs,
              0
            )
            presentationTimeUs += frameDurationUs
          }

          // 4. Drain encoder outputs to Muxer
          drainEncoder(encoder, bufferInfo, muxer, { trackIndex = it }, { muxerStarted = true }, muxerStarted)

          globalFrame++
          val overallProgress = (globalFrame.toFloat() / totalEstimatedFrames.coerceAtLeast(1)).coerceIn(0f, 0.99f)
          onProgress(
            RenderProgress(
              progress = overallProgress,
              currentScene = sceneIndex + 1,
              totalScenes = totalScenes,
              message = "Rendering Scene ${sceneIndex + 1}/$totalScenes: ${scene.title}"
            )
          )
        }
      }

      // Signal End of Stream
      val eosInputIndex = encoder.dequeueInputBuffer(10_000)
      if (eosInputIndex >= 0) {
        encoder.queueInputBuffer(eosInputIndex, 0, 0, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
      }

      // Drain remaining output buffers
      drainEncoder(encoder, bufferInfo, muxer, { trackIndex = it }, { muxerStarted = true }, muxerStarted, endOfStream = true)

    } finally {
      try {
        encoder.stop()
        encoder.release()
      } catch (e: Exception) {
        Log.e("AnimeVideoRenderer", "Error releasing encoder", e)
      }

      try {
        if (muxerStarted) {
          muxer.stop()
          muxer.release()
        }
      } catch (e: Exception) {
        Log.e("AnimeVideoRenderer", "Error releasing muxer", e)
      }
    }

    onProgress(
      RenderProgress(
        progress = 1.0f,
        currentScene = totalScenes,
        totalScenes = totalScenes,
        message = "MP4 Export complete! Video ready."
      )
    )

    outputFile
  }

  private fun drainEncoder(
    encoder: MediaCodec,
    bufferInfo: MediaCodec.BufferInfo,
    muxer: MediaMuxer,
    setTrackIndex: (Int) -> Unit,
    setMuxerStarted: () -> Unit,
    isMuxerStarted: Boolean,
    endOfStream: Boolean = false
  ) {
    var muxStarted = isMuxerStarted
    while (true) {
      val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
      if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
        if (!endOfStream) break else continue
      } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
        if (muxStarted) throw IllegalStateException("format changed twice")
        val newFormat = encoder.outputFormat
        val track = muxer.addTrack(newFormat)
        setTrackIndex(track)
        muxer.start()
        muxStarted = true
        setMuxerStarted()
      } else if (outputBufferIndex >= 0) {
        val encodedData = encoder.getOutputBuffer(outputBufferIndex) ?: continue
        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
          bufferInfo.size = 0
        }

        if (bufferInfo.size > 0 && muxStarted) {
          encodedData.position(bufferInfo.offset)
          encodedData.limit(bufferInfo.offset + bufferInfo.size)
          muxer.writeSampleData(0, encodedData, bufferInfo)
        }

        encoder.releaseOutputBuffer(outputBufferIndex, false)
        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
          break
        }
      }
    }
  }

  private fun drawSceneFrame(
    canvas: Canvas,
    width: Int,
    height: Int,
    scene: AnimeScene,
    character: AnimeCharacter?,
    characterBitmap: Bitmap?,
    backgroundBitmap: Bitmap?,
    sceneProgress: Float,
    sceneIndex: Int,
    totalScenes: Int
  ) {
    // 1. Dark anime background base
    canvas.drawColor(Color.parseColor("#090714"))

    // 2. Draw Background with Ken Burns Motion
    if (backgroundBitmap != null && !backgroundBitmap.isRecycled) {
      canvas.save()
      val scaleFactor = when (scene.cameraAngle) {
        CameraAngle.ZOOM_IN -> 1.0f + (sceneProgress * 0.18f)
        CameraAngle.ZOOM_OUT -> 1.18f - (sceneProgress * 0.18f)
        else -> 1.08f
      }

      val panX = when (scene.cameraAngle) {
        CameraAngle.PAN_LEFT -> (1.0f - sceneProgress) * (width * 0.08f)
        CameraAngle.PAN_RIGHT -> -(sceneProgress) * (width * 0.08f)
        else -> 0f
      }

      canvas.translate(panX, 0f)
      canvas.scale(scaleFactor, scaleFactor, width / 2f, height / 2f)

      val srcRect = Rect(0, 0, backgroundBitmap.width, backgroundBitmap.height)
      val dstRect = Rect(0, 0, width, height)
      canvas.drawBitmap(backgroundBitmap, srcRect, dstRect, null)
      canvas.restore()
    } else {
      // Atmospheric Cyber Gradient
      val gradPaint = Paint().apply {
        shader = LinearGradient(
          0f, 0f, width.toFloat(), height.toFloat(),
          intArrayOf(Color.parseColor("#1B0B33"), Color.parseColor("#080512")),
          null,
          Shader.TileMode.CLAMP
        )
      }
      canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradPaint)
    }

    // 3. Cinematic Vignette & Neon Atmospheric Lighting
    val vignettePaint = Paint().apply {
      shader = LinearGradient(
        0f, 0f, 0f, height.toFloat(),
        intArrayOf(Color.parseColor("#99000000"), Color.TRANSPARENT, Color.parseColor("#EE000000")),
        floatArrayOf(0f, 0.4f, 1f),
        Shader.TileMode.CLAMP
      )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)

    // 4. Character Reference Inset (Consistency Guarantee)
    if (character != null) {
      val cardLeft = width * 0.04f
      val cardTop = height * 0.12f
      val cardSize = height * 0.32f

      val cardPaint = Paint().apply {
        color = Color.parseColor("#DD120B24")
        style = Paint.Style.FILL
      }
      val borderPaint = Paint().apply {
        color = Color.parseColor("#00E5FF")
        strokeWidth = 4f
        style = Paint.Style.STROKE
      }

      val roundRect = RectF(cardLeft, cardTop, cardLeft + cardSize * 0.85f, cardTop + cardSize)
      canvas.drawRoundRect(roundRect, 18f, 18f, cardPaint)

      if (characterBitmap != null && !characterBitmap.isRecycled) {
        val charDst = RectF(roundRect.left + 8, roundRect.top + 8, roundRect.right - 8, roundRect.bottom - 8)
        canvas.drawBitmap(
          characterBitmap,
          Rect(0, 0, characterBitmap.width, characterBitmap.height),
          charDst,
          null
        )
      }

      canvas.drawRoundRect(roundRect, 18f, 18f, borderPaint)

      // Character Name Badge
      val badgePaint = Paint().apply {
        color = Color.parseColor("#FF2A85")
        style = Paint.Style.FILL
      }
      val badgeRect = RectF(roundRect.left, roundRect.bottom - 36f, roundRect.right, roundRect.bottom)
      canvas.drawRoundRect(badgeRect, 10f, 10f, badgePaint)

      val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 22f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
      }
      canvas.drawText(character.name, badgeRect.centerX(), badgeRect.centerY() + 7f, textPaint)
    }

    // 5. Scene Header & Camera Direction HUD
    val hudPaint = Paint().apply {
      color = Color.parseColor("#BB000000")
      style = Paint.Style.FILL
    }
    val hudRect = RectF(width * 0.04f, 20f, width * 0.96f, 75f)
    canvas.drawRoundRect(hudRect, 12f, 12f, hudPaint)

    val hudTextPaint = Paint().apply {
      color = Color.parseColor("#00E5FF")
      textSize = 24f
      isFakeBoldText = true
    }
    canvas.drawText(
      "「アニメAI」 दृश्य $sceneIndex/$totalScenes • ${scene.cameraAngle.label}",
      hudRect.left + 24f,
      hudRect.centerY() + 8f,
      hudTextPaint
    )

    // 6. Styled Hindi Subtitles Box at the Bottom
    val subBoxHeight = height * 0.22f
    val subBoxTop = height - subBoxHeight - 24f
    val subBoxRect = RectF(width * 0.06f, subBoxTop, width * 0.94f, height - 24f)

    val subBgPaint = Paint().apply {
      color = Color.parseColor("#E507050E")
      style = Paint.Style.FILL
    }
    canvas.drawRoundRect(subBoxRect, 20f, 20f, subBgPaint)

    val subBorderPaint = Paint().apply {
      color = Color.parseColor("#9D4EDD")
      strokeWidth = 3f
      style = Paint.Style.STROKE
    }
    canvas.drawRoundRect(subBoxRect, 20f, 20f, subBorderPaint)

    // Speaker Title
    val speakerPaint = Paint().apply {
      color = Color.parseColor("#FFB703")
      textSize = 26f
      isFakeBoldText = true
    }
    canvas.drawText(
      "► ${scene.hindiDialogueSpeaker}",
      subBoxRect.left + 24f,
      subBoxRect.top + 38f,
      speakerPaint
    )

    // Hindi Dialogue Text
    val dialoguePaint = Paint().apply {
      color = Color.WHITE
      textSize = 28f
      isFakeBoldText = true
    }

    // Basic 2-line break if text is long
    val fullText = scene.hindiDialogueText
    if (fullText.length > 38) {
      val splitIdx = fullText.lastIndexOf(' ', 38).coerceAtLeast(20)
      val line1 = fullText.substring(0, splitIdx)
      val line2 = fullText.substring(splitIdx).trim()
      canvas.drawText(line1, subBoxRect.left + 24f, subBoxRect.top + 76f, dialoguePaint)
      canvas.drawText(line2, subBoxRect.left + 24f, subBoxRect.top + 112f, dialoguePaint)
    } else {
      canvas.drawText(fullText, subBoxRect.left + 24f, subBoxRect.top + 84f, dialoguePaint)
    }
  }

  /**
   * Fast RGB to YUV420 semi-planar (NV12) conversion
   */
  private fun encodeYuv420sp(argb: IntArray, yuv420sp: ByteArray, width: Int, height: Int) {
    val frameSize = width * height
    var yIndex = 0
    var uvIndex = frameSize

    var a: Int
    var R: Int
    var G: Int
    var B: Int
    var Y: Int
    var U: Int
    var V: Int
    var index = 0

    for (j in 0 until height) {
      for (i in 0 until width) {
        val color = argb[index++]
        R = (color shr 16) and 0xff
        G = (color shr 8) and 0xff
        B = color and 0xff

        Y = ((66 * R + 129 * G + 25 * B + 128) shr 8) + 16
        U = ((-38 * R - 74 * G + 112 * B + 128) shr 8) + 128
        V = ((112 * R - 94 * G - 18 * B + 128) shr 8) + 128

        yuv420sp[yIndex++] = Y.coerceIn(0, 255).toByte()

        if (j % 2 == 0 && i % 2 == 0) {
          yuv420sp[uvIndex++] = U.coerceIn(0, 255).toByte()
          yuv420sp[uvIndex++] = V.coerceIn(0, 255).toByte()
        }
      }
    }
  }
}
