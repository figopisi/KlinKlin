package com.example.klinklinapps.ui

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import com.example.klinklinapps.data.Order
import com.google.firebase.firestore.FirebaseFirestore

private val DrvBg = Color(0xFFF0F7FF)
private val DrvPrimary = Color(0xFF5B9BD5)
private val DrvSecondary = Color(0xFFE3F2FD)
private val DrvAccent = Color(0xFF42A5F5)
private val DrvTextMain = Color(0xFF1A2332)
private val DrvTextSub = Color(0xFF64748B)
private val DrvSuccess = Color(0xFF43A047)
private val DrvWarning = Color(0xFFFF8F00)

/** Label status yang enak dibaca untuk driver & customer. */
fun statusLabel(status: String): String = when (status) {
    "MENUNGGU_PICKUP" -> "Menunggu Dijemput"
    "DRIVER_MENJEMPUT" -> "Driver Menjemput"
    "DIANTAR_KE_LAUNDRY" -> "Diantar ke Laundry"
    "DI_LAUNDRY" -> "Sampai di Laundry"
    "DITIMBANG" -> "Sedang Ditimbang"
    "ON_HOLD" -> "Menunggu Pembayaran"
    "DIPROSES" -> "Sedang Diproses"
    "MENUNGGU_DIANTAR" -> "Menunggu Diantar"
    "SELESAI" -> "Selesai"
    else -> status.replace("_", " ")
}

/** Aksi berikutnya untuk driver: Pair(label tombol, status baru). Null jika tidak ada aksi. */
private fun nextDriverAction(status: String): Pair<String, String>? = when (status) {
    "DRIVER_MENJEMPUT" -> "Sudah Pickup — Antar ke Laundry" to "DIANTAR_KE_LAUNDRY"
    "DIANTAR_KE_LAUNDRY" -> "Sampai di Laundry" to "DI_LAUNDRY"
    else -> null
}

