package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

enum class FontSizeScale(val multiplier: Float, val label: String) {
    NORMAL(1.0f, "Standard"),
    LARGE(1.25f, "Large"),
    EXTRA_LARGE(1.5f, "Extra Large");

    fun scale(sp: TextUnit): TextUnit {
        return (sp.value * multiplier).sp
    }
}

val LocalFontSizeScale = compositionLocalOf { FontSizeScale.NORMAL }
