package com.example.animeai.generator

import com.example.R
import com.example.animeai.model.AnimeCharacter
import com.example.animeai.model.AnimeProject
import com.example.animeai.model.AnimeScene
import com.example.animeai.model.BgmTrack
import com.example.animeai.model.CameraAngle
import com.example.animeai.model.SceneTransition
import java.util.UUID

object AnimeStoryEngine {

  val PRESET_PROMPTS = listOf(
    "Create a 15-minute Japanese zombie horror anime in Tokyo with 5 main characters.",
    "Tokyo Cyberpunk 2099: A rogue AI infects Shinjuku cyborgs and 5 ronin must unite.",
    "Demon Exorcists in Shibuya: Ancient cursed spirits awaken beneath Tokyo subway lines.",
    "Mecha Defense Tokyo: Alien bio-titans attack Odaiba Bay and 5 elite pilots deploy.",
    "Neon Ronin: An undercover ninja clan uncovers a dark corporate conspiracy in Akihabara."
  )

  fun generateProject(
    title: String,
    prompt: String,
    targetDurationMinutes: Int,
    characters: List<AnimeCharacter>
  ): AnimeProject {
    // Determine scene count based on duration
    val targetScenesCount = when (targetDurationMinutes) {
      5 -> 18
      10 -> 30
      15 -> 42
      20 -> 50
      else -> 60
    }

    val synopsis = generateSynopsis(prompt, characters)
    val scenes = generateScenes(prompt, targetScenesCount, characters)

    return AnimeProject(
      id = UUID.randomUUID().toString(),
      title = title.ifBlank { "Tokyo Anime Chronicle: Chapter 1" },
      storyPrompt = prompt,
      targetDurationMinutes = targetDurationMinutes,
      synopsis = synopsis,
      characters = characters,
      scenes = scenes,
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis()
    )
  }

  private fun generateSynopsis(prompt: String, characters: List<AnimeCharacter>): String {
    val names = characters.joinToString(", ") { it.name }
    return "टोक्यो की रात में अचानक एक भयानक जैविक लहर फैल जाती है। " +
        "$names को अपने मतभेद भुलाकर एक साथ आना पड़ता है। " +
        "जैसे-जैसे रहस्य गहराता है, शिबुया और शिनजुकु की गलियों में जीवन और मृत्यु का महासंग्राम शुरू होता है। " +
        "क्या वे सूर्योदय से पहले टोक्यो को बचा पाएंगे?"
  }

  private fun generateScenes(
    prompt: String,
    count: Int,
    characters: List<AnimeCharacter>
  ): List<AnimeScene> {
    val char1 = characters.getOrNull(0) ?: defaultChar("रेन्जिरो", "Hero")
    val char2 = characters.getOrNull(1) ?: defaultChar("साकुरा", "Ninja")
    val char3 = characters.getOrNull(2) ?: defaultChar("ज़ेवियर", "Mage")
    val char4 = characters.getOrNull(3) ?: defaultChar("युकी", "Pilot")
    val char5 = characters.getOrNull(4) ?: defaultChar("गाकू", "Tank")

    val actsCount = 3
    val scenesPerAct = count / actsCount

    val scenes = mutableListOf<AnimeScene>()

    for (i in 1..count) {
      val act = when {
        i <= scenesPerAct -> 1
        i <= scenesPerAct * 2 -> 2
        else -> 3
      }

      val sceneData = buildSceneTemplate(i, count, act, char1, char2, char3, char4, char5)
      scenes.add(sceneData)
    }

    return scenes
  }

