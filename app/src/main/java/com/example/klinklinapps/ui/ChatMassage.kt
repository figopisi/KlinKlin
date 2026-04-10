package com.example.klinklinapps.ui

import com.google.firebase.Timestamp

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",       // ← was likely missing
    val message: String = "",
    val type: String = "TEXT",       // "TEXT" or "SYSTEM_REMINDER"
    val timestamp: Timestamp? = null
)