package com.example.meowplan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MeowPlanColorScheme = lightColorScheme(
    primary = PinkPrimary,
    secondary = PinkLight,
    background = PageCream,
    surface = CardWhite,
    onPrimary = TextDark,
    onSecondary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark,
    outline = BorderPink
)

@Composable
fun MeowPlanTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MeowPlanColorScheme,
        typography = Typography,
        content = content
    )
}
