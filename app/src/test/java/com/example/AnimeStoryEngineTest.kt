package com.example

import com.example.animeai.generator.AnimeStoryEngine
import com.example.animeai.model.AnimeCharacter
import com.example.animeai.model.HindiVoiceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimeStoryEngineTest {

  @Test
  fun testGenerate15MinuteAnimeProject() {
    val dummyChars = listOf(
      AnimeCharacter(
        id = "c1",
        name = "Renjiro",
        age = 19,
        gender = "Male",
        personality = "Stoic Hero",
        hairColor = "Silver",
        hairstyle = "Spiky",
        eyeColor = "Cyan",
        clothing = "Trench Coat",
        heightCm = 182,
        specialAbilities = "Void Blade",
        storyRole = "Protagonist",
        hindiVoiceType = HindiVoiceType.HERO_MALE
      ),
      AnimeCharacter(
        id = "c2",
        name = "Sakura",
        age = 18,
        gender = "Female",
        personality = "Ninja",
        hairColor = "Pink",
        hairstyle = "Buns",
        eyeColor = "Magenta",
        clothing = "Stealth Suit",
        heightCm = 166,
        specialAbilities = "Shadow Dagger",
        storyRole = "Infiltrator",
        hindiVoiceType = HindiVoiceType.FEMALE_ENERGETIC
      )
    )

    val project = AnimeStoryEngine.generateProject(
      title = "Tokyo Test Anime",
      prompt = "Create a 15-minute Japanese zombie horror anime in Tokyo with 5 main characters.",
      targetDurationMinutes = 15,
      characters = dummyChars
    )

    assertNotNull(project)
    // 15-minute story must be between 30 and 60 scenes (requirement 3 & 8)
    assertTrue("Scenes count should be between 30 and 60, got ${project.scenes.size}", project.scenes.size in 30..60)

    // Verify all scenes have Hindi dialogues and camera directions
    for (scene in project.scenes) {
      assertTrue("Dialogue should not be empty", scene.hindiDialogueText.isNotBlank())
      assertNotNull("Camera angle should be assigned", scene.cameraAngle)
      assertTrue("Speaker should not be blank", scene.hindiDialogueSpeaker.isNotBlank())
    }
  }
}
