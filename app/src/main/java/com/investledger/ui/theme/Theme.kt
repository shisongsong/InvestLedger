package com.investledger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * ChatGPT 简洁风格配色
 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF10A37F),  // ChatGPT 绿色
    secondary = Color(0xFF19C37D),
    tertiary = Color(0xFF0D8F6F),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF7F7F8),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF202123),
    onSurface = Color(0xFF202123)
)

@Composable
fun InvestLedgerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}