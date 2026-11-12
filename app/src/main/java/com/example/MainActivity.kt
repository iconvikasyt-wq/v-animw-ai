package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.animeai.ui.components.AnimeStudioHeader
import com.example.animeai.ui.components.AnimeTabNavigation
import com.example.animeai.ui.screens.CharactersScreen
import com.example.animeai.ui.screens.ExportScreen
import com.example.animeai.ui.screens.ProjectsScreen
import com.example.animeai.ui.screens.StudioScreen
import com.example.animeai.ui.screens.TimelineScreen
import com.example.animeai.ui.screens.VisualsScreen
import com.example.animeai.ui.screens.VoicesScreen
import com.example.animeai.ui.theme.AnimeAITheme
import com.example.animeai.ui.theme.CyberBlack
import com.example.animeai.ui.theme.CyberSurface
import com.example.animeai.ui.theme.CyberSurfaceBorder
import com.example.animeai.ui.theme.NeonCyan
import com.example.animeai.ui.theme.NeonPurple
import com.example.animeai.ui.theme.TextPrimary
import com.example.animeai.viewmodel.AnimeStudioViewModel
import com.example.animeai.viewmodel.StudioTab
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AnimeAITheme {
        AnimeStudioApp()
      }
    }
  }
}

@Composable
fun AnimeStudioApp(
  viewModel: AnimeStudioViewModel = viewModel()
) {
  val selectedTab by viewModel.selectedTab.collectAsState()
  val currentProject by viewModel.currentProject.collectAsState()
  val statusAlert by viewModel.statusAlert.collectAsState()

  // Handle system back navigation
  BackHandler(enabled = selectedTab != StudioTab.STUDIO) {
    viewModel.setTab(StudioTab.STUDIO)
  }

  // Auto-dismiss alert after 3 seconds
  LaunchedEffect(statusAlert) {
    if (statusAlert != null) {
      delay(3200)
      viewModel.clearStatusAlert()
    }
  }

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    containerColor = CyberBlack,
    topBar = {
      Column {
        AnimeStudioHeader(
          title = currentProject?.title ?: "VIKAS AI ANIME STUDIO",
          totalScenes = currentProject?.scenes?.size ?: 42,
          durationMinutes = currentProject?.targetDurationMinutes ?: 15
        )
        AnimeTabNavigation(
          selectedTab = selectedTab,
          onTabSelected = { viewModel.setTab(it) }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTab) {
        StudioTab.STUDIO -> StudioScreen(viewModel, currentProject)
        StudioTab.CHARACTERS -> CharactersScreen(viewModel, currentProject)
        StudioTab.VISUALS -> VisualsScreen(viewModel, currentProject)
        StudioTab.VOICES -> VoicesScreen(viewModel, currentProject)
        StudioTab.TIMELINE -> TimelineScreen(viewModel, currentProject)
        StudioTab.EXPORT -> ExportScreen(viewModel, currentProject)
        StudioTab.PROJECTS -> ProjectsScreen(viewModel, currentProject)
      }

      // Floating Cyber Status Toast/Banner
      AnimatedVisibility(
        visible = statusAlert != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 12.dp, start = 16.dp, end = 16.dp)
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurface.copy(alpha = 0.95f))
            .border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
          Text(
            text = statusAlert ?: "",
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
