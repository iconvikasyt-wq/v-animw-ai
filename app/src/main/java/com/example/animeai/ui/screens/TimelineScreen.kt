package com.example.animeai.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.animeai.model.AnimeProject
import com.example.animeai.model.AnimeScene
import com.example.animeai.model.CameraAngle
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
fun TimelineScreen(
  viewModel: AnimeStudioViewModel,
  project: AnimeProject?
) {
  val isPlaying by viewModel.isPlaying.collectAsState()
  val activeIndex by viewModel.currentPlayingSceneIndex.collectAsState()
  val sceneProgress by viewModel.scenePlaybackProgress.collectAsState()

  val scenes = project?.scenes ?: emptyList()
  val currentScene = scenes.getOrNull(activeIndex) ?: scenes.firstOrNull()

  val activeChar = project?.characters?.find { it.id in (currentScene?.characterIds ?: emptyList()) }

  var editingScene by remember { mutableStateOf<AnimeScene?>(null) }

  // Animated Ken Burns scale & pan calculations
  val animatedScale by animateFloatAsState(
    targetValue = when (currentScene?.cameraAngle) {
      CameraAngle.ZOOM_IN -> 1.0f + (sceneProgress * 0.16f)
      CameraAngle.ZOOM_OUT -> 1.16f - (sceneProgress * 0.16f)
      else -> 1.06f
    },
    label = "ken_burns_scale"
  )

  val animatedPanX by animateFloatAsState(
    targetValue = when (currentScene?.cameraAngle) {
      CameraAngle.PAN_LEFT -> (1.0f - sceneProgress) * 30f
      CameraAngle.PAN_RIGHT -> -(sceneProgress) * 30f
      else -> 0f
    },
    label = "ken_burns_pan"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBlack)
  ) {
    // 1. VIDEO PLAYER CONTAINER (16:9)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
        .background(Color.Black)
        .border(1.dp, NeonPurple.copy(alpha = 0.5f))
        .testTag("anime_video_player")
    ) {
      if (currentScene != null) {
        // Background Scene with Ken Burns Motion
        Box(
          modifier = Modifier
            .matchParentSize()
            .graphicsLayer {
              scaleX = animatedScale
              scaleY = animatedScale
              translationX = animatedPanX
            }
        ) {
          AsyncImage(
            model = currentScene.presetDrawableRes ?: R.drawable.anime_bg_tokyo,
            contentDescription = "Scene Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
          )
        }

        // Vignette
        Box(
          modifier = Modifier
            .matchParentSize()
            .background(
              Brush.verticalGradient(
                listOf(
                  Color.Black.copy(alpha = 0.6f),
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.85f)
                )
              )
            )
        )

        // Character Portrait Overlay
        if (activeChar != null) {
          Box(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(start = 12.dp, bottom = 48.dp)
              .size(72.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(CyberBlack.copy(alpha = 0.9f))
              .border(2.dp, NeonCyan, RoundedCornerShape(10.dp))
          ) {
            val charModel = if (!activeChar.imagePath.isNullOrBlank()) {
              File(activeChar.imagePath)
            } else {
              activeChar.fallbackDrawableRes ?: R.drawable.anime_characters_group
            }
            AsyncImage(
              model = charModel,
              contentDescription = activeChar.name,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxWidth()
            )

            Box(
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(NeonPink)
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

        // Top HUD
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .background(CyberBlack.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = "「アニメAI」 दृश्य ${activeIndex + 1}/${scenes.size}",
              fontSize = 11.sp,
              color = NeonCyan,
              fontWeight = FontWeight.Bold
            )
          }

          Box(
            modifier = Modifier
              .background(CyberBlack.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = currentScene.cameraAngle.label,
              fontSize = 10.sp,
              color = NeonAmber,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        // Bottom Styled Subtitle Bar (Burned-In Karaoke Box)
        Box(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CyberBlack.copy(alpha = 0.92f))
            .border(1.dp, NeonPurple.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "► ${currentScene.hindiDialogueSpeaker}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeonAmber
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "• ${currentScene.soundEffect}",
                fontSize = 9.sp,
                color = TextSecondary
              )
            }
            Text(
              text = currentScene.hindiDialogueText,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              maxLines = 2
            )
          }
        }
      }
    }

    // Progress Bar for Current Scene
    LinearProgressIndicator(
      progress = { sceneProgress },
      modifier = Modifier
        .fillMaxWidth()
        .height(3.dp),
      color = NeonPink,
      trackColor = CyberSurfaceBorder
    )

    // Player Play/Pause Controls Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(CyberSurface)
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = {
            if (activeIndex > 0) viewModel.seekToScene(activeIndex - 1)
          },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(Icons.Default.FastRewind, contentDescription = "Prev Scene", tint = TextPrimary)
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = { viewModel.togglePlayback() },
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(NeonPurple)
            .testTag("btn_toggle_playback")
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = "Play/Pause",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = {
            if (activeIndex < scenes.size - 1) viewModel.seekToScene(activeIndex + 1)
          },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(Icons.Default.FastForward, contentDescription = "Next Scene", tint = TextPrimary)
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = if (isPlaying) "Playing Anime Video" else "Paused",
          fontSize = 11.sp,
          color = if (isPlaying) NeonGreen else TextSecondary,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Scene ${activeIndex + 1} of ${scenes.size}",
          fontSize = 10.sp,
          color = TextMuted
        )
      }
    }

    // 2. TIMELINE SCENES LIST
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "EPISODE TIMELINE (${scenes.size} SCENES)",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = NeonCyan,
        letterSpacing = 0.8.sp
      )

      Button(
        onClick = {
          val newSc = AnimeScene(
            id = "scene_${System.currentTimeMillis()}",
            sceneNumber = scenes.size + 1,
            title = "दृश्य ${scenes.size + 1}: नया मोड़",
            backgroundDescription = "टोक्यो की रात में नया संघर्ष",
            characterActions = "पात्र आगे बढ़ते हैं",
            characterExpressions = "दृढ़ संकल्प",
            cameraAngle = CameraAngle.ZOOM_IN,
            hindiDialogueSpeaker = project?.characters?.firstOrNull()?.name ?: "रेन्जिरो",
            hindiDialogueText = "हम हार नहीं मानेंगे!",
            presetDrawableRes = R.drawable.anime_bg_tokyo
          )
          viewModel.updateScene(newSc)
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
        border = BorderStroke(1.dp, NeonPurple),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(32.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "Add Scene", fontSize = 11.sp, color = NeonPurple, fontWeight = FontWeight.Bold)
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      itemsIndexed(scenes, key = { _, s -> s.id }) { index, sc ->
        val isSelected = activeIndex == index
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyberCard else CyberSurfaceVariant
          ),
          border = BorderStroke(
            1.dp,
            if (isSelected) NeonCyan else CyberSurfaceBorder
          ),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.seekToScene(index) }
            .testTag("timeline_scene_item_$index")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Scene Number Indicator
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) NeonCyan else CyberBlack),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${sc.sceneNumber}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) CyberBlack else TextPrimary
              )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Scene Details
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = sc.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = "${sc.hindiDialogueSpeaker}: “${sc.hindiDialogueText.take(28)}...”",
                fontSize = 10.sp,
                color = TextSecondary
              )
              Text(
                text = "${sc.cameraAngle.label} • ${sc.durationSeconds}s • ${sc.bgmTrack.title.take(16)}",
                fontSize = 9.sp,
                color = NeonAmber
              )
            }

            // Edit & Reorder Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = { editingScene = sc },
                modifier = Modifier.size(30.dp)
              ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Scene", tint = TextSecondary, modifier = Modifier.size(15.dp))
              }

              IconButton(
                onClick = { if (index > 0) viewModel.reorderScenes(index, index - 1) },
                enabled = index > 0,
                modifier = Modifier.size(30.dp)
              ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = if (index > 0) NeonCyan else TextMuted, modifier = Modifier.size(15.dp))
              }

              IconButton(
                onClick = { if (index < scenes.size - 1) viewModel.reorderScenes(index, index + 1) },
                enabled = index < scenes.size - 1,
                modifier = Modifier.size(30.dp)
              ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = if (index < scenes.size - 1) NeonCyan else TextMuted, modifier = Modifier.size(15.dp))
              }

              IconButton(
                onClick = { viewModel.deleteScene(sc.id) },
                modifier = Modifier.size(30.dp)
              ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Scene", tint = NeonPink, modifier = Modifier.size(15.dp))
              }
            }
          }
        }
      }
    }
  }

  // Edit Scene Dialog
  if (editingScene != null) {
    EditSceneDialog(
      scene = editingScene!!,
      onDismiss = { editingScene = null },
      onSave = { updated ->
        viewModel.updateScene(updated)
        editingScene = null
      }
    )
  }
}

