package com.example.klinklinapps.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Soft & Modern Brand Colors (Optimized)
val BrandBlue = Color(0xFF4A90E2) // Soft vibrant blue
val BrandBlueDark = Color(0xFF357ABD)
val BrandBlueLight = Color(0xFFF0F7FF) // Soft background blue tint

val SunYellow = Color(0xFFFFD600)
val SunYellowVariant = Color(0xFFFFC107)

// Neutral Palette
val White = Color(0xFFFFFFFF)
val Gray100 = Color(0xFFF8FAFC)
val Gray200 = Color(0xFFF1F5F9)
val Gray500 = Color(0xFF64748B)
val Gray800 = Color(0xFF1E293B)
val NavyBlue = Color(0xFF0F172A)

// Gradients
val PrimaryGradient = Brush.linearGradient(listOf(BrandBlue, Color(0xFF7CB9FF)))
val DarkGradient = Brush.linearGradient(listOf(NavyBlue, Color(0xFF1E293B)))
val SoftBlueGradient = Brush.verticalGradient(listOf(Color(0xFFF0F7FF), Color(0xFFFFFFFF)))
