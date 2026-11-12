package com.example.animeai.model

import androidx.annotation.DrawableRes
import com.example.R

enum class HindiVoiceType(
  val displayName: String,
  val pitch: Float,
  val speechRate: Float,
  val gender: String,
  val description: String
) {
  HERO_MALE("वीर पुरुष (Hero Male)", 0.95f, 0.95f, "Male", "Confident, bold anime protagonist voice"),
  YOUTH_MALE("युवा नायक (Youth Male)", 1.25f, 1.05f, "Male", "Energetic, youthful anime hero"),
  FEMALE_ENERGETIC("उत्साही नारी (Female Energetic)", 1.45f, 1.05f, "Female", "Lively, spirited anime heroine"),
  FEMALE_MYSTERIOUS("रहस्यमयी नारी (Female Mystic)", 0.85f, 0.85f, "Female", "Whispering, deep mysterious female voice"),
  DEEP_NARRATOR("गंभीर सूत्रधार (Deep Narrator)", 0.65f, 0.88f, "Male", "Epic cinematic narrator voice"),
  VILLAIN_DARK("अंधकार खलनायक (Dark Villain)", 0.60f, 0.80f, "Male", "Menacing, raspy anime antagonist")
}

data class NormalizedRect(
  val left: Float,
  val top: Float,
  val right: Float,
  val bottom: Float
)

data class AnimeCharacter(
  val id: String,
  val name: String,
  val age: Int,
  val gender: String,
  val personality: String,
  val hairColor: String,
  val hairstyle: String,
  val eyeColor: String,
  val clothing: String,
  val heightCm: Int,
  val specialAbilities: String,
  val storyRole: String,
  val hindiVoiceType: HindiVoiceType,
  val imagePath: String? = null,
  @DrawableRes val fallbackDrawableRes: Int? = null,
  val cropBounds: NormalizedRect? = null
)

enum class CameraAngle(val label: String, val instruction: String) {
  PAN_LEFT("Pan Left", "Smooth lateral camera pan from right to left tracking movement"),
  PAN_RIGHT("Pan Right", "Smooth camera pan from left to right revealing background"),
  ZOOM_IN("Zoom In (Hero Focus)", "Dramatic slow push-in towards character's eyes"),
  ZOOM_OUT("Zoom Out (Wide Scope)", "Camera pulls back to show atmospheric scale"),
  CLOSE_UP("Extreme Close-Up", "Intense close-up on expression, sweat, or eyes"),
  DUTCH_ANGLE("Dutch Angle (Horror Tilt)", "Canted camera angle for tension, fear, or combat"),
  WIDE_SHOT("Cinematic Wide", "Full landscape panorama establishing Tokyo environment"),
  ACTION_SHAKE("Action Cam", "Dynamic kinetic camera with anime speedlines")
}

enum class SceneTransition(val label: String) {
  CUT("Hard Anime Cut"),
  FADE_BLACK("Fade to Black"),
  CROSS_DISSOLVE("Cross Dissolve"),
  NEON_FLASH("Neon White Flash"),
  GLITCH("Cyber Glitch Cut"),
  ZOOM_BLUR("Zoom Motion Blur")
}

enum class BgmTrack(val title: String, val mood: String, val bpm: Int) {
  ZOMBIE_HORROR("Tokyo Horror & Shadows", "Ominous Suspense", 75),
  CYBERPUNK_BATTLE("Cyber Neon Clash", "High Octane Action", 140),
  MELANCHOLY_FLUTE("Shinjuku Rain Memories", "Emotional & Nostalgic", 68),
  HERO_AWAKENING("Awakening of the Ronin", "Epic Orchestral Anime OST", 125),
  TOKYO_NIGHT_PULSE("Tokyo Midnight Synth", "Stylized Cyberpunk Beats", 110)
}

data class AnimeScene(
  val id: String,
  val sceneNumber: Int,
  val episodeNumber: Int = 1,
  val durationSeconds: Int = 6,
  val characterIds: List<String> = emptyList(),
  val title: String,
  val backgroundDescription: String,
  val characterActions: String,
  val characterExpressions: String,
  val cameraAngle: CameraAngle = CameraAngle.ZOOM_IN,
  val hindiDialogueSpeaker: String,
  val hindiDialogueText: String,
  val englishTranslation: String = "",
  val soundEffect: String = "Footsteps in rain, distant zombie groans",
  val bgmTrack: BgmTrack = BgmTrack.ZOMBIE_HORROR,
  val transition: SceneTransition = SceneTransition.CROSS_DISSOLVE,
  val customImagePath: String? = null,
  @DrawableRes val presetDrawableRes: Int? = null
)

data class AnimeProject(
  val id: String,
  val title: String,
  val storyPrompt: String,
  val targetDurationMinutes: Int = 15,
  val synopsis: String,
  val characters: List<AnimeCharacter>,
  val scenes: List<AnimeScene>,
  val uploadedSheetPath: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