@Composable
fun EditSceneDialog(
  scene: AnimeScene,
  onDismiss: () -> Unit,
  onSave: (AnimeScene) -> Unit
) {
  var title by remember { mutableStateOf(scene.title) }
  var speaker by remember { mutableStateOf(scene.hindiDialogueSpeaker) }
  var dialogue by remember { mutableStateOf(scene.hindiDialogueText) }
  var actions by remember { mutableStateOf(scene.characterActions) }
  var durationStr by remember { mutableStateOf(scene.durationSeconds.toString()) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = CyberSurface,
      border = BorderStroke(1.dp, NeonCyan),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "Edit Scene ${scene.sceneNumber}",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Scene Title") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = CyberSurfaceBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = speaker,
          onValueChange = { speaker = it },
          label = { Text("Speaker Name") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = CyberSurfaceBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = dialogue,
          onValueChange = { dialogue = it },
          label = { Text("Hindi Dialogue") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = CyberSurfaceBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          minLines = 2,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = durationStr,
          onValueChange = { durationStr = it },
          label = { Text("Duration (Seconds)") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = CyberSurfaceBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = {
            val updated = scene.copy(
              title = title,
              hindiDialogueSpeaker = speaker,
              hindiDialogueText = dialogue,
              durationSeconds = durationStr.toIntOrNull() ?: scene.durationSeconds
            )
            onSave(updated)
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(text = "Save Scene Changes", color = CyberBlack, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
