package com.example.klinklinapps.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.klinklinapps.data.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import java.util.Date

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    private val _messages = mutableStateOf<List<ChatMessage>>(emptyList())
    val messages: State<List<ChatMessage>> = _messages

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        listenToMessages()
    }

    fun listenToMessages() {
        val user = auth.currentUser ?: return
        
        _isLoading.value = true
        db.collection("chats")
            .document(user.uid)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    val messageList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                    }
                    _messages.value = messageList
                }
            }
    }

    fun sendMessage(text: String) {
        val user = auth.currentUser ?: return
        if (text.isBlank()) return

        val newMessage = hashMapOf(
            "senderId" to user.uid,
            "receiverId" to "ADMIN_KLINKLIN",
            "message" to text,
            "timestamp" to Timestamp(Date()),
            "isRead" to false,
            "type" to "TEXT"
        )

        db.collection("chats")
            .document(user.uid)
            .collection("messages")
            .add(newMessage)
    }
}
