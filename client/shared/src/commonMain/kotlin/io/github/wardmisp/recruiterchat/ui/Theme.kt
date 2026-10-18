package io.github.wardmisp.recruiterchat.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Bleu des titres du CV.
private val LightColors = lightColorScheme(
    primary = Color(0xFF2E5A88),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E3F7),
    onPrimaryContainer = Color(0xFF0F2A47),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CC3F0),
    onPrimary = Color(0xFF0F2A47),
    primaryContainer = Color(0xFF244A72),
    onPrimaryContainer = Color(0xFFD3E3F7),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
