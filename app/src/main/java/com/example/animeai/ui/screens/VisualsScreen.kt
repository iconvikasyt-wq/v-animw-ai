package com.example.animeai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.animeai.model.AnimeProject
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
import java.io.File

@Composable
fun VisualsScreen(
  viewModel: AnimeStudioViewModel,
  project: AnimeProject?
) {
  val scenes = project?.scenes ?: emptyList()
  var selectedSceneIndex by remember { mutableStateOf(0) }
  val currentScene = scenes.getOrNull(selectedSceneIndex) ?: scenes.firstOrNull()

  val activeChar = project?.characters?.find { it.id in (currentScene?.characterIds ?: emptyList()) }

  val visualStyles = listOf(
    "Japanese 3D Anime (Ufotable Style)",
    "Cyberpunk Tokyo Neon (MAPPA Style)",
    "Cinematic Horror Shading",
    "Classic Cel-Shaded 2D Animation"
  )
  var selectedStyle by remember { mutableStateOf(visualStyles[0]) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBlack)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "JAPANESE 3D ANIME VISUALS",
          fontSize = 15.sp,
          fontWeight = FontWeight.Black,
          color = NeonCyan,
          letterSpacing = 1.sp
        )
        Text(
          text = "Scene-by-scene anime art with consistent character conditioning",
          fontSize = 11.sp,
          color = TextSecondary
        )
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(NeonCyan.copy(alpha = 0.2f))
          .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
          .padding(horizontal = 8.dp, vertical = 3.dp)
      ) {
        Text(
          text = "「作画監督」",
          fontSize = 10.sp,
          color = NeonCyan,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Scene Selector Carousel
    Text(
      text = "SELECT SCENE (${scenes.size} TOTAL):",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = TextMuted
    )
    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      scenes.forEachIndexed { index, sc ->
        val isSelected = selectedSceneIndex == index
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NeonPurple else CyberSurfaceVariant)
            .border(
              1.dp,
              if (isSelected) NeonPink else CyberSurfaceBorder,
              RoundedCornerShape(8.dp)
            )
            .clickable { selectedSceneIndex = index }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("visual_scene_chip_$index")
        ) {
          Text(
            text = "Scene ${sc.sceneNumber}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else TextSecondary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Main Visual Preview Card
    if (currentScene != null) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.5.dp, NeonPurple.copy(alpha = 0.6f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("card_scene_visual_preview")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // Visual Display with Overlays
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(16f / 9f)
              .clip(RoundedCornerShape(12.dp))
              .background(CyberBlack)
          ) {
            // Background Tokyo Scene
            AsyncImage(
              model = currentScene.presetDrawableRes ?: R.drawable.anime_bg_tokyo,
              contentDescription = "Scene Background",
              contentScale = ContentScale.Crop,
              modifier = Modifier.matchParentSize()
            )

            // Character Consistency Reference Overlay
            if (activeChar != null) {
              Box(
                modifier = Modifier
                  .align(Alignment.BottomStart)
                  .padding(10.dp)
                  .size(80.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(CyberBlack.copy(alpha = 0.85f))
                  .border(2.dp, NeonCyan, RoundedCornerShape(10.dp))
              ) {
                val model = if (!activeChar.imagePath.isNullOrBlank()) {
                  File(activeChar.imagePath)
                } else {
                  activeChar.fallbackDrawableRes ?: R.drawable.anime_characters_group
                }
                AsyncImage(
                  model = model,
                  contentDescription = activeChar.name,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxWidth()
                )

                Box(
                  modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(NeonPink.copy(alpha = 0.9f))
                    .padding(vertical = 1.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = activeChar.name.take(6),
                    fontSize = 8.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            // HUD Badges
            Row(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .background(CyberBlack.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                  .padding(horizontal = 6.dp, vertical = 3.dp)
              ) {
                Text(
                  text = "🎥 ${currentScene.cameraAngle.label}",
                  fontSize = 10.sp,
                  color = NeonCyan,
                  fontWeight = FontWeight.Bold
                )
              }

              Box(
                modifier = Modifier
                  .background(CyberBlack.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                  .padding(horizontal = 6.dp, vertical = 3.dp)
              ) {
                Text(
                  text = "⏱ ${currentScene.durationSeconds}s",
                  fontSize = 10.sp,
                  color = NeonAmber,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Scene Title & Info
          Text(
            text = currentScene.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Environment: ${currentScene.backgroundDescription}",
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 16.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Action: ${currentScene.characterActions}",
            fontSize = 12.sp,
            color = NeonAmber,
            lineHeight = 16.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Style Switcher
          Text(
            text = "ART DIRECTION STYLE:",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            visualStyles.forEach { style ->
              val isSelected = selectedStyle == style
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CyberSurfaceVariant)
                  .border(1.dp, if (isSelected) NeonCyan else CyberSurfaceBorder, RoundedCornerShape(6.dp))
                  .clickable { selectedStyle = style }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = style,
                  fontSize = 10.sp,
                  color = if (isSelected) NeonCyan else TextSecondary,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Regenerate Button
          Button(
            onClick = {
              viewModel.tts.speak("दृश्य ${currentScene.sceneNumber} का नया जापानी एनिमेशन रेंडर हो रहा है।", com.example.animeai.model.HindiVoiceType.HERO_MALE)
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
            border = BorderStroke(1.dp, NeonPurple),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("btn_regenerate_visual")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonPurple)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Regenerate Scene Visual (${selectedStyle.take(15)}...)",
              fontSize = 13.sp,
              color = NeonPurple,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
