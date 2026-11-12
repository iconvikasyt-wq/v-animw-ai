package com.example.animeai.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
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

@Composable
fun ExportScreen(
  viewModel: AnimeStudioViewModel,
  project: AnimeProject?
) {
  val context = LocalContext.current

  val isExporting by viewModel.isExporting.collectAsState()
  val progress by viewModel.exportProgress.collectAsState()
  val statusMessage by viewModel.exportStatusMessage.collectAsState()
  val exportedFile by viewModel.exportedFile.collectAsState()

  var use1080p by remember { mutableStateOf(false) }
  var exportOnlyPreview by remember { mutableStateOf(false) }

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
          text = "MP4 VIDEO EXPORT STUDIO",
          fontSize = 16.sp,
          fontWeight = FontWeight.Black,
          color = NeonCyan,
          letterSpacing = 1.sp
        )
        Text(
          text = "Export final anime episodes in 720p/1080p MP4 format with subtitles",
          fontSize = 11.sp,
          color = TextSecondary
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
          text = "「MP4出力」",
          fontSize = 10.sp,
          color = NeonPink,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Settings Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = CyberCard),
      border = BorderStroke(1.dp, CyberSurfaceBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "EXPORT SPECIFICATIONS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextMuted,
          letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Resolution Switcher (720p vs 1080p)
        Text(
          text = "RESOLUTION (16:9 LANDSCAPE):",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (!use1080p) NeonPurple else CyberSurfaceVariant)
              .border(
                1.dp,
                if (!use1080p) NeonPink else CyberSurfaceBorder,
                RoundedCornerShape(10.dp)
              )
              .clickable { use1080p = false }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "720p HD",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (!use1080p) Color.White else TextPrimary
              )
              Text(
                text = "1280 x 720 (Fast)",
                fontSize = 10.sp,
                color = if (!use1080p) Color.White.copy(alpha = 0.8f) else TextSecondary
              )
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (use1080p) NeonPurple else CyberSurfaceVariant)
              .border(
                1.dp,
                if (use1080p) NeonPink else CyberSurfaceBorder,
                RoundedCornerShape(10.dp)
              )
              .clickable { use1080p = true }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "1080p Full HD",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (use1080p) Color.White else TextPrimary
              )
              Text(
                text = "1920 x 1080 (Cinema)",
                fontSize = 10.sp,
                color = if (use1080p) Color.White.copy(alpha = 0.8f) else TextSecondary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scene Count Scope Selector
        Text(
          text = "RENDER SCOPE:",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (!exportOnlyPreview) NeonCyan.copy(alpha = 0.2f) else CyberSurfaceVariant)
              .border(
                1.dp,
                if (!exportOnlyPreview) NeonCyan else CyberSurfaceBorder,
                RoundedCornerShape(10.dp)
              )
              .clickable { exportOnlyPreview = false }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Full Episode (${project?.scenes?.size ?: 0} Scenes)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (!exportOnlyPreview) NeonCyan else TextSecondary
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (exportOnlyPreview) NeonCyan.copy(alpha = 0.2f) else CyberSurfaceVariant)
              .border(
                1.dp,
                if (exportOnlyPreview) NeonCyan else CyberSurfaceBorder,
                RoundedCornerShape(10.dp)
              )
              .clickable { exportOnlyPreview = true }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Quick Test (First 5 Scenes)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (exportOnlyPreview) NeonCyan else TextSecondary
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Start Export Button
    Button(
      onClick = {
        val maxScenes = if (exportOnlyPreview) 5 else null
        viewModel.exportMp4Video(resolution1080p = use1080p, maxScenes = maxScenes)
      },
      enabled = !isExporting,
      colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .testTag("btn_start_export_mp4")
    ) {
      if (isExporting) {
        CircularProgressIndicator(
          color = Color.White,
          modifier = Modifier.size(20.dp),
          strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Rendering MP4 Video (${(progress * 100).toInt()}%)...",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      } else {
        Icon(Icons.Default.VideoFile, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Export Complete Anime Episode (MP4)",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Real Rendering Progress Card
    AnimatedVisibility(visible = isExporting) {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, NeonPink),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Hardware MediaCodec Encoding",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = NeonPink
            )
            Text(
              text = "${(progress * 100).toInt()}%",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = NeonGreen
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = NeonPink,
            trackColor = CyberSurfaceBorder
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = statusMessage,
            fontSize = 11.sp,
            color = TextPrimary
          )
        }
      }
    }

    // Export Complete Card
    if (exportedFile != null && !isExporting) {
      Spacer(modifier = Modifier.height(10.dp))
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.5.dp, NeonGreen),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("card_exported_video_result")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "MP4 VIDEO READY FOR DOWNLOAD",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NeonGreen
              )
              Text(
                text = exportedFile?.name ?: "",
                fontSize = 11.sp,
                color = TextPrimary
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Size: ${(exportedFile?.length() ?: 0) / 1024} KB • Format: MP4 (H.264 / AAC) • 16:9",
            fontSize = 11.sp,
            color = TextSecondary
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                val file = exportedFile ?: return@Button
                val uri = try {
                  FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                } catch (e: Exception) {
                  Uri.fromFile(file)
                }
                val intent = Intent(Intent.ACTION_VIEW).apply {
                  setDataAndType(uri, "video/mp4")
                  addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Play Anime Episode"))
              },
              colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("btn_play_exported_video")
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberBlack)
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Play Video", color = CyberBlack, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = {
                val file = exportedFile ?: return@Button
                val uri = try {
                  FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                } catch (e: Exception) {
                  Uri.fromFile(file)
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "video/mp4"
                  putExtra(Intent.EXTRA_STREAM, uri)
                  addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share MP4 Anime Video"))
              },
              colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
              border = BorderStroke(1.dp, NeonCyan),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("btn_share_exported_video")
            ) {
              Icon(Icons.Default.Share, contentDescription = null, tint = NeonCyan)
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Share MP4", color = NeonCyan, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