  private fun buildSceneTemplate(
    index: Int,
    total: Int,
    act: Int,
    c1: AnimeCharacter,
    c2: AnimeCharacter,
    c3: AnimeCharacter,
    c4: AnimeCharacter,
    c5: AnimeCharacter
  ): AnimeScene {
    val id = "scene_$index"
    val duration = when {
      index == 1 || index == total -> 7
      index % 5 == 0 -> 8
      else -> 5
    }

    // Dynamic rotation of camera angles
    val camera = when (index % 8) {
      0 -> CameraAngle.WIDE_SHOT
      1 -> CameraAngle.ZOOM_IN
      2 -> CameraAngle.DUTCH_ANGLE
      3 -> CameraAngle.CLOSE_UP
      4 -> CameraAngle.PAN_LEFT
      5 -> CameraAngle.PAN_RIGHT
      6 -> CameraAngle.ACTION_SHAKE
      else -> CameraAngle.ZOOM_OUT
    }

    val transition = when {
      index == total -> SceneTransition.FADE_BLACK
      index % 6 == 0 -> SceneTransition.NEON_FLASH
      index % 4 == 0 -> SceneTransition.GLITCH
      else -> SceneTransition.CROSS_DISSOLVE
    }

    val bgm = when (act) {
      1 -> if (index <= 3) BgmTrack.MELANCHOLY_FLUTE else BgmTrack.ZOMBIE_HORROR
      2 -> if (index % 2 == 0) BgmTrack.CYBERPUNK_BATTLE else BgmTrack.TOKYO_NIGHT_PULSE
      else -> BgmTrack.HERO_AWAKENING
    }

    // Act-specific story beat generation
    val (title, bgDesc, action, expr, speaker, hindiText, engText, soundFx, charsPresent) = when (act) {
      1 -> {
        // Act 1: Awakening & Incident Discovery
        when (index % 4) {
          1 -> Tuple9(
            "दृश्य $index: शिबुया क्रॉसिंग का सन्नाटा",
            "टोक्यो के प्रसिद्ध शिबुया क्रॉसिंग पर बिखरा सन्नाटा, लाल और नीली नियॉन लाइटें बारिश की बूंदों में चमक रही हैं।",
            "${c1.name} अपनी चमकती कटाना के मुठ्ठे पर हाथ रखे आगे बढ़ता है।",
            "गंभीर और सतर्क निगाहें, हवा में खतरे की गंध।",
            c1.name,
            "टोक्यो की ये रात सामान्य नहीं है... हवा में बारूद और ख़ामोशी दोनों तैर रही हैं।",
            "This Tokyo night is not normal... both gunpowder and silence hang in the air.",
            "हल्की बारिश, दूर से आती सायरन की गूंज",
            listOf(c1.id)
          )
          2 -> Tuple9(
            "दृश्य $index: साइबर सेंसर्स पर अलर्ट",
            "शिनजुकु की छत पर साइबर टर्मिनल्स, होलोग्राफिक मॉनिटर्स पर लाल खतरे के संकेत फ्लैश हो रहे हैं।",
            "${c2.name} अपने नैनो-पैड्स पर तेजी से डेटा स्कैन करती है और पीछे मुड़ती है।",
            "माथे पर पसीना, आंखों में घबराहट और चौंकने का भाव।",
            c2.name,
            "रेन्जिरो! सबवे लाइन चार से भारी बायो-एनर्जी सिग्नल्स आ रहे हैं! वे कोई इंसान नहीं हैं!",
            "Renjiro! Heavy bio-energy signals from subway line four! They are not human!",
            "होलोग्राफिक बीप्स, तेज कीबोर्ड टाइपिंग",
            listOf(c1.id, c2.id)
          )
          3 -> Tuple9(
            "दृश्य $index: अंधेरी परछाइयां",
            "भूमिगत सबवे स्टेशन की सीढ़ियां, टूटी हुई ट्यूबलाइटें झिलमिला रही हैं। कोहरे में काली आकृतियां रेंग रही हैं।",
            "${c3.name} अपने चोगे से प्राचीन रून्स की चमक बिखेरते हुए साए को देखता है।",
            "शांत रहस्यमयी मुस्कान, होंठों पर प्राचीन मंत्र।",
            c3.name,
            "यह वही अभिशाप है जिसकी भविष्यवाणी सौ साल पहले की गई थी... अंधेरा जाग चुका है।",
            "This is the very curse foretold a century ago... the darkness has awakened.",
            "ट्यूबलाइट की चरमराहट, गहरी डरावनी सांसें",
            listOf(c3.id)
          )
          else -> Tuple9(
            "दृश्य $index: आपातकालीन मोर्चा",
            "टोक्यो टॉवर के पास मुख्य बैरिकेड, पुलिस गाड़ियां उलटी पड़ी हैं।",
            "${c4.name} अपने मेका ड्रोन को तैयार करती है और ${c5.name} भारी ढाल जमीन में गाड़ देता है।",
            "दृढ़ संकल्प, युद्ध के लिए तैयार योद्धा।",
            c5.name,
            "कोई भी पीछे नहीं हटेगा! हम टोक्यो के आखिरी रक्षा कवच हैं!",
            "Nobody retreats! We are the final shield of Tokyo!",
            "धातु के टकराने की गूंज, जेट थ्रस्टर्स की सीटी",
            listOf(c4.id, c5.id)
          )
        }
      }
      2 -> {
        // Act 2: Escalation, Ambush & Combat
        when (index % 4) {
          1 -> Tuple9(
            "दृश्य $index: ज़ोंबी झुंड का हमला",
            "अकिहाबारा की इलेक्ट्रिक स्ट्रीट, शटर टूटे हुए, सैकड़ों भयानक संक्रमित साये चारों तरफ से घेरते हैं।",
            "${c1.name} बिजली की गति से कटाना खींचता है, नीली ऊर्जा की लकीरें हवा को चीरती हैं।",
            "आंखों में नीली चमक, अदम्य साहस।",
            c1.name,
            "शून्य प्रहार! साकुरा, मेरी दाईं तरफ का रास्ता साफ करो!",
            "Void Strike! Sakura, clear the path on my right!",
            "कटाना की तीखी आवाज, संक्रमितों की चीख",
            listOf(c1.id, c2.id)
          )
          2 -> Tuple9(
            "दृश्य $index: निंजा शैडो डैगर",
            "दीवारों और नियॉन बिलबोर्ड्स पर छलांग लगाते हुए परछाई।",
            "${c2.name} हवा में कलाबाजी खाते हुए प्लाज्मा डैगर फेंकती है।",
            "तीखा फोकस, तेज हवा में लहराते गुलाबी बाल।",
            c2.name,
            "रास्ता साफ है! लेकिन इनकी तादाद हर सेकेंड दोगुनी हो रही है!",
            "Path cleared! But their numbers are doubling every second!",
            "प्लाज्मा धमाका, कांच के बिखरने की आवाज",
            listOf(c2.id)
          )
          3 -> Tuple9(
            "दृश्य $index: जादू और प्लाज्मा का संगम",
            "एक विशाल चौराहे पर रून्स का चमकता घेरा और ऊपर उड़ते मिसाइल ड्रोन्स।",
            "${c3.name} और ${c4.name} एक साथ अपनी शक्तियों को जोड़ते हैं।",
            "ऊर्जा का दबाव, दोनों के चेहरे पर तनाव।",
            c4.name,
            "प्लाज्मा ओवरचार्ज 120 प्रतिशत! ज़ेवियर, अपना बैरियर मजबूत रखो!",
            "Plasma overcharge 120 percent! Xavier, hold your barrier strong!",
            "विद्युत तरंगों की गूंज, ज़बरदस्त लेज़र बीम",
            listOf(c3.id, c4.id)
          )
          else -> Tuple9(
            "दृश्य $index: अभेद्य टाइटन शील्ड",
            "रास्ते के बीचों बीच खड़ा महाबली योद्धा, जहां दर्जनों राक्षस टकराकर गिरते हैं।",
            "${c5.name} जमीन पर मुक्का मारता है, शॉकवेव से जमीन फट जाती है।",
            "गंभीर दहाड़, मांसपेशियों में खिंचाव।",
            c5.name,
            "जब तक मेरी सांस चल रही है, कोई इस रेखा को पार नहीं करेगा!",
            "As long as I draw breath, none shall cross this line!",
            "धरती का कंपन, भारी धातु ढाल की टक्कर",
            listOf(c5.id, c1.id)
          )
        }
      }
      else -> {
        // Act 3: Climax & Victory
        when (index % 4) {
          1 -> Tuple9(
            "दृश्य $index: टोक्यो का हृदय",
            "रोप्पोंगी हिल्स के ऊपर चमकता विशालकाय ऊर्जा भंवर, बादलों में लाल बिजली कड़क रही है।",
            "पांचों मुख्य पात्र एक साथ खड़े होते हैं, पीछे सुबह की पहली किरण की आभा।",
            "एकजुटता, गर्व और पूर्ण आत्मविश्वास।",
            c1.name,
            "दोस्तों, यही वो क्षण है जिसके लिए हमने जन्म लिया था। एक साथ हमला!",
            "Friends, this is the moment we were born for. Strike together as one!",
            "महाकाव्य संगीत की गूंज, ऊर्जा का गर्जन",
            listOf(c1.id, c2.id, c3.id, c4.id, c5.id)
          )
          2 -> Tuple9(
            "दृश्य $index: अंतिम संयुक्त प्रहार",
            "आकाश में चमकती पांच अलग-अलग रंगों की ऊर्जा धाराएं (नीली, गुलाबी, लाल, नारंगी, हरी)।",
            "सभी पांचों पात्र अपनी अंतिम गुप्त तकनीक का प्रयोग करते हैं।",
            "अंतिम सीमा का पराक्रम, चमकते चेहरे।",
            c2.name,
            "यह टोक्यो का भविष्य है! नष्ट हो जाओ अंधकार!",
            "This is Tokyo's future! Perish into the void, darkness!",
            "विशाल सोनिक बूम, अंधकार का फटना",
            listOf(c1.id, c2.id, c3.id, c4.id, c5.id)
          )
          3 -> Tuple9(
            "दृश्य $index: भोर का प्रकाश",
            "टोक्यो के ऊपर से बादल छंट रहे हैं, माउंट फूजी की छाया में उगता हुआ लाल सूरज।",
            "पात्र अपनी श्वास सामान्य करते हुए शहर को देखते हैं।",
            "राहत, संतोष और हल्की मुस्कान।",
            c3.name,
            "अंधेरा मिट गया... आज का सवेरा टोक्यो का नया जन्म है।",
            "The darkness has lifted... today's dawn is Tokyo's rebirth.",
            "सुबह की हल्की हवा, पक्षियों की आवाज",
            listOf(c1.id, c3.id, c5.id)
          )
          else -> Tuple9(
            "दृश्य $index: अगला अध्याय",
            "शिबुया की सड़कों पर नई शुरुआत, पांचों पात्रों की परछाईं सूर्य की रोशनी में लंबी होती हुई।",
            "वे आगे कदम बढ़ाते हैं, एक नए युग की रक्षा के लिए तत्पर।",
            "दृढ़ संकल्प, भविष्य की ओर आशा भरी नजरें।",
            c1.name,
            "जब भी टोक्यो को हमारी जरूरत होगी, हम हमेशा तैयार रहेंगे।",
            "Whenever Tokyo calls upon us, we shall always stand ready.",
            "सुमधुर समापन संगीत, आउट्रो फेड",
            listOf(c1.id, c2.id, c4.id)
          )
        }
      }
    }

    return AnimeScene(
      id = id,
      sceneNumber = index,
      episodeNumber = 1,
      durationSeconds = duration,
      characterIds = charsPresent,
      title = title,
      backgroundDescription = bgDesc,
      characterActions = action,
      characterExpressions = expr,
      cameraAngle = camera,
      hindiDialogueSpeaker = speaker,
      hindiDialogueText = hindiText,
      englishTranslation = engText,
      soundEffect = soundFx,
      bgmTrack = bgm,
      transition = transition,
      presetDrawableRes = R.drawable.anime_bg_tokyo
    )
  }

  private fun defaultChar(name: String, role: String): AnimeCharacter {
    return AnimeCharacter(
      id = "char_${UUID.randomUUID().toString().take(6)}",
      name = name,
      age = 18,
      gender = "Male",
      personality = "Brave hero",
      hairColor = "Black",
      hairstyle = "Spiky",
      eyeColor = "Blue",
      clothing = "Anime Ronin Suit",
      heightCm = 175,
      specialAbilities = "Katana Strike",
      storyRole = role,
      hindiVoiceType = com.example.animeai.model.HindiVoiceType.HERO_MALE
    )
  }

  private data class Tuple9<A, B, C, D, E, F, G, H, I>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H,
    val ninth: I
  )
}
