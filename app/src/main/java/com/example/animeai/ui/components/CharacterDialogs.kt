package com.example.animeai.ui.components

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.animeai.model.AnimeCharacter
import com.example.animeai.model.HindiVoiceType
import com.example.animeai.model.NormalizedRect
import com.example.animeai.ui.theme.CyberBlack
import com.example.animeai.ui.theme.CyberCard
import com.example.animeai.ui.theme.CyberSurface
import com.example.animeai.ui.theme.CyberSurfaceBorder
import com.example.animeai.ui.theme.CyberSurfaceVariant
import com.example.animeai.ui.theme.NeonAmber
import com.example.animeai.ui.theme.NeonCyan
import com.example.animeai.ui.theme.NeonPink
import com.example.animeai.ui.theme.NeonPurple
import com.example.animeai.ui.theme.TextPrimary
import com.example.animeai.ui.theme.TextSecondary
import com.example.animeai.util.CharacterCropHelper

@Composable
fun CharacterCropDialog(
  sourceBitmap: Bitmap,
  onDismiss: () -> Unit,
  onExtractCharacter: (rect: NormalizedRect, name: String, role: String, voiceType: HindiVoiceType) -> Unit
) {
  val defaultSlots = remember { CharacterCropHelper.detectDefaultSlots() }
  var selectedSlotIndex by remember { mutableStateOf(0) }

  var characterName by remember { mutableStateOf("नया पात्र (New Character)") }
  var storyRole by remember { mutableStateOf("योद्धा (Warrior)") }
  var selectedVoice by remember { mutableStateOf(HindiVoiceType.HERO_MALE) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = CyberSurface,
      border = BorderStroke(1.5.dp, NeonPurple),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("dialog_character_crop")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Crop, contentDescription = null, tint = NeonCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Crop Character From Sheet",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Select character region from the multi-character uploaded image:",
          fontSize = 12.sp,
          color = TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Image Preview with Slot Overlays
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(12.dp))
        ) {
          AsyncImage(
            model = sourceBitmap,
            contentDescription = "Character Sheet",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
          )

          // 5 Slot bounding box outlines
          Row(modifier = Modifier.matchParentSize()) {
            defaultSlots.forEachIndexed { index, rect ->
              val isSelected = selectedSlotIndex == index
              Box(
                modifier = Modifier
                  .weight(1f)
                  .padding(2.dp)
                  .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) NeonPink else NeonCyan.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(6.dp)
                  )
                  .background(
                    if (isSelected) NeonPurple.copy(alpha = 0.25f) else Color.Transparent
                  )
                  .clickable { selectedSlotIndex = index },
                contentAlignment = Alignment.TopCenter
              ) {
                Box(
                  modifier = Modifier
                    .padding(4.dp)
                    .background(CyberBlack.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "Char #${index + 1}",
                    fontSize = 9.sp,
                    color = if (isSelected) NeonPink else NeonCyan,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Character Name Field
        OutlinedTextField(
          value = characterName,
          onValueChange = { characterName = it },
          label = { Text("Character Name (Hindi / English)") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = CyberSurfaceBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_crop_name")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Story Role
        OutlinedTextField(
          value = storyRole,
          onValueChange = { storyRole = it },
          label = { Text("Story Role (e.g. Lead Hero, Ninja, Mage)") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = CyberSurfaceBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Select Hindi Voice Type:",
          fontSize = 12.sp,
          color = TextSecondary,
          fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          HindiVoiceType.values().forEach { v ->
            val isSelected = selectedVoice == v
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) NeonPurple.copy(alpha = 0.2f) else CyberSurfaceVariant)
                .border(
                  1.dp,
                  if (isSelected) NeonPurple else Color.Transparent,
                  RoundedCornerShape(8.dp)
                )
                .clickable { selectedVoice = v }
                .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
              Text(
                text = v.displayName,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) NeonCyan else TextPrimary,
                modifier = Modifier.weight(1f)
              )
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            onExtractCharacter(
              defaultSlots[selectedSlotIndex],
              characterName,
              storyRole,
              selectedVoice
            )
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_save_cropped_char")
        ) {
          Text(
            text = "Extract & Save Character Profile",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
      }
    }
  }
}

@Composable
fun CharacterEditDialog(
  character: AnimeCharacter,
  onDismiss: () -> Unit,
  onSave: (AnimeCharacter) -> Unit
) {
  var name by remember { mutableStateOf(character.name) }
  var ageStr by remember { mutableStateOf(character.age.toString()) }
  var gender by remember { mutableStateOf(character.gender) }
  var personality by remember { mutableStateOf(character.personality) }
  var hairColor by remember { mutableStateOf(character.hairColor) }
  var hairstyle by remember { mutableStateOf(character.hairstyle) }
  var eyeColor by remember { mutableStateOf(character.eyeColor) }
  var clothing by remember { mutableStateOf(character.clothing) }
  var heightStr by remember { mutableStateOf(character.heightCm.toString()) }
  var abilities by remember { mutableStateOf(character.specialAbilities) }
  var storyRole by remember { mutableStateOf(character.storyRole) }
  var voiceType by remember { mutableStateOf(character.hindiVoiceType) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = CyberSurface,
      border = BorderStroke(1.5.dp, NeonCyan),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("dialog_character_edit")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Edit Character Profile",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Character Name") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = CyberSurfaceBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = ageStr,
            onValueChange = { ageStr = it },
            label = { Text("Age") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = CyberSurfaceBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.weight(1f)
          )

          Spacer(modifier = Modifier.width(8.dp))

          OutlinedTextField(
            value = heightStr,
            onValueChange = { heightStr = it },
            label = { Text("Height (cm)") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = CyberSurfaceBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = gender,
          onValueChange = { gender = it },
          label = { Text("Gender (Male/Female/Android)") },
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
          value = storyRole,
          onValueChange = { storyRole = it },
          label = { Text("Story Role") },
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
          value = hairColor,
          onValueChange = { hairColor = it },
          label = { Text("Hair Color") },
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
          value = eyeColor,
          onValueChange = { eyeColor = it },
          label = { Text("Eye Color") },
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
          value = clothing,
          onValueChange = { clothing = it },
          label = { Text("Clothing & Outfit") },
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
          value = abilities,
          onValueChange = { abilities = it },
          label = { Text("Special Abilities") },
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
          value = personality,
          onValueChange = { personality = it },
          label = { Text("Personality") },
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
            val updated = character.copy(
              name = name,
              age = ageStr.toIntOrNull() ?: character.age,
              heightCm = heightStr.toIntOrNull() ?: character.heightCm,
              gender = gender,
              storyRole = storyRole,
              hairColor = hairColor,
              hairstyle = hairstyle,
              eyeColor = eyeColor,
              clothing = clothing,
              specialAbilities = abilities,
              personality = personality,
              hindiVoiceType = voiceType
            )
            onSave(updated)
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_save_edited_char")
        ) {
          Text(
            text = "Save Profile Changes",
            color = CyberBlack,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
      }
    }
  }
}