@Composable
fun DriverDashboardScreen(
    viewModel: DriverViewModel,
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
    val availableOrders by viewModel.availableOrders
    val myOrders by viewModel.myOrders
    val deliveryOrders by viewModel.deliveryOrders
    val finishedOrders by viewModel.finishedOrders
    val driverName by viewModel.driverName
    val isProcessing by viewModel.isProcessing
    val errorMessage by viewModel.errorMessage

    var selectedTab by remember { mutableStateOf(0) } // 0 = order masuk, 1 = order aktif, 2 = diantar, 3 = riwayat
    var chatOrder by remember { mutableStateOf<Order?>(null) }
    var lastBackPress by remember { mutableStateOf(0L) }
    var showProfile by remember { mutableStateOf(false) }
    var confirmRequest by remember { mutableStateOf<ConfirmRequest?>(null) }

    confirmRequest?.let { request ->
        ConfirmActionDialog(request = request, onDismiss = { confirmRequest = null })
    }

    // Pasang ulang listener saat dashboard dibuka (mis. setelah ganti akun)
    LaunchedEffect(Unit) { viewModel.refresh() }

    // Back tidak keluar app: tutup profil/chat -> kembali tab order masuk -> (dobel-back) keluar
    BackHandler(enabled = true) {
        when {
            showProfile -> showProfile = false
            chatOrder != null -> chatOrder = null
            selectedTab != 0 -> selectedTab = 0
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPress < 2000) {
                    (context as? Activity)?.finish()
                } else {
                    lastBackPress = now
                    Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // Layar profil menimpa dashboard
    if (showProfile) {
        ProfileManagementScreen(
            initialName = profileName,
            email = profileEmail,
            initialPhone = profilePhone,
            initialAddress = profileAddress,
            roleLabel = "Driver",
            isSaving = isSavingProfile,
            onSave = onSaveProfile,
            onDeleteAccount = onDeleteAccount,
            onLogout = onLogout,
            onBack = { showProfile = false }
        )
        return
    }

    // Layar chat menimpa dashboard ketika dibuka
    chatOrder?.let { order ->
        OrderChatScreen(
            chatId = order.id,
            peerName = order.customerName.ifEmpty { "Customer" },
            peerSubtitle = "Customer",
            peerIcon = Icons.Default.Person,
            onBack = { chatOrder = null }
        )
        return
    }

    Scaffold(containerColor = DrvBg) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Surface(color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).background(DrvSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = DrvPrimary)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Halo Driver,", fontSize = 12.sp, color = DrvTextSub)
                        Text(driverName, fontSize = 18.sp, fontWeight = FontWeight.Black, color = DrvTextMain)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DrvSuccess.copy(0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(DrvSuccess, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Aktif", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DrvSuccess)
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { showProfile = true }) {
                        Icon(Icons.Default.ManageAccounts, contentDescription = "Kelola Profil", tint = DrvPrimary)
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
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = DrvTextSub)
                    }
                }
            }

            // Tab selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DriverTab("Masuk", availableOrders.size, selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                DriverTab("Berjalan", myOrders.size, selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
                DriverTab("Diantar", deliveryOrders.size, selectedTab == 2, Modifier.weight(1f)) { selectedTab = 2 }
                DriverTab("Riwayat", finishedOrders.size, selectedTab == 3, Modifier.weight(1f)) { selectedTab = 3 }
            }

            when (selectedTab) {
                0 -> AvailableOrdersList(
                    orders = availableOrders,
                    isProcessing = isProcessing,
                    onAccept = { order ->
                        confirmRequest = ConfirmRequest(
                            title = "Terima Order Ini?",
                            message = "Order #${order.id.takeLast(5).uppercase()} akan menjadi tanggung jawabmu dan status berubah menjadi \"${statusLabel("DRIVER_MENJEMPUT")}\".",
                            confirmLabel = "Ya, Terima",
                            onConfirm = { viewModel.acceptOrder(order) }
                        )
                    }
                )
                1 -> MyOrdersList(
                    orders = myOrders,
                    isProcessing = isProcessing,
                    onAdvance = { order, status ->
                        confirmRequest = ConfirmRequest(
                            title = "Ubah Status Order?",
                            message = "Status Order #${order.id.takeLast(5).uppercase()} akan diubah menjadi \"${statusLabel(status)}\".",
                            confirmLabel = "Ya, Ubah",
                            onConfirm = { viewModel.advanceStatus(order, status) }
                        )
                    },
                    onChat = { chatOrder = it },
                    onRelease = { order ->
                        confirmRequest = ConfirmRequest(
                            title = "Lepas Pesanan?",
                            message = "Order #${order.id.takeLast(5).uppercase()} akan dikembalikan ke antrean dan bisa diambil driver lain.",
                            confirmLabel = "Ya, Lepas",
                            destructive = true,
                            onConfirm = { viewModel.releaseOrder(order) }
                        )
                    },
                    onFinishDelivery = { order ->
                        confirmRequest = ConfirmRequest(
                            title = "Selesaikan Pengantaran?",
                            message = "Order #${order.id.takeLast(5).uppercase()} akan ditandai SELESAI dan pembayaran diteruskan ke laundry.",
                            confirmLabel = "Ya, Selesaikan",
                            onConfirm = { viewModel.finishDelivery(order) }
                        )
                    }
                )
                2 -> DeliveryOrdersList(
                    orders = deliveryOrders,
                    isProcessing = isProcessing,
                    onChat = { chatOrder = it },
                    onFinishDelivery = { order ->
                        confirmRequest = ConfirmRequest(
                            title = "Selesaikan Pengantaran?",
                            message = "Order #${order.id.takeLast(5).uppercase()} akan ditandai SELESAI dan pembayaran diteruskan ke laundry.",
                            confirmLabel = "Ya, Selesaikan",
                            onConfirm = { viewModel.finishDelivery(order) }
                        )
                    }
                )
                else -> FinishedOrdersList(orders = finishedOrders)
            }
        }
    }
}

@Composable
private fun FinishedOrdersList(orders: List<Order>) {
    if (orders.isEmpty()) {
        EmptyState(
            icon = Icons.Default.History,
            title = "Belum ada riwayat pesanan",
            subtitle = "Pesanan yang sudah kamu selesaikan akan muncul di sini."
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(orders) { order ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OrderHeaderRow(order)
                    Spacer(modifier = Modifier.height(14.dp))
                    InfoRow(Icons.Default.Person, order.customerName.ifEmpty { "Customer" })
                    if (order.address.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(Icons.Default.LocationOn, order.address)
                    }
                    LaundryInfoSection(order.laundryUid)
                }
            }
        }
    }
}

