package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  lightColorScheme(
    primary = GameTeal,
    secondary = GameBlueAccent,
    tertiary = GameGold,
    background = GameNavyDark,
    surface = GameNavyCard,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = TextLight,
    onSurface = TextLight
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GameTeal,
    secondary = GameBlueAccent,
    tertiary = GameGold,
    background = GameNavyDark,
    surface = GameNavyCard,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = TextLight,
    onSurface = TextLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Clean Minimalism is a light-themed design by default
  dynamicColor: Boolean = false, // Disable dynamic colors to keep our custom theme branding
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
