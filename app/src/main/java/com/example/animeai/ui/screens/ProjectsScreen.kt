package com.example.animeai.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsScreen(
  viewModel: AnimeStudioViewModel,
  currentProject: AnimeProject?
) {
  val projects by viewModel.projectsList.collectAsState()
  val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBlack)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Folder, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(22.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "SAVED ANIME PROJECTS",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = 1.sp
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NeonPurple.copy(alpha = 0.2f))
            .border(1.dp, NeonPurple.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = "${projects.size} Projects Saved",
            fontSize = 10.sp,
            color = NeonPurple,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // Projects List
    items(projects, key = { it.id }) { proj ->
      val isActive = proj.id == currentProject?.id
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(
          1.5.dp,
          if (isActive) NeonCyan else CyberSurfaceBorder
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { viewModel.switchProject(proj) }
          .testTag("project_card_${proj.id}")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = proj.title,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary,
              modifier = Modifier.weight(1f)
            )

            if (isActive) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(NeonGreen.copy(alpha = 0.2f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(text = "Active", fontSize = 10.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Prompt: “${proj.storyPrompt.take(65)}...”",
            fontSize = 11.sp,
            color = TextSecondary
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${proj.targetDurationMinutes} min • ${proj.scenes.size} Scenes • ${proj.characters.size} Characters",
              fontSize = 11.sp,
              color = NeonAmber,
              fontWeight = FontWeight.SemiBold
            )

            Text(
              text = dateFormat.format(Date(proj.updatedAt)),
              fontSize = 10.sp,
              color = TextMuted
            )
          }
        }
      }
    }

    // Free AI Architecture & Specifications Info (Requirement 13 from prompt)
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
        border = BorderStroke(1.dp, CyberSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "FREE & OPEN-SOURCE ARCHITECTURE",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "• 100% Free: No subscriptions, credit cards or paid tokens required.\n" +
                "• Video Engine: Native Android MediaCodec (H.264 AVC) & MediaMuxer hardware encoder.\n" +
                "• Voice Engine: Local Android TextToSpeech engine with Hindi (hi-IN) locale.\n" +
                "• Soundtrack: Synthesized multi-track anime audio buffer synthesis.\n" +
                "• Storage: Local persistent JSON & Bitmap cache.",
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 16.sp
          )
        }
      }
    }

    item {
      Button(
        onClick = { viewModel.setTab(StudioTab.STUDIO) },
        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "Create New Anime Project", fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }
    }

    item {
      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}
