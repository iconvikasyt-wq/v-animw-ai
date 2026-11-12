package com.example.animeai.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.animeai.generator.AnimeStoryEngine
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
import com.example.animeai.viewmodel.StudioTab

@Composable
fun StudioScreen(
  viewModel: AnimeStudioViewModel,
  project: AnimeProject?
) {
  var storyPrompt by remember {
    mutableStateOf(
      project?.storyPrompt ?: "Create a 15-minute Japanese zombie horror anime in Tokyo with 5 main characters."
    )
  }
  var projectTitle by remember {
    mutableStateOf(project?.title ?: "टोक्यो ज़ोंबी हॉरर 2088 (Tokyo Zombie Horror)")
  }
  var selectedDuration by remember {
    mutableStateOf(project?.targetDurationMinutes ?: 15)
  }

  val durationOptions = listOf(5, 10, 15, 20, 30)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBlack)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    // Top Hero Banner
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = CyberCard),
      border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("card_studio_hero")
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.horizontalGradient(
              listOf(
                NeonPurple.copy(alpha = 0.15f),
                CyberSurfaceVariant,
                NeonCyan.copy(alpha = 0.15f)
              )
            )
          )
          .padding(16.dp)
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "ONE PROMPT ANIME GENERATOR",
              fontSize = 14.sp,
              fontWeight = FontWeight.Black,
              color = NeonCyan,
              letterSpacing = 1.sp
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Generate a full Japanese 2D/3D anime episode with consistent characters, 30-60 scenes, camera directions & Hindi voices.",
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 17.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Project Title
    Text(
      text = "PROJECT TITLE",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = TextMuted,
      letterSpacing = 0.8.sp
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
      value = projectTitle,
      onValueChange = { projectTitle = it },
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NeonCyan,
        unfocusedBorderColor = CyberSurfaceBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedContainerColor = CyberSurface,
        unfocusedContainerColor = CyberSurface
      ),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("input_project_title")
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Story Prompt Box (Large as requested in prompt)
    Text(
      text = "STORY PROMPT",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = TextMuted,
      letterSpacing = 0.8.sp
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
      value = storyPrompt,
      onValueChange = { storyPrompt = it },
      minLines = 4,
      maxLines = 6,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NeonPurple,
        unfocusedBorderColor = CyberSurfaceBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedContainerColor = CyberSurface,
        unfocusedContainerColor = CyberSurface
      ),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("input_story_prompt")
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Presets quick chips
    Text(
      text = "TRY ANIME TEMPLATES:",
      fontSize = 10.sp,
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
      AnimeStoryEngine.PRESET_PROMPTS.forEachIndexed { idx, preset ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurfaceVariant)
            .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(8.dp))
            .clickable { storyPrompt = preset }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("chip_preset_$idx")
        ) {
          Text(
            text = preset.take(34) + "...",
            fontSize = 11.sp,
            color = NeonCyan,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Duration Selector (5, 10, 15, 20, 30 min)
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Default.Schedule, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "EPISODE DURATION & SCENE COUNT",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextMuted
      )
    }
    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      durationOptions.forEach { dur ->
        val isSelected = selectedDuration == dur
        val sceneEst = when (dur) {
          5 -> "18 Sc"
          10 -> "30 Sc"
          15 -> "42 Sc"
          20 -> "50 Sc"
          else -> "60 Sc"
        }
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) NeonPurple else CyberSurfaceVariant)
            .border(
              1.dp,
              if (isSelected) NeonPink else CyberSurfaceBorder,
              RoundedCornerShape(10.dp)
            )
            .clickable { selectedDuration = dur }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${dur}m",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) Color.White else TextPrimary
            )
            Text(
              text = sceneEst,
              fontSize = 10.sp,
              color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Consistency Lock & Character Preview Section
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
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "LOCKED CONSISTENT CHARACTERS",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NeonGreen
            )
          }

          Text(
            text = "${project?.characters?.size ?: 5} Profiles",
            fontSize = 10.sp,
            color = TextSecondary
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          project?.characters?.forEach { c ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(CyberBlack)
                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "${c.name} (${c.hairColor})",
                fontSize = 10.sp,
                color = TextPrimary
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Primary Neon Generation Button
    Button(
      onClick = {
        viewModel.generateNewAnime(projectTitle, storyPrompt, selectedDuration)
      },
      colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .testTag("btn_generate_anime")
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonAmber)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Generate Full Anime (${selectedDuration} Min • Multi-Scene)",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Synopsis and Scene breakdown of active project
    if (project != null) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "SYNOPSIS & STORYLINE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = project.synopsis,
            fontSize = 12.sp,
            color = TextPrimary,
            lineHeight = 18.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Total Scenes: ${project.scenes.size}",
              fontSize = 11.sp,
              color = TextSecondary
            )
            Text(
              text = "Est. Runtime: ~${project.targetDurationMinutes} Minutes",
              fontSize = 11.sp,
              color = NeonAmber
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = { viewModel.setTab(StudioTab.TIMELINE) },
            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
            border = BorderStroke(1.dp, NeonCyan),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("btn_view_in_timeline")
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Open in Video Timeline Player",
              color = NeonCyan,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }
    }
  }
}
