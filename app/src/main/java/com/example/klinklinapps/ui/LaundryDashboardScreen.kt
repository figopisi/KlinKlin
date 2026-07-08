package com.example.klinklinapps.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.data.Order

private val LdyBg = Color(0xFFF0F7FF)
private val LdyPrimary = Color(0xFF5B9BD5)
private val LdySecondary = Color(0xFFE3F2FD)
private val LdyAccent = Color(0xFF42A5F5)
private val LdyTextMain = Color(0xFF1A2332)
private val LdyTextSub = Color(0xFF64748B)
private val LdySuccess = Color(0xFF43A047)
private val LdyWarning = Color(0xFFFF8F00)

@Composable
fun LaundryDashboardScreen(
    viewModel: LaundryViewModel,
    profileName: String,
    profileEmail: String,
    profilePhone: String,
    profileAddress: String,
    isSavingProfile: Boolean,
    onSaveProfile: (String, String, String, String, String) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit = {}
) {
    val context = LocalContext.current
    val incomingOrders by viewModel.incomingOrders
    val processingOrders by viewModel.processingOrders
    val completedOrders by viewModel.completedOrders
    val laundryName by viewModel.laundryName
    val isProcessing by viewModel.isProcessing
    val errorMessage by viewModel.errorMessage

    var selectedTab by remember { mutableStateOf(0) }
    var weighOrder by remember { mutableStateOf<Order?>(null) }
    var showProfile by remember { mutableStateOf(false) }
    var chatOrder by remember { mutableStateOf<Order?>(null) }
    var confirmRequest by remember { mutableStateOf<ConfirmRequest?>(null) }

    confirmRequest?.let { request ->
        ConfirmActionDialog(request = request, onDismiss = { confirmRequest = null })
    }

    // Pasang ulang listener saat dashboard dibuka (mis. setelah ganti akun)
    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    chatOrder?.let { order ->
        OrderChatScreen(
            chatId = "${order.customerUid}_${order.laundryUid}",
            peerName = order.customerName.ifEmpty { "Customer" },
            peerSubtitle = "Customer",
            peerIcon = Icons.Default.Person,
            chatCollection = "laundry_chats",
            onBack = { chatOrder = null }
        )
        return
    }

    if (showProfile) {
        ProfileManagementScreen(
            initialName = profileName,
            email = profileEmail,
            initialPhone = profilePhone,
            initialAddress = profileAddress,
            roleLabel = "Laundry Partner",
            isSaving = isSavingProfile,
            onSave = onSaveProfile,
            onDeleteAccount = onDeleteAccount,
            onLogout = onLogout,
            onBack = { showProfile = false }
        )
        return
    }

    weighOrder?.let { order ->
        WeighDialog(
            order = order,
            onDismiss = { weighOrder = null },
            onConfirm = { weight, subtotal ->
                viewModel.weighAndProcess(order, weight, subtotal)
                weighOrder = null
            }
        )
    }

    Scaffold(containerColor = LdyBg) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Surface(color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).background(LdySecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalLaundryService, contentDescription = null, tint = LdyPrimary)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mitra Laundry", fontSize = 12.sp, color = LdyTextSub)
                        Text(laundryName, fontSize = 18.sp, fontWeight = FontWeight.Black, color = LdyTextMain)
                    }
                    IconButton(onClick = { showProfile = true }) {
                        Icon(Icons.Default.ManageAccounts, contentDescription = "Kelola Profil", tint = LdyPrimary)
                    }
                    IconButton(onClick = {
                        confirmRequest = ConfirmRequest(
                            title = "Keluar dari Akun?",
                            message = "Kamu harus login kembali untuk menggunakan aplikasi. Lanjutkan?",
                            confirmLabel = "Ya, Keluar",
                            destructive = true,
                            onConfirm = onLogout
                        )
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = LdyTextSub)
                    }
                }
            }

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LaundryTab("Masuk", incomingOrders.size, selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                LaundryTab("Diproses", processingOrders.size, selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
                LaundryTab("Selesai", completedOrders.size, selectedTab == 2, Modifier.weight(1f)) { selectedTab = 2 }
            }

            when (selectedTab) {
                0 -> if (incomingOrders.isEmpty()) {
                    LaundryEmptyState(Icons.Default.Inbox, "Belum ada pesanan masuk", "Cucian dari driver akan muncul di sini untuk ditimbang.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(incomingOrders) { order ->
                            LaundryOrderCard(order, onChat = { chatOrder = order }) {
                                if (order.status == "ON_HOLD") {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        color = LdyWarning.copy(0.12f)
                                    ) {
                                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = LdyWarning, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                "Menunggu pembayaran customer Rp ${order.priceAdjustment}. Berat & harga sudah tersimpan (draft).",
                                                fontSize = 12.sp, color = LdyWarning, fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            confirmRequest = ConfirmRequest(
                                                title = "Cek Pembayaran & Proses?",
                                                message = "Sistem akan memeriksa saldo customer untuk Order #${order.id.takeLast(5).uppercase()}. Jika sudah cukup, status berubah menjadi \"${statusLabel("DIPROSES")}\".",
                                                confirmLabel = "Ya, Proses",
                                                onConfirm = { viewModel.confirmHold(order) }
                                            )
                                        },
                                        enabled = !isProcessing,
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = LdyPrimary)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Cek Pembayaran & Proses", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { weighOrder = order },
                                        enabled = !isProcessing,
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = LdyAccent)
                                    ) {
                                        Icon(Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Timbang & Proses", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> if (processingOrders.isEmpty()) {
                    LaundryEmptyState(Icons.Default.LocalLaundryService, "Belum ada cucian diproses", "Pesanan yang sudah dibayar & ditimbang muncul di sini.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(processingOrders) { order ->
                            LaundryOrderCard(order, onChat = { chatOrder = order }) {
                                Button(
                                    onClick = {
                                        confirmRequest = ConfirmRequest(
                                            title = "Selesaikan Proses Cucian?",
                                            message = "Status Order #${order.id.takeLast(5).uppercase()} akan diubah menjadi \"${statusLabel("MENUNGGU_DIANTAR")}\" dan driver dapat mengantarnya ke customer.",
                                            confirmLabel = "Ya, Selesai",
                                            onConfirm = { viewModel.finishOrder(order) }
                                        )
                                    },
                                    enabled = !isProcessing,
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LdySuccess)
                                ) {
                                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Selesai — Siap Diantar", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                else -> if (completedOrders.isEmpty()) {
                    LaundryEmptyState(Icons.Default.DoneAll, "Belum ada pesanan selesai", "Riwayat pesanan yang sudah kamu proses muncul di sini.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(completedOrders) { order ->
                            LaundryOrderCard(order, onChat = { chatOrder = order }) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = LdySuccess.copy(0.12f)
                                ) {
                                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = LdySuccess, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            if (order.status == "SELESAI") "Selesai • dibayar Rp ${order.totalPrice}"
                                            else "Selesai diproses — menunggu driver mengantar",
                                            fontSize = 12.sp, color = LdySuccess, fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LaundryTab(label: String, count: Int, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (selected) LdyPrimary else Color.White
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (selected) Color.White else LdyTextMain)
            if (count > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier.size(20.dp).background(if (selected) Color.White else LdyPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(count.toString(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = if (selected) LdyPrimary else Color.White)
                }
            }
        }
    }
}

@Composable
private fun LaundryOrderCard(order: Order, onChat: (() -> Unit)? = null, action: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(46.dp).background(LdySecondary, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocalLaundryService, contentDescription = null, tint = LdyPrimary)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Order #${order.id.takeLast(5).uppercase()}", fontWeight = FontWeight.Black, color = LdyTextMain)
                    Text(order.service.ifEmpty { "Layanan Laundry" }, fontSize = 11.sp, color = LdyTextSub)
                }
                LaundryStatusChip(order.status)
            }
            Spacer(modifier = Modifier.height(14.dp))
            LaundryInfoRow(Icons.Default.Person, order.customerName.ifEmpty { "Customer" })
            if (order.weight > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LaundryInfoRow(Icons.Default.Scale, "${order.weight} kg  •  Subtotal Rp ${order.laundrySubtotal}")
            }
            if (order.driverName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LaundryInfoRow(Icons.Default.TwoWheeler, "Diantar oleh ${order.driverName}")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (onChat != null) {
                OutlinedButton(
                    onClick = onChat,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LdyPrimary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat Customer", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            action()
        }
    }
}

@Composable
private fun LaundryStatusChip(status: String) {
    val (bg, fg) = when (status) {
        "DI_LAUNDRY" -> LdyWarning.copy(0.12f) to LdyWarning
        "ON_HOLD" -> Color(0xFFEF5350).copy(0.12f) to Color(0xFFD32F2F)
        "DIPROSES" -> LdySecondary to LdyPrimary
        "SELESAI", "MENUNGGU_DIANTAR" -> LdySuccess.copy(0.12f) to LdySuccess
        else -> LdySecondary to LdyPrimary
    }
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Text(
            statusLabel(status),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = fg, fontWeight = FontWeight.Bold, fontSize = 10.sp
        )
    }
}

@Composable
private fun LaundryInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = LdyTextSub, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, color = LdyTextMain)
    }
}

