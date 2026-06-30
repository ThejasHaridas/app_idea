package com.thejas.upitracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary         = Color(0xFF818CF8),
    secondary       = Color(0xFFA78BFA),
    background      = Color(0xFF030712),
    surface         = Color(0xFF111827),
    surfaceVariant  = Color(0xFF1F2937),
    onBackground    = Color(0xFFF9FAFB),
    onSurface       = Color(0xFFF9FAFB),
    outline         = Color(0xFF374151)
)

@Composable
fun UPITrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
