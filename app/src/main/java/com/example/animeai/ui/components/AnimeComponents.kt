package com.example.animeai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.animeai.model.AnimeCharacter
import com.example.animeai.ui.theme.AccentKanji
import com.example.animeai.ui.theme.CyberBlack
import com.example.animeai.ui.theme.CyberCard
import com.example.animeai.ui.theme.CyberSurface
import com.example.animeai.ui.theme.CyberSurfaceBorder
import com.example.animeai.ui.theme.NeonAmber
import com.example.animeai.ui.theme.NeonCyan
import com.example.animeai.ui.theme.NeonGreen
import com.example.animeai.ui.theme.NeonPink
import com.example.animeai.ui.theme.NeonPurple
import com.example.animeai.ui.theme.TextMuted
import com.example.animeai.ui.theme.TextPrimary
import com.example.animeai.ui.theme.TextSecondary
import com.example.animeai.viewmodel.StudioTab
import java.io.File

@Composable
fun AnimeStudioHeader(
  title: String,
  totalScenes: Int,
  durationMinutes: Int
) {
  val infiniteTransition = rememberInfiniteTransition(label = "neon_glow")
  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 0.9f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow_alpha"
  )

  Surface(
    color = CyberSurface,
    border = BorderStroke(1.dp, CyberSurfaceBorder),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("anime_studio_header")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
              Brush.linearGradient(listOf(NeonPurple, NeonCyan))
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "AI",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "ANIME AI",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 17.sp,
              color = TextPrimary,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(NeonPurple.copy(alpha = 0.25f))
                .border(1.dp, NeonPurple.copy(alpha = glowAlpha), RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
              Text(
                text = "「日本アニメ」",
                fontSize = 10.sp,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Text(
            text = title,
            fontSize = 11.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberBlack)
            .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "${durationMinutes}m • ${totalScenes} Scenes",
            color = NeonPink,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}

@Composable
fun AnimeTabNavigation(
  selectedTab: StudioTab,
  onTabSelected: (StudioTab) -> Unit
) {
  ScrollableTabRow(
    selectedTabIndex = selectedTab.ordinal,
    containerColor = CyberSurface,
    contentColor = TextPrimary,
    edgePadding = 12.dp,
    divider = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(CyberSurfaceBorder)
      )
    },
    indicator = { tabPositions ->
      if (selectedTab.ordinal < tabPositions.size) {
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
          height = 3.dp,
          color = NeonCyan
        )
      }
    },
    modifier = Modifier
      .fillMaxWidth()
      .testTag("anime_tabs_bar")
  ) {
    StudioTab.values().forEach { tab ->
      val isSelected = selectedTab == tab
      Tab(
        selected = isSelected,
        onClick = { onTabSelected(tab) },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = tab.iconKanji,
              fontSize = 11.sp,
              color = if (isSelected) NeonPink else TextMuted,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = tab.label,
              fontSize = 13.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) TextPrimary else TextSecondary
            )
          }
        },
        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
      )
    }
  }
}

@Composable
fun CharacterProfileCard(
  character: AnimeCharacter,
  onEditClick: () -> Unit,
  onVoiceTestClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = CyberCard),
    border = BorderStroke(1.dp, CyberSurfaceBorder),
    modifier = modifier
      .fillMaxWidth()
      .testTag("character_card_${character.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Character Avatar with Neon Glow
      Box(
        modifier = Modifier
          .size(76.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(CyberBlack)
          .border(1.5.dp, NeonCyan, RoundedCornerShape(12.dp))
      ) {
        val model = if (!character.imagePath.isNullOrBlank()) {
          File(character.imagePath)
        } else {
          character.fallbackDrawableRes ?: R.drawable.anime_characters_group
        }

        AsyncImage(
          model = model,
          contentDescription = character.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxWidth()
        )

        // Gender Badge
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .background(CyberBlack.copy(alpha = 0.85f), RoundedCornerShape(topStart = 6.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
          Text(
            text = character.gender.take(1),
            fontSize = 9.sp,
            color = if (character.gender.startsWith("F", ignoreCase = true)) NeonPink else NeonCyan,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Character Info
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = character.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(NeonPurple.copy(alpha = 0.2f))
              .border(0.8.dp, NeonPurple.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = character.storyRole,
              fontSize = 9.sp,
              color = NeonPurple,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "Age ${character.age} • ${character.heightCm}cm • ${character.hairColor}",
          fontSize = 11.sp,
          color = TextSecondary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "⚡ ${character.specialAbilities}",
          fontSize = 11.sp,
          color = NeonAmber,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Hindi Voice: ${character.hindiVoiceType.displayName}",
            fontSize = 10.sp,
            color = NeonCyan,
            fontWeight = FontWeight.Medium
          )
        }
      }

      Spacer(modifier = Modifier.width(6.dp))

      // Action Buttons
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
          onClick = onVoiceTestClick,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(NeonCyan.copy(alpha = 0.15f))
            .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape)
            .testTag("btn_voice_test_${character.id}")
        ) {
          Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Test Hindi Voice",
            tint = NeonCyan,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        IconButton(
          onClick = onEditClick,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(CyberSurfaceBorder)
            .testTag("btn_edit_${character.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Profile",
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