@Composable
private fun LaundryEmptyState(icon: ImageVector, title: String, subtitle: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(56.dp), tint = LdyTextSub.copy(0.3f))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = LdyTextSub, fontWeight = FontWeight.Bold)
            Text(subtitle, color = LdyTextSub.copy(0.6f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun WeighDialog(order: Order, onDismiss: () -> Unit, onConfirm: (Double, Long) -> Unit) {
    var weightText by remember { mutableStateOf("") }
    var subtotalText by remember { mutableStateOf("") }
    val weight = weightText.toDoubleOrNull()
    val subtotal = subtotalText.toLongOrNull()
    val valid = weight != null && weight > 0 && subtotal != null && subtotal > 0

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = LdyPrimary,
        focusedLabelColor = LdyPrimary,
        cursorColor = LdyPrimary,
        focusedTextColor = LdyTextMain,
        unfocusedTextColor = LdyTextMain
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Timbang Order #${order.id.takeLast(5).uppercase()}", fontWeight = FontWeight.Black, color = LdyTextMain) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Masukkan berat asli & harga laundry murni. Selisih dari estimasi otomatis diselesaikan via saldo customer.", fontSize = 12.sp, color = LdyTextSub)
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Berat (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = subtotalText,
                    onValueChange = { subtotalText = it },
                    label = { Text("Subtotal Laundry (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = fieldColors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (valid) onConfirm(weight!!, subtotal!!) },
                enabled = valid,
                colors = ButtonDefaults.buttonColors(containerColor = LdyPrimary)
            ) { Text("Proses") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = LdyTextSub) } },
        containerColor = Color.White
    )
}
