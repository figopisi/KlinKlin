package com.example.klinklinapps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TwoWheeler
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.data.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

private val ChatBg = Color(0xFFF0F7FF)
private val ChatPrimary = Color(0xFF5B9BD5)
private val ChatSecondary = Color(0xFFE3F2FD)
private val ChatTextMain = Color(0xFF1A2332)
private val ChatTextSub = Color(0xFF64748B)

/**
 * Chat per-order yang menghubungkan customer dengan driver.
 * Firestore: order_chats/{orderId}/messages/{messageId}
 * Dipakai oleh kedua sisi (driver & customer) dengan orderId yang sama.
 */
@Composable
fun OrderChatScreen(
    chatId: String,
    peerName: String,
    peerSubtitle: String = "Driver KlinKlin",
    peerIcon: ImageVector = Icons.Default.TwoWheeler,
    chatCollection: String = "order_chats",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUserId = Firebase.auth.currentUser?.uid ?: ""
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    DisposableEffect(chatCollection, chatId) {
        val reg = db.collection(chatCollection).document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Toast.makeText(context, "Gagal memuat chat: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if (snap != null) {
                    messages.clear()
                    messages.addAll(
                        snap.documents.mapNotNull { d ->
                            d.toObject(ChatMessage::class.java)?.copy(id = d.id)
                        }
                    )
                }
            }
        onDispose { reg.remove() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty() || currentUserId.isEmpty()) return
        val chatDoc = db.collection(chatCollection).document(chatId)
        // Simpan metadata induk agar chat mudah ditemukan & aman.
        chatDoc.set(
            mapOf("chatId" to chatId, "updatedAt" to FieldValue.serverTimestamp()),
            SetOptions.merge()
        )
        chatDoc.collection("messages")
            .add(
                ChatMessage(
                    senderId = currentUserId,
                    message = text,
                    type = "TEXT",
                    isRead = false,
                    timestamp = Timestamp.now()
                )
            )
            .addOnFailureListener { e ->
                Toast.makeText(context, "Pesan gagal terkirim: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        inputText = ""
    }

    Column(modifier = Modifier.fillMaxSize().background(ChatBg)) {
        // Header
        Surface(color = Color.White, shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = ChatPrimary)
                }
                Box(
                    modifier = Modifier.size(42.dp).background(ChatSecondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(peerIcon, contentDescription = null, tint = ChatPrimary, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(peerName, fontWeight = FontWeight.Black, fontSize = 16.sp, color = ChatTextMain)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFF43A047), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(peerSubtitle, fontSize = 11.sp, color = ChatTextSub)
                    }
                }
            }
        }

        // Messages
        if (messages.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "Belum ada pesan.\nMulai percakapan dengan ${peerName}.",
                    color = ChatTextSub, fontSize = 13.sp,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    OrderChatBubble(msg = msg, isMe = msg.senderId == currentUserId)
                }
            }
        }

        // Input
        Surface(color = Color.White, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Tulis pesan...", color = ChatTextSub.copy(0.6f)) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChatPrimary,
                        unfocusedBorderColor = ChatSecondary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = ChatTextMain,
                        unfocusedTextColor = ChatTextMain,
                        cursorColor = ChatPrimary
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(48.dp)
                        .background(if (inputText.isNotBlank()) ChatPrimary else ChatSecondary, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { sendMessage() }, enabled = inputText.isNotBlank()) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Kirim",
                            tint = if (inputText.isNotBlank()) Color.White else ChatTextSub
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderChatBubble(msg: ChatMessage, isMe: Boolean) {
    val time = msg.timestamp?.toDate()?.let {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
    } ?: ""

    // Pesan otomatis (SYSTEM) tampil sebagai kartu info yang menonjol
    if (msg.type == "SYSTEM") {
        Column(modifier = Modifier.fillMaxWidth()) {
            Surface(
                color = ChatSecondary,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp)) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = ChatPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            msg.senderName.ifEmpty { "KlinKlin" },
                            fontSize = 11.sp, fontWeight = FontWeight.Black, color = ChatPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(msg.message, fontSize = 13.sp, color = ChatTextMain, lineHeight = 18.sp)
                        Text(time, fontSize = 10.sp, color = ChatTextSub, modifier = Modifier.align(Alignment.End))
                    }
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe && msg.senderName.isNotEmpty()) {
            Text(
                msg.senderName,
                fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ChatTextSub,
                modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
            )
        }
        Surface(
            color = if (isMe) ChatPrimary else Color.White,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isMe) 18.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 18.dp
            ),
            tonalElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) {
                Text(
                    text = msg.message,
                    color = if (isMe) Color.White else ChatTextMain,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
                Text(
                    text = time,
                    color = if (isMe) Color.White.copy(0.7f) else ChatTextSub,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
