package com.example.klinklinapps.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.klinklinapps.data.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    // The ChatScreen reads this as:  val messages by viewModel.messages
    val messages = mutableStateOf<List<ChatMessage>>(emptyList())

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {
        listenToMessages()
    }

    fun listenToMessages() {
        db.collection("chats")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    messages.value = it.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                    }
                }
            }
    }

    fun sendMessage(text: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            db.collection("chats").add(
                ChatMessage(
                    senderId  = uid,
                    message   = text.trim(),
                    type      = "TEXT",
                    timestamp = Timestamp.now()
                )
            )
        }
    }
}