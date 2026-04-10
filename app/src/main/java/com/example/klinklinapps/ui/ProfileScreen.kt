package com.example.klinklinapps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KlinProfileScreen(
    userName: String,
    userEmail: String,
    userPhone: String,
    userAddress: String,
    subscriptionType: String?,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onOpenSubscription: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    
    // States for editing
    var editedName by remember { mutableStateOf(userName) }
    var editedPhone by remember { mutableStateOf(userPhone) }
    var editedAddress by remember { mutableStateOf(userAddress) }
    var newPassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya", fontWeight = FontWeight.Black) },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profil", tint = BrandBlue)
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Gray500)
                        }
                    } else {
                        IconButton(onClick = { isEditing = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Batal", tint = Color.Red)
                        }
                        IconButton(onClick = { 
                            isEditing = false 
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Simpan", tint = Color(0xFF4CAF50))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray100)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header / Avatar (Reverted to Initials)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(BrandBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = editedName.take(1).uppercase(),
                    color = White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isEditing) {
                Text(text = editedName, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Gray800)
                Text(text = userEmail, fontSize = 14.sp, color = Gray500)
            } else {
                Text(text = "Sedang Mengubah Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Personal Info Section
            ProfileSectionTitle("Informasi Akun")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("Nama Lengkap") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = editedPhone,
                            onValueChange = { editedPhone = it },
                            label = { Text("Nomor Telepon") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = editedAddress,
                            onValueChange = { editedAddress = it },
                            label = { Text("Alamat Lengkap") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Password Baru (Kosongkan jika tidak ganti)") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        ProfileInfoItem(Icons.Default.Person, "Nama", editedName)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Gray100)
                        ProfileInfoItem(Icons.Default.Phone, "Nomor Telepon", editedPhone)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Gray100)
                        ProfileInfoItem(Icons.Default.LocationOn, "Alamat Pengiriman", editedAddress)
                    }
                }
            }
            
            if (!isEditing) {
                Spacer(modifier = Modifier.height(24.dp))
                ProfileSectionTitle("Status Berlangganan")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSubscription() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (subscriptionType != null) SunYellow.copy(alpha = 0.1f) else White
                    )
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (subscriptionType != null) Icons.Default.WorkspacePremium else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (subscriptionType != null) SunYellowVariant else Gray500
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (subscriptionType != null) "KlinKlin Plus+" else "Belum Berlangganan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (subscriptionType != null) NavyBlue else Gray800
                            )
                            Text(
                                text = if (subscriptionType != null) "Paket: $subscriptionType" else "Aktifkan untuk nikmati fitur hemat",
                                fontSize = 13.sp,
                                color = if (subscriptionType != null) NavyBlue.copy(alpha = 0.7f) else Gray500
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Gray500)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        color = BrandBlue,
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, bottom = 8.dp)
    )
}

@Composable
fun ProfileInfoItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(36.dp).background(BrandBlueLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = Gray500)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray800)
        }
    }
}
