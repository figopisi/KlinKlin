package com.example.klinklinapps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val AdminBg = Color(0xFFF8FAFC)
private val AdminPrimary = Color(0xFF1E293B)
private val AdminAccent = Color(0xFF3B82F6)
private val AdminSuccess = Color(0xFF10B981)
private val AdminError = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(onLogout: () -> Unit) {
    val viewModel: AdminViewModel = viewModel()
    val bankRequests by viewModel.bankRequests
    val isProcessing by viewModel.isProcessing
    
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            }
        )
    }

    Scaffold(
        containerColor = AdminBg,
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel", fontWeight = FontWeight.Black, color = Color.White) },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminPrimary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Stats Header
            Surface(color = AdminPrimary, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Persetujuan Rekening", color = Color.White.copy(0.7f), fontSize = 14.sp)
                    Text("${bankRequests.size} Permintaan Pending", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }

            if (bankRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Semua permintaan sudah diproses", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(bankRequests) { request ->
                        RequestCard(
                            request = request,
                            isProcessing = isProcessing,
                            onApprove = { viewModel.approveBankChange(request) },
                            onReject = { viewModel.rejectBankChange(request) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: BankChangeRequest,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(AdminAccent.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = AdminAccent, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(request.laundryName, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("REKENING LAMA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(request.currentBank, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(request.currentAccount, fontSize = 12.sp)
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.padding(horizontal = 8.dp).align(Alignment.CenterVertically), tint = Color.LightGray)
                Column(modifier = Modifier.weight(1f)) {
                    Text("REKENING BARU", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AdminAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(request.pendingBank, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminAccent)
                    Text(request.pendingAccountNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("a/n ${request.pendingAccountName}", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onReject,
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminError)
                ) {
                    Text("Tolak", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onApprove,
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminSuccess)
                ) {
                    Text("Setujui", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