@Composable
private fun DeliveryOrdersList(
    orders: List<Order>,
    isProcessing: Boolean,
    onChat: (Order) -> Unit,
    onFinishDelivery: (Order) -> Unit
) {
    if (orders.isEmpty()) {
        EmptyState(
            icon = Icons.Default.LocalShipping,
            title = "Belum ada yang perlu diantar",
            subtitle = "Cucian yang sudah selesai dari laundry akan muncul di sini."
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(orders) { order ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OrderHeaderRow(order)
                    Spacer(modifier = Modifier.height(14.dp))
                    InfoRow(Icons.Default.Person, order.customerName.ifEmpty { "Customer" })
                    if (order.address.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(Icons.Default.LocationOn, order.address)
                    }
                    LaundryInfoSection(order.laundryUid)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { onChat(order) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DrvPrimary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chat Customer", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onFinishDelivery(order) },
                        enabled = !isProcessing,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DrvSuccess)
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Selesaikan Pengantaran", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverTab(
    label: String,
    count: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (selected) DrvPrimary else Color.White
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (selected) Color.White else DrvTextMain
            )
            if (count > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier.size(20.dp)
                        .background(if (selected) Color.White else DrvPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        count.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (selected) DrvPrimary else Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun AvailableOrdersList(
    orders: List<Order>,
    isProcessing: Boolean,
    onAccept: (Order) -> Unit
) {
    if (orders.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Inbox,
            title = "Belum ada order masuk",
            subtitle = "Order baru akan muncul di sini secara otomatis."
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(orders) { order ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OrderHeaderRow(order)
                    Spacer(modifier = Modifier.height(14.dp))
                    InfoRow(Icons.Default.Person, order.customerName.ifEmpty { "Customer" })
                    if (order.customerPhone.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(Icons.Default.Phone, order.customerPhone)
                    }
                    if (order.address.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(Icons.Default.LocationOn, order.address)
                    }
                    LaundryInfoSection(order.laundryUid)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onAccept(order) },
                        enabled = !isProcessing,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DrvPrimary)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Terima Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MyOrdersList(
    orders: List<Order>,
    isProcessing: Boolean,
    onAdvance: (Order, String) -> Unit,
    onChat: (Order) -> Unit,
    onRelease: (Order) -> Unit,
    onFinishDelivery: (Order) -> Unit
) {
    if (orders.isEmpty()) {
        EmptyState(
            icon = Icons.Default.LocalShipping,
            title = "Belum ada order aktif",
            subtitle = "Order yang kamu terima akan muncul di sini."
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(orders) { order ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OrderHeaderRow(order)
                    Spacer(modifier = Modifier.height(14.dp))
                    InfoRow(Icons.Default.Person, order.customerName.ifEmpty { "Customer" })
                    if (order.address.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(Icons.Default.LocationOn, order.address)
                    }
                    LaundryInfoSection(order.laundryUid)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tombol chat — hanya saat driver aktif menangani order (status != SELESAI, sudah pasti di list ini)
                    OutlinedButton(
                        onClick = { onChat(order) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DrvPrimary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chat Customer", fontWeight = FontWeight.Bold)
                    }

                    val action = nextDriverAction(order.status)
                    when {
                        // Laundry sudah selesai proses -> driver antar balik & selesaikan
                        order.status == "MENUNGGU_DIANTAR" -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onFinishDelivery(order) },
                                enabled = !isProcessing,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DrvSuccess)
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Selesaikan Pengantaran", fontWeight = FontWeight.Bold)
                            }
                        }
                        action != null -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onAdvance(order, action.second) },
                                enabled = !isProcessing,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DrvAccent)
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(action.first, fontWeight = FontWeight.Bold)
                            }
                            // Lepas order -> kembali ke antrean (MENUNGGU_PICKUP)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { onRelease(order) },
                                enabled = !isProcessing,
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Lepas Pesanan", fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = DrvSecondary
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = DrvPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Laundry sedang memproses pesanan ini.",
                                        fontSize = 12.sp,
                                        color = DrvPrimary,
                                        fontWeight = FontWeight.Medium
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

@Composable
private fun OrderHeaderRow(order: Order) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(46.dp).background(DrvSecondary, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.LocalLaundryService, contentDescription = null, tint = DrvPrimary)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Order #${order.id.takeLast(5).uppercase()}", fontWeight = FontWeight.Black, color = DrvTextMain)
            Text(order.service.ifEmpty { "Layanan Laundry" }, fontSize = 11.sp, color = DrvTextSub)
        }
        StatusChip(order.status)
    }
}

@Composable
private fun LaundryInfoSection(laundryUid: String) {
    var name by remember { mutableStateOf("Memuat...") }
    var address by remember { mutableStateOf("Memuat alamat...") }
    LaunchedEffect(laundryUid) {
        if (laundryUid.isNotEmpty()) {
            FirebaseFirestore.getInstance().collection("users").document(laundryUid).get()
                .addOnSuccessListener { doc ->
                    name = doc.getString("name") ?: "Laundry"
                    address = doc.getString("address") ?: "Alamat tidak ditemukan"
                }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    InfoRow(Icons.Default.Store, "$name: $address")
}

@Composable
private fun StatusChip(status: String) {
    val (bg, fg) = when (status) {
        "MENUNGGU_PICKUP" -> DrvWarning.copy(0.12f) to DrvWarning
        "SELESAI" -> DrvSuccess.copy(0.12f) to DrvSuccess
        else -> DrvSecondary to DrvPrimary
    }
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Text(
            statusLabel(status),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = DrvTextSub, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, color = DrvTextMain)
    }
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(56.dp), tint = DrvTextSub.copy(0.3f))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = DrvTextSub, fontWeight = FontWeight.Bold)
            Text(subtitle, color = DrvTextSub.copy(0.6f), fontSize = 13.sp)
        }
    }
}
