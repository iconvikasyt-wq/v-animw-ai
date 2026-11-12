package com.example.animeai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.animeai.model.AnimeProject
import com.example.animeai.model.BgmTrack
import com.example.animeai.model.HindiVoiceType
import com.example.animeai.ui.theme.CyberBlack
import com.example.animeai.ui.theme.CyberCard
import com.example.animeai.ui.theme.CyberSurface
import com.example.animeai.ui.theme.CyberSurfaceBorder
import com.example.animeai.ui.theme.CyberSurfaceVariant
import com.example.animeai.ui.theme.NeonAmber
import com.example.animeai.ui.theme.NeonCyan
import com.example.animeai.ui.theme.NeonGreen
import com.example.animeai.ui.theme.NeonPink
import com.example.animeai.ui.theme.NeonPurple
import com.example.animeai.ui.theme.TextMuted
import com.example.animeai.ui.theme.TextPrimary
import com.example.animeai.ui.theme.TextSecondary
import com.example.animeai.viewmodel.AnimeStudioViewModel

@Composable
fun VoicesScreen(
  viewModel: AnimeStudioViewModel,
  project: AnimeProject?
) {
  val characters = project?.characters ?: emptyList()
  val scenes = project?.scenes ?: emptyList()

  val isSpeaking by viewModel.tts.isSpeaking.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBlack)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "HINDI VOICE GENERATOR",
              fontSize = 16.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary,
              letterSpacing = 1.sp
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(NeonGreen.copy(alpha = 0.2f))
              .border(1.dp, NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = "Hindi (hi-IN) Active",
              fontSize = 10.sp,
              color = NeonGreen,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Local speech synthesis with custom pitch & speed for male heroes, heroines, villains and narrator.",
          fontSize = 12.sp,
          color = TextSecondary
        )
      }
    }

    // 2. Character Voice Mapping Cards
    item {
      Text(
        text = "CHARACTER VOICE ROSTER:",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextMuted,
        letterSpacing = 0.8.sp
      )
    }

    items(characters, key = { it.id }) { char ->
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = char.name,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${char.hindiVoiceType.displayName} • ${char.hindiVoiceType.description}",
              fontSize = 11.sp,
              color = NeonCyan
            )
            Text(
              text = "Pitch: ${char.hindiVoiceType.pitch}x • Speed: ${char.hindiVoiceType.speechRate}x",
              fontSize = 10.sp,
              color = TextSecondary
            )
          }

          IconButton(
            onClick = {
              val sampleLine = "${char.name} यहाँ है! टोक्यो का भविष्य अब हमारे हाथों में है।"
              viewModel.playHindiVoiceTest(sampleLine, char.hindiVoiceType)
            },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(NeonPurple.copy(alpha = 0.2f))
              .border(1.dp, NeonPurple, CircleShape)
              .testTag("btn_voice_test_row_${char.id}")
          ) {
            Icon(Icons.Default.VolumeUp, contentDescription = "Test Voice", tint = NeonPurpleBright)
          }
        }
      }
    }

    // 3. Background Music (BGM) Synthesizer Hub
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.MusicNote, contentDescription = null, tint = NeonAmber)
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "ANIME BACKGROUND SOUNDTRACKS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NeonAmber
              )
            }

            if (viewModel.audioEngine.isPlaying) {
              Text(
                text = "▶ PLAYING",
                fontSize = 10.sp,
                color = NeonGreen,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BgmTrack.values().forEach { track ->
              val isPlayingThis = viewModel.audioEngine.isPlaying && viewModel.audioEngine.currentTrack == track
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isPlayingThis) NeonAmber.copy(alpha = 0.2f) else CyberSurfaceVariant)
                  .border(
                    1.dp,
                    if (isPlayingThis) NeonAmber else CyberSurfaceBorder,
                    RoundedCornerShape(8.dp)
                  )
                  .clickable { viewModel.playBgmTest(track) }
                  .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(
                    text = track.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlayingThis) NeonAmber else TextPrimary
                  )
                  Text(
                    text = "${track.mood} • ${track.bpm} BPM",
                    fontSize = 10.sp,
                    color = TextSecondary
                  )
                }

                Icon(
                  imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                  contentDescription = null,
                  tint = if (isPlayingThis) NeonAmber else NeonCyan,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    }

    // 4. Scene Dialogues & Subtitle Inspector
    item {
      Text(
        text = "SCENE HINDI DIALOGUES (${scenes.size} SCENES):",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextMuted,
        letterSpacing = 0.8.sp
      )
    }

    items(scenes.take(15), key = { it.id }) { scene ->
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
        border = BorderStroke(1.dp, CyberSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Scene ${scene.sceneNumber}: ${scene.hindiDialogueSpeaker}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan
            )

            IconButton(
              onClick = {
                val activeChar = characters.find { it.id in scene.characterIds }
                val voice = activeChar?.hindiVoiceType ?: HindiVoiceType.HERO_MALE
                viewModel.tts.speak(scene.hindiDialogueText, voice)
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.Default.VolumeUp, contentDescription = "Play Dialogue", tint = NeonCyan, modifier = Modifier.size(18.dp))
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Hindi Dialogue
          Text(
            text = "“${scene.hindiDialogueText}”",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            lineHeight = 19.sp
          )

          Spacer(modifier = Modifier.height(4.dp))

          // Subtitle translation
          Text(
            text = scene.englishTranslation,
            fontSize = 11.sp,
            color = TextSecondary
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}

private val NeonPurpleBright = Color(0xFFC084FC)
