package com.example.animeai.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.animeai.model.AnimeCharacter
import com.example.animeai.model.HindiVoiceType
import com.example.animeai.model.NormalizedRect
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object CharacterCropHelper {

  fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
      context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream)
      }
    } catch (e: Exception) {
      Log.e("CharacterCropHelper", "Failed to load bitmap from uri", e)
      null
    }
  }

  fun loadBitmapFromResource(context: Context, resId: Int): Bitmap? {
    return try {
      BitmapFactory.decodeResource(context.resources, resId)
    } catch (e: Exception) {
      Log.e("CharacterCropHelper", "Failed to load bitmap from res", e)
      null
    }
  }

  fun cropAndSaveCharacter(
    context: Context,
    sourceBitmap: Bitmap,
    rect: NormalizedRect,
    characterName: String
  ): String? {
    return try {
      val bmpWidth = sourceBitmap.width
      val bmpHeight = sourceBitmap.height

      val cropX = (rect.left.coerceIn(0f, 1f) * bmpWidth).toInt()
      val cropY = (rect.top.coerceIn(0f, 1f) * bmpHeight).toInt()
      val cropW = ((rect.right.coerceIn(0f, 1f) - rect.left.coerceIn(0f, 1f)) * bmpWidth).toInt()
        .coerceAtLeast(1)
        .coerceAtMost(bmpWidth - cropX)
      val cropH = ((rect.bottom.coerceIn(0f, 1f) - rect.top.coerceIn(0f, 1f)) * bmpHeight).toInt()
        .coerceAtLeast(1)
        .coerceAtMost(bmpHeight - cropY)

      val cropped = Bitmap.createBitmap(sourceBitmap, cropX, cropY, cropW, cropH)

      val dir = File(context.filesDir, "characters").apply { if (!exists()) mkdirs() }
      val safeName = characterName.lowercase().replace("\\s+".toRegex(), "_")
      val outFile = File(dir, "char_${safeName}_${UUID.randomUUID().toString().take(6)}.jpg")

      FileOutputStream(outFile).use { fos ->
        cropped.compress(Bitmap.CompressFormat.JPEG, 92, fos)
      }
      outFile.absolutePath
    } catch (e: Exception) {
      Log.e("CharacterCropHelper", "Error cropping character", e)
      null
    }
  }

  /**
   * Automatically calculates 5 character candidate bounding boxes
   * across a standard multi-character line-up sheet
   */
  fun detectDefaultSlots(): List<NormalizedRect> {
    return listOf(
      NormalizedRect(0.01f, 0.05f, 0.20f, 0.95f), // Character 1: Left
      NormalizedRect(0.20f, 0.05f, 0.40f, 0.95f), // Character 2: Mid-Left
      NormalizedRect(0.40f, 0.05f, 0.60f, 0.95f), // Character 3: Center
      NormalizedRect(0.60f, 0.05f, 0.80f, 0.95f), // Character 4: Mid-Right
      NormalizedRect(0.80f, 0.05f, 0.99f, 0.95f)  // Character 5: Right
    )
  }

  /**
   * Generates default starter roster using the 5 detected slots
   */
  fun generatePresetCharacters(context: Context): List<AnimeCharacter> {
    val slots = detectDefaultSlots()
    return listOf(
      AnimeCharacter(
        id = "char_renjiro",
        name = "रेन्जिरो (Renjiro)",
        age = 19,
        gender = "Male",
        personality = "गंभीर एवं निडर कटाना योद्धा (Stoic, fearless ronin protector)",
        hairColor = "चांदी जैसा सफेद (Silver White)",
        hairstyle = "नुकीले स्पाइकी बाल और समुराई पोनीटेल (Spiky anime ponytail)",
        eyeColor = "चमकदार नीला (Electric Cyan Glow)",
        clothing = "ब्लैक नियो-टोक्यो ट्रेंच कोट और साइबर आर्मर (Cyber Ronin Trench)",
        heightCm = 182,
        specialAbilities = "शून्य कटाना स्ट्राइक (Void Blade Flash, Mach-3 slice)",
        storyRole = "मुख्य नायक (Lead Protagonist)",
        hindiVoiceType = HindiVoiceType.HERO_MALE,
        cropBounds = slots[0]
      ),
      AnimeCharacter(
        id = "char_sakura",
        name = "साकुरा (Sakura)",
        age = 18,
        gender = "Female",
        personality = "तेजस्वी एवं साहसी साइबर निंजा (Feisty, cunning cyber ninja)",
        hairColor = "गुलाबी नियॉन (Neon Cyber Pink)",
        hairstyle = "ट्विन बन्स और लंबी लटें (Twin Cyber Buns)",
        eyeColor = "गुलाबी मैजेंटा (Glowing Magenta)",
        clothing = "लाइटवेट स्टेल्थ नैनो सूट (Stealth Shinobi Suit)",
        heightCm = 166,
        specialAbilities = "शैडो डैगर एवं होलोग्राफिक क्लोन (Shadow Cloak & Plasma Daggers)",
        storyRole = "इन्फिल्ट्रेटर एवं साथी (Co-Protagonist / Tech Infiltrator)",
        hindiVoiceType = HindiVoiceType.FEMALE_ENERGETIC,
        cropBounds = slots[1]
      ),
      AnimeCharacter(
        id = "char_xavier",
        name = "ज़ेवियर (Xavier)",
        age = 25,
        gender = "Male",
        personality = "शांत, रहस्यमयी डार्क सॉर्सरर (Enigmatic dark alchemist)",
        hairColor = "जेट ब्लैक (Jet Black)",
        hairstyle = "हुड से ढंके लंबे बाल (Shaggy hooded locks)",
        eyeColor = "गहरा रक्त-लाल (Deep Crimson)",
        clothing = "प्राचीन रून्स वाला डार्क चोगा (Rune-inscribed hooded cloak)",
        heightCm = 178,
        specialAbilities = "डार्क एनर्जी बैरियर एवं टेलीकिनेसिस (Void Barrier & Dark Magic)",
        storyRole = "रहस्यमयी मार्गदर्शक (Mysterious Ally / Anti-Hero)",
        hindiVoiceType = HindiVoiceType.VILLAIN_DARK,
        cropBounds = slots[2]
      ),
      AnimeCharacter(
        id = "char_yuki",
        name = "युकी (Yuki)",
        age = 16,
        gender = "Female",
        personality = "उत्साही मेका पायलट एवं जीनियस (Cheerful mech tactical genius)",
        hairColor = "चमकीला नारंगी (Bright Sunset Orange)",
        hairstyle = "लंबी ट्विनटेल्स (Twin anime tails with data clips)",
        eyeColor = "सुनहरा एम्बर (Golden Amber)",
        clothing = "ऑरेंज साइबर पायलट फ्लाइट सूट (Mech Pilot Suit)",
        heightCm = 156,
        specialAbilities = "प्लाज्मा ब्लास्टर एवं ड्रोन कॉलिंग (Tactical Plasma Support)",
        storyRole = "टैक्टिकल ऑपरेटर (Tactical Support / Comic Energy)",
        hindiVoiceType = HindiVoiceType.FEMALE_MYSTERIOUS,
        cropBounds = slots[3]
      ),
      AnimeCharacter(
        id = "char_gaku",
        name = "गाकू (Gaku)",
        age = 29,
        gender = "Male",
        personality = "शक्तिशाली, निष्ठावान कवच रक्षक (Loyal battle-hardened vanguard)",
        hairColor = "गहरा भूरा (Dark Ash Brown)",
        hairstyle = "मिलिट्री क्रू कट (Short combat cut)",
        eyeColor = "पन्ना हरा (Emerald Green)",
        clothing = "हैवी टाइटन एक्सोस्केलेटन प्लेट्स (Titan Exo Armor)",
        heightCm = 196,
        specialAbilities = "काइनेटिक शील्ड एवं सुपर स्ट्रेंथ (Unbreakable Kinetic Wall)",
        storyRole = "ढाल रक्षक (Vanguard Tank / Mentor)",
        hindiVoiceType = HindiVoiceType.DEEP_NARRATOR,
        cropBounds = slots[4]
      )
    )
  }
}
