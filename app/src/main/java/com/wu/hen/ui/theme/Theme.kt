package com.wu.hen.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 静态品牌配色（Android 12 以下回退使用）
private val LightColors =
    lightColorScheme(
        primary = Color(0xFF00696E),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF6FF6FD),
        onPrimaryContainer = Color(0xFF002022),
        secondary = Color(0xFF4A6365),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFCCE8EA),
        onSecondaryContainer = Color(0xFF051F21),
        tertiary = Color(0xFF4B607C),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD3E4FF),
        onTertiaryContainer = Color(0xFF041C35),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFF4FBFC),
        onBackground = Color(0xFF161D1E),
        surface = Color(0xFFF4FBFC),
        onSurface = Color(0xFF161D1E),
        surfaceVariant = Color(0xFFDAE4E5),
        onSurfaceVariant = Color(0xFF3F494A),
        outline = Color(0xFF6F797A),
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFF4DDAFC),
        onPrimary = Color(0xFF003739),
        primaryContainer = Color(0xFF004F52),
        onPrimaryContainer = Color(0xFF6FF6FD),
        secondary = Color(0xFFB0CBCD),
        onSecondary = Color(0xFF1C3437),
        secondaryContainer = Color(0xFF334B4D),
        onSecondaryContainer = Color(0xFFCCE8EA),
        tertiary = Color(0xFFB1C8E8),
        onTertiary = Color(0xFF1A314B),
        tertiaryContainer = Color(0xFF334863),
        onTertiaryContainer = Color(0xFFD3E4FF),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF0E1516),
        onBackground = Color(0xFFDDE4E5),
        surface = Color(0xFF0E1516),
        onSurface = Color(0xFFDDE4E5),
        surfaceVariant = Color(0xFF3F494A),
        onSurfaceVariant = Color(0xFFBEC8C9),
        outline = Color(0xFF899393),
    )

/**
 * 无痕主题：Android 12+ 优先 Material You 动态取色
 */
@Composable
fun WuHenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context)
                else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColors
            else -> LightColors
        }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
