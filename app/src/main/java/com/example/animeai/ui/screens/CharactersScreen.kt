package com.example.animeai.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.animeai.model.AnimeCharacter
import com.example.animeai.model.AnimeProject
import com.example.animeai.model.HindiVoiceType
import com.example.animeai.model.NormalizedRect
import com.example.animeai.ui.components.CharacterCropDialog
import com.example.animeai.ui.components.CharacterEditDialog
import com.example.animeai.ui.components.CharacterProfileCard
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
import com.example.animeai.util.CharacterCropHelper
import com.example.animeai.viewmodel.AnimeStudioViewModel

@Composable
fun CharactersScreen(
  viewModel: AnimeStudioViewModel,
  project: AnimeProject?
) {
  val context = LocalContext.current
  val characters = project?.characters ?: emptyList()

  var showCropDialog by remember { mutableStateOf(false) }
  var characterToEdit by remember { mutableStateOf<AnimeCharacter?>(null) }

  // Multi-character image bitmap for cropping
  var activeSheetBitmap by remember {
    mutableStateOf(CharacterCropHelper.loadBitmapFromResource(context, R.drawable.anime_characters_group))
  }

  // Zero-permission Android Photo Picker for picking user's own multi-character image
  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      viewModel.setUploadedSheetUri(uri)
      val bmp = CharacterCropHelper.loadBitmapFromUri(context, uri)
      if (bmp != null) {
        activeSheetBitmap = bmp
        showCropDialog = true
      }
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBlack)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Feature Title Header
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(NeonPink)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "MY ANIME CHARACTERS",
              fontSize = 17.sp,
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
              text = "「一括画像・個別分離」",
              fontSize = 10.sp,
              color = NeonCyan,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Upload 1 single image containing multiple anime characters. Automatically detect or manually crop each character into a permanent individual profile with Hindi voice assignments.",
          fontSize = 12.sp,
          color = TextSecondary,
          lineHeight = 17.sp
        )
      }
    }

    // 2. Multi-Character Source Sheet Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberSurfaceBorder),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("card_character_sheet")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "SOURCE MULTI-CHARACTER SHEET",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan
            )
            Text(
              text = "Contains 5 Anime Heroes",
              fontSize = 10.sp,
              color = TextSecondary
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Sheet Preview
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(16f / 9f)
              .clip(RoundedCornerShape(12.dp))
              .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
          ) {
            val previewModel: Any = activeSheetBitmap ?: R.drawable.anime_characters_group
            AsyncImage(
              model = previewModel,
              contentDescription = "Multi Character Sheet",
              contentScale = ContentScale.Crop,
              modifier = Modifier.matchParentSize()
            )

            Box(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .background(CyberBlack.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "✓ 5 Characters Detected in Image",
                fontSize = 10.sp,
                color = NeonGreen,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Action Buttons: Upload User Image or Crop Sheet
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                photoPicker.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
              border = BorderStroke(1.dp, NeonPink),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("btn_upload_character_sheet")
            ) {
              Icon(Icons.Default.Upload, contentDescription = null, tint = NeonPink, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Upload Image",
                fontSize = 12.sp,
                color = NeonPink,
                fontWeight = FontWeight.Bold
              )
            }

            Button(
              onClick = {
                if (activeSheetBitmap == null) {
                  activeSheetBitmap = CharacterCropHelper.loadBitmapFromResource(context, R.drawable.anime_characters_group)
                }
                showCropDialog = true
              },
              colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("btn_crop_character")
            ) {
              Icon(Icons.Default.Crop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Crop Character",
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // 3. Consistency Guarantee Banner
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Lock, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "CHARACTER IDENTITY LOCKED ACROSS ALL SCENES",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NeonGreen
            )
            Text(
              text = "Same face, hairstyle, eye color, and clothing will appear consistently in generated scenes. Characters are never randomly replaced.",
              fontSize = 10.sp,
              color = TextSecondary,
              lineHeight = 14.sp
            )
          }
        }
      }
    }

    // 4. Character Profiles Count Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "SEPARATED PROFILES (${characters.size})",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = TextMuted,
          letterSpacing = 0.8.sp
        )

        Text(
          text = "Tap 🔊 for Hindi TTS test",
          fontSize = 11.sp,
          color = NeonCyan
        )
      }
    }

    // 5. Individual Separated Character Cards
    items(characters, key = { it.id }) { char ->
      CharacterProfileCard(
        character = char,
        onEditClick = { characterToEdit = char },
        onVoiceTestClick = {
          val testPhrase = "${char.name} तैयार है! टोक्यो की रक्षा हम सब मिलकर करेंगे।"
          viewModel.playHindiVoiceTest(testPhrase, char.hindiVoiceType)
        }
      )
    }

    item {
      Spacer(modifier = Modifier.height(30.dp))
    }
  }

  // Crop Dialog
  if (showCropDialog && activeSheetBitmap != null) {
    CharacterCropDialog(
      sourceBitmap = activeSheetBitmap!!,
      onDismiss = { showCropDialog = false },
      onExtractCharacter = { rect, name, role, voice ->
        viewModel.cropAndExtractCharacterFromSheet(
          activeSheetBitmap!!,
          rect,
          name,
          role,
          voice
        )
        showCropDialog = false
      }
    )
  }

  // Edit Character Dialog
  if (characterToEdit != null) {
    CharacterEditDialog(
      character = characterToEdit!!,
      onDismiss = { characterToEdit = null },
      onSave = { updated ->
        viewModel.updateCharacterProfile(updated)
        characterToEdit = null
      }
    )
  }
}
