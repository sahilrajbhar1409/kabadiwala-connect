package com.kabadiwalaconnect.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = GreenDark,
    background = Cream,
    surface = Color.White,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun KabadiwalaConnectTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography(
            headlineLarge = LocalTextStyle.current.copy(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            ),
            headlineMedium = LocalTextStyle.current.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            titleLarge = LocalTextStyle.current.copy(
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            ),
            bodyLarge = LocalTextStyle.current.copy(
                fontSize = 16.sp
            )
        ),
        content = content
    )
}