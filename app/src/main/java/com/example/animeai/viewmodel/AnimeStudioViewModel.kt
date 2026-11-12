package com.example.animeai.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.animeai.audio.AnimeAudioEngine
import com.example.animeai.data.ProjectRepository
import com.example.animeai.export.AnimeVideoRenderer
import com.example.animeai.model.AnimeCharacter
import com.example.animeai.model.AnimeProject
import com.example.animeai.model.AnimeScene
import com.example.animeai.model.BgmTrack
import com.example.animeai.model.HindiVoiceType
import com.example.animeai.model.NormalizedRect
import com.example.animeai.tts.HindiVoiceSynthesizer
import com.example.animeai.util.CharacterCropHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

enum class StudioTab(val label: String, val iconKanji: String) {
  STUDIO("Studio", "「物語」"),
  CHARACTERS("Characters", "「人物」"),
  VISUALS("Visuals", "「作画」"),
  VOICES("Hindi Voices", "「音声」"),
  TIMELINE("Timeline", "「動画」"),
  EXPORT("Export MP4", "「出力」"),
  PROJECTS("Projects", "「作品」")
}

class AnimeStudioViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = ProjectRepository(application)
  val tts = HindiVoiceSynthesizer(application)
  val audioEngine = AnimeAudioEngine()
  private val videoRenderer = AnimeVideoRenderer(application)

  val currentProject: StateFlow<AnimeProject?> = repository.currentProject
  val projectsList: StateFlow<List<AnimeProject>> = repository.projectsList

  private val _selectedTab = MutableStateFlow(StudioTab.STUDIO)
  val selectedTab: StateFlow<StudioTab> = _selectedTab.asStateFlow()

  // Timeline / Player State
  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _currentPlayingSceneIndex = MutableStateFlow(0)
  val currentPlayingSceneIndex: StateFlow<Int> = _currentPlayingSceneIndex.asStateFlow()

  private val _scenePlaybackProgress = MutableStateFlow(0f)
  val scenePlaybackProgress: StateFlow<Float> = _scenePlaybackProgress.asStateFlow()

  private var playbackJob: Job? = null

  // Export State
  private val _isExporting = MutableStateFlow(false)
  val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

  private val _exportProgress = MutableStateFlow(0f)
  val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

  private val _exportStatusMessage = MutableStateFlow("")
  val exportStatusMessage: StateFlow<String> = _exportStatusMessage.asStateFlow()

  private val _exportedFile = MutableStateFlow<File?>(null)
  val exportedFile: StateFlow<File?> = _exportedFile.asStateFlow()

  // Selected Character for Editing or Detail
  private val _selectedCharacterForEdit = MutableStateFlow<AnimeCharacter?>(null)
  val selectedCharacterForEdit: StateFlow<AnimeCharacter?> = _selectedCharacterForEdit.asStateFlow()

  // Custom multi-character image sheet
  private val _uploadedSheetUri = MutableStateFlow<Uri?>(null)
  val uploadedSheetUri: StateFlow<Uri?> = _uploadedSheetUri.asStateFlow()

  private val _statusAlert = MutableStateFlow<String?>(null)
  val statusAlert: StateFlow<String?> = _statusAlert.asStateFlow()

  init {
    viewModelScope.launch {
      repository.initialize()
    }
  }

  fun setTab(tab: StudioTab) {
    _selectedTab.value = tab
    if (tab != StudioTab.TIMELINE && _isPlaying.value) {
      pausePlayback()
    }
  }

  fun clearStatusAlert() {
    _statusAlert.value = null
  }

  // --- Story & Project Generation ---
  fun generateNewAnime(title: String, prompt: String, durationMinutes: Int) {
    viewModelScope.launch {
      _statusAlert.value = "Generating Japanese anime story & scenes..."
      val project = repository.createNewProject(title, prompt, durationMinutes)
      _statusAlert.value = "Created \"${project.title}\" with ${project.scenes.size} scenes!"
      _selectedTab.value = StudioTab.TIMELINE
    }
  }

  // --- Character Management & Cropping ---
  fun selectCharacterForEdit(character: AnimeCharacter?) {
    _selectedCharacterForEdit.value = character
  }

  fun updateCharacterProfile(character: AnimeCharacter) {
    viewModelScope.launch {
      repository.updateCharacter(character)
      _selectedCharacterForEdit.value = null
      _statusAlert.value = "Updated ${character.name} profile"
    }
  }

  fun cropAndExtractCharacterFromSheet(
    sourceBitmap: Bitmap,
    rect: NormalizedRect,
    name: String,
    role: String,
    voiceType: HindiVoiceType
  ) {
    viewModelScope.launch {
      val savedPath = CharacterCropHelper.cropAndSaveCharacter(
        getApplication(),
        sourceBitmap,
        rect,
        name
      )

      val newChar = AnimeCharacter(
        id = "char_${System.currentTimeMillis()}",
        name = name,
        age = 18,
        gender = "Custom",
        personality = "Determined anime fighter",
        hairColor = "Custom Color",
        hairstyle = "Custom Anime Style",
        eyeColor = "Glowing Eyes",
        clothing = "Neo-Tokyo Battle Suit",
        heightCm = 175,
        specialAbilities = "Secret Awakening Technique",
        storyRole = role,
        hindiVoiceType = voiceType,
        imagePath = savedPath,
        cropBounds = rect
      )

      repository.addCharacter(newChar)
      _statusAlert.value = "Extracted and saved $name to character profiles!"
    }
  }

  fun setUploadedSheetUri(uri: Uri?) {
    _uploadedSheetUri.value = uri
  }

  fun playHindiVoiceTest(text: String, voiceType: HindiVoiceType) {
    tts.speak(text, voiceType)
  }

  fun playBgmTest(track: BgmTrack) {
    if (audioEngine.isPlaying && audioEngine.currentTrack == track) {
      audioEngine.stop()
    } else {
      audioEngine.playTrack(track, volume = 0.6f)
    }
  }

  fun stopBgm() {
    audioEngine.stop()
  }

  // --- Scene Editing ---
  fun updateScene(scene: AnimeScene) {
    viewModelScope.launch {
      repository.updateScene(scene)
    }
  }

  fun deleteScene(sceneId: String) {
    viewModelScope.launch {
      repository.deleteScene(sceneId)
      _statusAlert.value = "Scene deleted"
    }
  }

  fun reorderScenes(fromIndex: Int, toIndex: Int) {
    viewModelScope.launch {
      repository.reorderScenes(fromIndex, toIndex)
    }
  }

  // --- Timeline Video Player ---
  fun togglePlayback() {
    if (_isPlaying.value) {
      pausePlayback()
    } else {
      startPlayback()
    }
  }

  fun startPlayback() {
    val project = currentProject.value ?: return
    if (project.scenes.isEmpty()) return

    _isPlaying.value = true
    playbackJob?.cancel()

    playbackJob = viewModelScope.launch {
      var sceneIdx = _currentPlayingSceneIndex.value
      if (sceneIdx >= project.scenes.size) {
        sceneIdx = 0
        _currentPlayingSceneIndex.value = 0
      }

      while (isActive && _isPlaying.value && sceneIdx < project.scenes.size) {
        val scene = project.scenes[sceneIdx]
        _currentPlayingSceneIndex.value = sceneIdx

        // Speak Hindi dialogue
        val activeChar = project.characters.find { it.id in scene.characterIds }
        val voiceType = activeChar?.hindiVoiceType ?: HindiVoiceType.HERO_MALE
        tts.speak(scene.hindiDialogueText, voiceType)

        // Play matching BGM
        if (!audioEngine.isPlaying || audioEngine.currentTrack != scene.bgmTrack) {
          audioEngine.playTrack(scene.bgmTrack, volume = 0.45f)
        }

        val totalDurationMs = (scene.durationSeconds.coerceIn(3, 8)) * 1000L
        val intervalMs = 50L
        var elapsedMs = 0L

        while (isActive && _isPlaying.value && elapsedMs < totalDurationMs) {
          delay(intervalMs)
          elapsedMs += intervalMs
          _scenePlaybackProgress.value = (elapsedMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
        }

        if (_isPlaying.value) {
          sceneIdx++
          if (sceneIdx >= project.scenes.size) {
            sceneIdx = 0
            _isPlaying.value = false
            audioEngine.stop()
            break
          }
        }
      }
    }
  }

  fun pausePlayback() {
    _isPlaying.value = false
    playbackJob?.cancel()
    playbackJob = null
    tts.stop()
    audioEngine.stop()
  }

  fun seekToScene(index: Int) {
    val project = currentProject.value ?: return
    if (index in project.scenes.indices) {
      _currentPlayingSceneIndex.value = index
      _scenePlaybackProgress.value = 0f
      if (_isPlaying.value) {
        startPlayback()
      }
    }
  }

  // --- MP4 Video Export ---
  fun exportMp4Video(resolution1080p: Boolean = false, maxScenes: Int? = null) {
    val project = currentProject.value ?: return
    if (_isExporting.value) return

    _isExporting.value = true
    _exportProgress.value = 0f
    _exportStatusMessage.value = "Initializing hardware video encoder..."
    _exportedFile.value = null

    viewModelScope.launch {
      try {
        val width = if (resolution1080p) 1920 else 1280
        val height = if (resolution1080p) 1080 else 720

        val file = videoRenderer.renderMp4(
          project = project,
          targetWidth = width,
          targetHeight = height,
          fps = 15,
          maxScenesToExport = maxScenes
        ) { progress ->
          _exportProgress.value = progress.progress
          _exportStatusMessage.value = progress.message
        }

        _exportedFile.value = file
        _statusAlert.value = "MP4 Video Exported Successfully (${file.length() / 1024} KB)!"
      } catch (e: Exception) {
        Log.e("AnimeVM", "Failed to export MP4", e)
        _exportStatusMessage.value = "Export failed: ${e.message}"
        _statusAlert.value = "Export error: ${e.message}"
      } finally {
        _isExporting.value = false
      }
    }
  }

  fun switchProject(project: AnimeProject) {
    repository.selectProject(project)
    _currentPlayingSceneIndex.value = 0
    _scenePlaybackProgress.value = 0f
    _selectedTab.value = StudioTab.STUDIO
    _statusAlert.value = "Switched to ${project.title}"
  }

  override fun onCleared() {
    super.onCleared()
    tts.shutdown()
    audioEngine.stop()
  }
}
