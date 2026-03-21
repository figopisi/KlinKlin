package com.example.klinklinapps.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Jakarta Sans Simulated using Default with Weight adjustments for more modern look
val JakartaSans = FontFamily.Default

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
)
