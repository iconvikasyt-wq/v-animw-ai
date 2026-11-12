package com.example.animeai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AnimeColorScheme = darkColorScheme(
  primary = NeonPurple,
  onPrimary = CyberBlack,
  primaryContainer = CyberSurfaceVariant,
  onPrimaryContainer = NeonPurpleBright,
  secondary = NeonCyan,
  onSecondary = CyberBlack,
  secondaryContainer = CyberSurfaceVariant,
  onSecondaryContainer = NeonCyanBright,
  tertiary = NeonPink,
  onTertiary = CyberBlack,
  background = CyberBlack,
  onBackground = TextPrimary,
  surface = CyberSurface,
  onSurface = TextPrimary,
  surfaceVariant = CyberSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = CyberSurfaceBorder,
  outlineVariant = NeonPurple.copy(alpha = 0.3f),
  inverseSurface = TextPrimary,
  inverseOnSurface = CyberBlack
)

@Composable
fun AnimeAITheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = AnimeColorScheme,
    typography = Typography(),
    content = content
  )
}
