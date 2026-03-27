package com.example.klinklinapps.data

import com.google.firebase.Timestamp

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Timestamp? = null,
    val isRead: Boolean = false,
    val type: String = "TEXT" // TEXT, SYSTEM_REMINDER
)
