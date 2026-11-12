package com.example.animeai.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.animeai.generator.AnimeStoryEngine
import com.example.animeai.model.AnimeCharacter
import com.example.animeai.model.AnimeProject
import com.example.animeai.model.AnimeScene
import com.example.animeai.util.CharacterCropHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ProjectRepository(private val context: Context) {

  private val _currentProject = MutableStateFlow<AnimeProject?>(null)
  val currentProject: StateFlow<AnimeProject?> = _currentProject.asStateFlow()

  private val _projectsList = MutableStateFlow<List<AnimeProject>>(emptyList())
  val projectsList: StateFlow<List<AnimeProject>> = _projectsList.asStateFlow()

  private val projectsDir = File(context.filesDir, "projects").apply { if (!exists()) mkdirs() }

  suspend fun initialize() = withContext(Dispatchers.IO) {
    val loaded = loadProjectsFromDisk()
    if (loaded.isEmpty()) {
      // Create initial iconic 15-minute 5-character anime project
      val initial = createDefaultTokyoZombieProject()
      saveProject(initial)
      _currentProject.value = initial
      _projectsList.value = listOf(initial)
    } else {
      _projectsList.value = loaded
      _currentProject.value = loaded.firstOrNull()
    }
  }

  private fun createDefaultTokyoZombieProject(): AnimeProject {
    val presetChars = CharacterCropHelper.generatePresetCharacters(context)

    // Pre-crop and link character images if bitmap is available
    val sheetBmp = CharacterCropHelper.loadBitmapFromResource(context, R.drawable.anime_characters_group)
    val enrichedChars = if (sheetBmp != null) {
      presetChars.map { char ->
        val bounds = char.cropBounds
        val savedPath = if (bounds != null) {
          CharacterCropHelper.cropAndSaveCharacter(context, sheetBmp, bounds, char.name)
        } else null
        char.copy(
          imagePath = savedPath,
          fallbackDrawableRes = R.drawable.anime_characters_group
        )
      }
    } else {
      presetChars
    }

    return AnimeStoryEngine.generateProject(
      title = "टोक्यो ज़ोंबी हॉरर 2088 (Tokyo Zombie Horror)",
      prompt = "Create a 15-minute Japanese zombie horror anime in Tokyo with 5 main characters.",
      targetDurationMinutes = 15,
      characters = enrichedChars
    )
  }

  suspend fun createNewProject(
    title: String,
    prompt: String,
    durationMinutes: Int
  ): AnimeProject = withContext(Dispatchers.IO) {
    val characters = _currentProject.value?.characters ?: CharacterCropHelper.generatePresetCharacters(context)
    val project = AnimeStoryEngine.generateProject(title, prompt, durationMinutes, characters)
    saveProject(project)
    _currentProject.value = project
    _projectsList.value = loadProjectsFromDisk()
    project
  }

  suspend fun saveProject(project: AnimeProject) = withContext(Dispatchers.IO) {
    try {
      val json = serializeProject(project)
      val file = File(projectsDir, "proj_${project.id}.json")
      file.writeText(json.toString())
      _currentProject.value = project
      _projectsList.value = loadProjectsFromDisk()
    } catch (e: Exception) {
      Log.e("ProjectRepo", "Failed to save project", e)
    }
  }

  suspend fun updateCharacter(updated: AnimeCharacter) = withContext(Dispatchers.IO) {
    val current = _currentProject.value ?: return@withContext
    val newChars = current.characters.map { if (it.id == updated.id) updated else it }
    val newProject = current.copy(characters = newChars, updatedAt = System.currentTimeMillis())
    saveProject(newProject)
  }

  suspend fun addCharacter(newChar: AnimeCharacter) = withContext(Dispatchers.IO) {
    val current = _currentProject.value ?: return@withContext
    val newChars = current.characters + newChar
    val newProject = current.copy(characters = newChars, updatedAt = System.currentTimeMillis())
    saveProject(newProject)
  }

  suspend fun removeCharacter(charId: String) = withContext(Dispatchers.IO) {
    val current = _currentProject.value ?: return@withContext
    val newChars = current.characters.filter { it.id != charId }
    val newProject = current.copy(characters = newChars, updatedAt = System.currentTimeMillis())
    saveProject(newProject)
  }

  suspend fun updateScene(updatedScene: AnimeScene) = withContext(Dispatchers.IO) {
    val current = _currentProject.value ?: return@withContext
    val newScenes = current.scenes.map { if (it.id == updatedScene.id) updatedScene else it }
    val newProject = current.copy(scenes = newScenes, updatedAt = System.currentTimeMillis())
    saveProject(newProject)
  }

  suspend fun addScene(newScene: AnimeScene) = withContext(Dispatchers.IO) {
    val current = _currentProject.value ?: return@withContext
    val newScenes = current.scenes + newScene.copy(sceneNumber = current.scenes.size + 1)
    val newProject = current.copy(scenes = newScenes, updatedAt = System.currentTimeMillis())
    saveProject(newProject)
  }

  suspend fun deleteScene(sceneId: String) = withContext(Dispatchers.IO) {
    val current = _currentProject.value ?: return@withContext
    val newScenes = current.scenes.filter { it.id != sceneId }
      .mapIndexed { idx, scene -> scene.copy(sceneNumber = idx + 1) }
    val newProject = current.copy(scenes = newScenes, updatedAt = System.currentTimeMillis())
    saveProject(newProject)
  }

  suspend fun reorderScenes(fromIdx: Int, toIdx: Int) = withContext(Dispatchers.IO) {
    val current = _currentProject.value ?: return@withContext
    val mutableList = current.scenes.toMutableList()
    if (fromIdx in mutableList.indices && toIdx in mutableList.indices) {
      val item = mutableList.removeAt(fromIdx)
      mutableList.add(toIdx, item)
      val renumbered = mutableList.mapIndexed { idx, s -> s.copy(sceneNumber = idx + 1) }
      val newProject = current.copy(scenes = renumbered, updatedAt = System.currentTimeMillis())
      saveProject(newProject)
    }
  }

  fun selectProject(project: AnimeProject) {
    _currentProject.value = project
  }

  private fun loadProjectsFromDisk(): List<AnimeProject> {
    val list = mutableListOf<AnimeProject>()
    val files = projectsDir.listFiles { f -> f.extension == "json" } ?: return emptyList()
    for (file in files) {
      try {
        val jsonStr = file.readText()
        val json = JSONObject(jsonStr)
        list.add(deserializeProject(json))
      } catch (e: Exception) {
        Log.e("ProjectRepo", "Failed to deserialize ${file.name}", e)
      }
    }
    return list.sortedByDescending { it.updatedAt }
  }

  private fun serializeProject(p: AnimeProject): JSONObject {
    return JSONObject().apply {
      put("id", p.id)
      put("title", p.title)
      put("storyPrompt", p.storyPrompt)
      put("targetDurationMinutes", p.targetDurationMinutes)
      put("synopsis", p.synopsis)
      put("uploadedSheetPath", p.uploadedSheetPath ?: "")
      put("createdAt", p.createdAt)
      put("updatedAt", p.updatedAt)

      val charArray = JSONArray()
      p.characters.forEach { c ->
        val cObj = JSONObject().apply {
          put("id", c.id)
          put("name", c.name)
          put("age", c.age)
          put("gender", c.gender)
          put("personality", c.personality)
          put("hairColor", c.hairColor)
          put("hairstyle", c.hairstyle)
          put("eyeColor", c.eyeColor)
          put("clothing", c.clothing)
          put("heightCm", c.heightCm)
          put("specialAbilities", c.specialAbilities)
          put("storyRole", c.storyRole)
          put("hindiVoiceType", c.hindiVoiceType.name)
          put("imagePath", c.imagePath ?: "")
          c.cropBounds?.let { b ->
            put("cropLeft", b.left.toDouble())
            put("cropTop", b.top.toDouble())
            put("cropRight", b.right.toDouble())
            put("cropBottom", b.bottom.toDouble())
          }
        }
        charArray.put(cObj)
      }
      put("characters", charArray)

      val sceneArray = JSONArray()
      p.scenes.forEach { s ->
        val sObj = JSONObject().apply {
          put("id", s.id)
          put("sceneNumber", s.sceneNumber)
          put("episodeNumber", s.episodeNumber)
          put("durationSeconds", s.durationSeconds)
          put("characterIds", JSONArray(s.characterIds))
          put("title", s.title)
          put("backgroundDescription", s.backgroundDescription)
          put("characterActions", s.characterActions)
          put("characterExpressions", s.characterExpressions)
          put("cameraAngle", s.cameraAngle.name)
          put("hindiDialogueSpeaker", s.hindiDialogueSpeaker)
          put("hindiDialogueText", s.hindiDialogueText)
          put("englishTranslation", s.englishTranslation)
          put("soundEffect", s.soundEffect)
          put("bgmTrack", s.bgmTrack.name)
          put("transition", s.transition.name)
          put("customImagePath", s.customImagePath ?: "")
        }
        sceneArray.put(sObj)
      }
      put("scenes", sceneArray)
    }
  }

  private fun deserializeProject(json: JSONObject): AnimeProject {
    val charArray = json.getJSONArray("characters")
    val characters = mutableListOf<AnimeCharacter>()
    for (i in 0 until charArray.length()) {
      val cObj = charArray.getJSONObject(i)
      val voiceTypeName = cObj.optString("hindiVoiceType", "HERO_MALE")
      val voiceType = try {
        com.example.animeai.model.HindiVoiceType.valueOf(voiceTypeName)
      } catch (e: Exception) {
        com.example.animeai.model.HindiVoiceType.HERO_MALE
      }

      val bounds = if (cObj.has("cropLeft")) {
        com.example.animeai.model.NormalizedRect(
          cObj.getDouble("cropLeft").toFloat(),
          cObj.getDouble("cropTop").toFloat(),
          cObj.getDouble("cropRight").toFloat(),
          cObj.getDouble("cropBottom").toFloat()
        )
      } else null

      characters.add(
        AnimeCharacter(
          id = cObj.getString("id"),
          name = cObj.getString("name"),
          age = cObj.getInt("age"),
          gender = cObj.getString("gender"),
          personality = cObj.getString("personality"),
          hairColor = cObj.getString("hairColor"),
          hairstyle = cObj.getString("hairstyle"),
          eyeColor = cObj.getString("eyeColor"),
          clothing = cObj.getString("clothing"),
          heightCm = cObj.getInt("heightCm"),
          specialAbilities = cObj.getString("specialAbilities"),
          storyRole = cObj.getString("storyRole"),
          hindiVoiceType = voiceType,
          imagePath = cObj.optString("imagePath").takeIf { it.isNotBlank() },
          fallbackDrawableRes = R.drawable.anime_characters_group,
          cropBounds = bounds
        )
      )
    }

    val sceneArray = json.getJSONArray("scenes")
    val scenes = mutableListOf<AnimeScene>()
    for (i in 0 until sceneArray.length()) {
      val sObj = sceneArray.getJSONObject(i)
      val cIdsArr = sObj.getJSONArray("characterIds")
      val cIds = mutableListOf<String>()
      for (k in 0 until cIdsArr.length()) {
        cIds.add(cIdsArr.getString(k))
      }

      val camAngle = try {
        com.example.animeai.model.CameraAngle.valueOf(sObj.getString("cameraAngle"))
      } catch (e: Exception) {
        com.example.animeai.model.CameraAngle.ZOOM_IN
      }

      val bgm = try {
        com.example.animeai.model.BgmTrack.valueOf(sObj.getString("bgmTrack"))
      } catch (e: Exception) {
        com.example.animeai.model.BgmTrack.ZOMBIE_HORROR
      }

      val trans = try {
        com.example.animeai.model.SceneTransition.valueOf(sObj.getString("transition"))
      } catch (e: Exception) {
        com.example.animeai.model.SceneTransition.CROSS_DISSOLVE
      }

      scenes.add(
        AnimeScene(
          id = sObj.getString("id"),
          sceneNumber = sObj.getInt("sceneNumber"),
          episodeNumber = sObj.optInt("episodeNumber", 1),
          durationSeconds = sObj.getInt("durationSeconds"),
          characterIds = cIds,
          title = sObj.getString("title"),
          backgroundDescription = sObj.getString("backgroundDescription"),
          characterActions = sObj.getString("characterActions"),
          characterExpressions = sObj.getString("characterExpressions"),
          cameraAngle = camAngle,
          hindiDialogueSpeaker = sObj.getString("hindiDialogueSpeaker"),
          hindiDialogueText = sObj.getString("hindiDialogueText"),
          englishTranslation = sObj.optString("englishTranslation", ""),
          soundEffect = sObj.optString("soundEffect", ""),
          bgmTrack = bgm,
          transition = trans,
          customImagePath = sObj.optString("customImagePath").takeIf { it.isNotBlank() },
          presetDrawableRes = R.drawable.anime_bg_tokyo
        )
      )
    }

    return AnimeProject(
      id = json.getString("id"),
      title = json.getString("title"),
      storyPrompt = json.getString("storyPrompt"),
      targetDurationMinutes = json.optInt("targetDurationMinutes", 15),
      synopsis = json.getString("synopsis"),
      characters = characters,
      scenes = scenes,
      uploadedSheetPath = json.optString("uploadedSheetPath").takeIf { it.isNotBlank() },
      createdAt = json.optLong("createdAt", System.currentTimeMillis()),
      updatedAt = json.optLong("updatedAt", System.currentTimeMillis())
    )
  }
}
