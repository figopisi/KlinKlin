package com.example.klinklinapps.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.R
import com.example.klinklinapps.data.Order

private val LdyBg = Color(0xFFF0F7FF)
private val LdyPrimary = Color(0xFF5B9BD5)
private val LdySecondary = Color(0xFFE3F2FD)
private val LdyAccent = Color(0xFF42A5F5)
private val LdyTextMain = Color(0xFF1A2332)
private val LdyTextSub = Color(0xFF64748B)
private val LdySuccess = Color(0xFF43A047)
private val LdyWarning = Color(0xFFFF8F00)

data class BankMethod(val id: String, val name: String, val imageRes: Int)

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
    val balance by viewModel.balance
    val totalRevenue by viewModel.totalRevenue
    val totalWithdrawal by viewModel.totalWithdrawal
    
    val bankName by viewModel.bankName
    val bankAccountNumber by viewModel.bankAccountNumber
    val bankAccountName by viewModel.bankAccountName
    val bankChangeStatus by viewModel.bankChangeRequestStatus
    
    val isProcessing by viewModel.isProcessing
    val errorMessage by viewModel.errorMessage

    var selectedTab by remember { mutableStateOf(0) }
    var weighOrder by remember { mutableStateOf<Order?>(null) }
    var showProfile by remember { mutableStateOf(false) }
    var showFinanceDetail by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showBankEditDialog by remember { mutableStateOf(false) }
    var chatOrder by remember { mutableStateOf<Order?>(null) }
    var confirmRequest by remember { mutableStateOf<ConfirmRequest?>(null) }
    var lastBackPress by remember { mutableStateOf(0L) }

    // BackHandler untuk navigasi mulus
    BackHandler(enabled = true) {
        when {
            showProfile -> showProfile = false
            showFinanceDetail -> showFinanceDetail = false
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

    confirmRequest?.let { request ->
        ConfirmActionDialog(request = request, onDismiss = { confirmRequest = null })
    }

    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // Modal Withdrawal
    if (showWithdrawDialog) {
        WithdrawDialog(
            currentBalance = balance,
            bankName = bankName,
            accountNumber = bankAccountNumber,
            accountName = bankAccountName,
            onDismiss = { showWithdrawDialog = false },
            onConfirm = { amount ->
                viewModel.withdrawBalance(amount) {
                    showWithdrawDialog = false
                    Toast.makeText(context, "Penarikan Rp $amount berhasil diproses", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Modal Edit Bank
    if (showBankEditDialog) {
        BankEditDialog(
            onDismiss = { showBankEditDialog = false },
            onConfirm = { method, accName, accNum ->
                viewModel.requestBankChange(method.name, accName, accNum)
                showBankEditDialog = false
                Toast.makeText(context, "Permintaan perubahan rekening dikirim ke admin", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Overlay Layar Keuangan
    if (showFinanceDetail) {
        FinanceDetailScreen(
            balance = balance,
            totalRevenue = totalRevenue,
            totalWithdrawal = totalWithdrawal,
            bankName = bankName,
            bankAccountNumber = bankAccountNumber,
            bankAccountName = bankAccountName,
            bankChangeStatus = bankChangeStatus,
            onBack = { showFinanceDetail = false },
            onWithdrawClick = { 
                if (bankAccountNumber.isEmpty()) {
                    Toast.makeText(context, "Atur rekening tujuan dulu", Toast.LENGTH_SHORT).show()
                } else {
                    showWithdrawDialog = true 
                }
            },
            onEditBankClick = { showBankEditDialog = true }
        )
        return
    }

    // Overlay Chat
    if (chatOrder != null) {
        val order = chatOrder!!
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

    // Overlay Profil
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

            // Kartu Cepat Keuangan
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .clickable { showFinanceDetail = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LdyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total Pendapatan", color = Color.White.copy(0.8f), fontSize = 12.sp)
                        Text(
                            "Rp ${totalRevenue.toString().reversed().chunked(3).joinToString(".").reversed()}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Box(
                        modifier = Modifier.size(40.dp).background(Color.White.copy(0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                    }
                }
            }

            // Tab Selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LaundryTab("Masuk", incomingOrders.size, selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                LaundryTab("Diproses", processingOrders.size, selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
                LaundryTab("Selesai", completedOrders.size, selectedTab == 2, Modifier.weight(1f)) { selectedTab = 2 }
            }

            // Daftar Pesanan berdasarkan Tab
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
                                                message = "Sistem akan memeriksa saldo customer untuk Order #${order.id.takeLast(5).uppercase()}. Jika sudah cukup, status berubah menjadi \"Diproses\".",
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
                                            message = "Status Order #${order.id.takeLast(5).uppercase()} akan diubah menjadi \"Siap Diantar\" dan driver dapat mengantarnya ke customer.",
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
private fun FinanceDetailScreen(
    balance: Long,
    totalRevenue: Long,
    totalWithdrawal: Long,
    bankName: String,
    bankAccountNumber: String,
    bankAccountName: String,
    bankChangeStatus: String,
    onBack: () -> Unit,
    onWithdrawClick: () -> Unit,
    onEditBankClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = LdyBg) {
        Column {
            // Header Keuangan
            Surface(color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LdyPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manajemen Keuangan", fontSize = 18.sp, fontWeight = FontWeight.Black, color = LdyTextMain)
                }
            }

            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FinanceCard("Total Pendapatan", totalRevenue, LdyPrimary, Icons.Default.Payments)
                FinanceCard("Saldo Saat Ini", balance, LdySuccess, Icons.Default.AccountBalanceWallet)
                FinanceCard("Total Penarikan", totalWithdrawal, LdyWarning, Icons.Default.History)

                Spacer(modifier = Modifier.height(8.dp))
                
                // Bagian Rekening
                Text("Rekening Tujuan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = LdyTextMain)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        if (bankAccountNumber.isEmpty()) {
                            Text("Belum ada rekening diatur", fontSize = 13.sp, color = LdyTextSub)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(32.dp).background(LdySecondary, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AccountBalance, null, modifier = Modifier.size(16.dp), tint = LdyPrimary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(bankName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("$bankAccountNumber a/n $bankAccountName", fontSize = 12.sp, color = LdyTextSub)
                                }
                            }
                        }
                        
                        if (bankChangeStatus == "PENDING") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(color = LdyWarning.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    "Permintaan perubahan sedang ditinjau admin",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 11.sp, color = LdyWarning, fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onEditBankClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LdyPrimary)
                            ) {
                                Text(if (bankAccountNumber.isEmpty()) "Atur Rekening" else "Ubah Rekening", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onWithdrawClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LdyPrimary)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Tarik Saldo", fontWeight = FontWeight.Black)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun FinanceCard(label: String, amount: Long, color: Color, icon: ImageVector) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(color.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, fontSize = 12.sp, color = LdyTextSub)
                Text(
                    "Rp ${amount.toString().reversed().chunked(3).joinToString(".").reversed()}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = LdyTextMain
                )
            }
        }
    }
}

@Composable
private fun WithdrawDialog(
    currentBalance: Long,
    bankName: String,
    accountNumber: String,
    accountName: String,
    onDismiss: () -> Unit, 
    onConfirm: (Long) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val amount = amountText.toLongOrNull() ?: 0L
    val isValid = amount >= 10000 && amount <= currentBalance
    var showConfirmModal by remember { mutableStateOf(false) }

    if (showConfirmModal) {
        AlertDialog(
            onDismissRequest = { showConfirmModal = false },
            title = { Text("Konfirmasi Penarikan", fontWeight = FontWeight.Black) },
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tarik saldo sebesar Rp ${amountText.reversed().chunked(3).joinToString(".").reversed()}?")
                    Surface(color = LdySecondary, shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Text("Tujuan Transfer:", fontSize = 11.sp, color = LdyTextSub)
                            Text(bankName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("$accountNumber a/n $accountName", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onConfirm(amount) },
                    colors = ButtonDefaults.buttonColors(containerColor = LdyPrimary)
                ) { Text("Ya, Tarik Sekarang") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmModal = false }) { Text("Batal", color = LdyTextSub) }
            },
            containerColor = Color.White
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tarik Saldo", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Saldo tersedia: Rp ${currentBalance.toString().reversed().chunked(3).joinToString(".").reversed()}", fontSize = 13.sp, color = LdyTextSub)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amountText = it },
                    label = { Text("Nominal Penarikan") },
                    placeholder = { Text("Min Rp 10.000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LdyPrimary, focusedLabelColor = LdyPrimary)
                )
                if (amountText.isNotEmpty() && amount < 10000) {
                    Text("Minimal penarikan Rp 10.000", color = Color.Red, fontSize = 11.sp)
                } else if (amount > currentBalance) {
                    Text("Saldo tidak mencukupi", color = Color.Red, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { showConfirmModal = true },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = LdyPrimary)
            ) { Text("Lanjut") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = LdyTextSub) }
        },
        containerColor = Color.White
    )
}

@Composable
private fun BankEditDialog(onDismiss: () -> Unit, onConfirm: (BankMethod, String, String) -> Unit) {
    val methods = listOf(
        BankMethod("1", "BNI", R.drawable.logo_dana), 
        BankMethod("2", "BCA", R.drawable.logo_bca),
        BankMethod("3", "GOPAY", R.drawable.logo_gopay),
        BankMethod("4", "DANA", R.drawable.logo_dana)
    )
    
    var selectedMethod by remember { mutableStateOf<BankMethod?>(null) }
    var accountName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(0) }

    if (step == 0) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Pilih Bank / E-Wallet", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    methods.forEach { method ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { 
                                selectedMethod = method
                                step = 1
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = LdySecondary.copy(0.3f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = method.imageRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(method.name, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
            containerColor = Color.White
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Lengkapi Data Rekening", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Metode: ${selectedMethod?.name}", fontWeight = FontWeight.Bold, color = LdyPrimary)
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("Nama Pemilik Rekening") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("Nomor Rekening / HP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text("Perubahan data rekening memerlukan persetujuan admin (1x24 jam).", fontSize = 11.sp, color = LdyTextSub)
                }
            },
            confirmButton = {
                Button(
                    onClick = { onConfirm(selectedMethod!!, accountName, accountNumber) },
                    enabled = accountName.isNotEmpty() && accountNumber.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = LdyPrimary)
                ) { Text("Ajukan Perubahan") }
            },
            dismissButton = { TextButton(onClick = { step = 0 }) { Text("Kembali") } },
            containerColor = Color.White
        )
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
