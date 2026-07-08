package com.example.klinklinapps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Halaman kelola profil — dipakai customer & driver (dan laundry).
 * Bisa edit nama/telepon/alamat, ganti password (opsional), logout, dan hapus akun.
 * onBack null -> dipakai sebagai tab (tanpa tombol back).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileManagementScreen(
    initialName: String,
    email: String,
    initialPhone: String,
    initialAddress: String,
    roleLabel: String,
    isSaving: Boolean,
    onSave: (name: String, phone: String, address: String, currentPassword: String, newPassword: String) -> Unit,
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var phone by remember(initialPhone) { mutableStateOf(initialPhone) }
    var address by remember(initialAddress) { mutableStateOf(initialAddress) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
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

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDeleteAccount()
            }
        )
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = KlinKlinTheme.Primary,
        unfocusedBorderColor = KlinKlinTheme.Secondary,
        focusedLabelColor = KlinKlinTheme.Primary,
        cursorColor = KlinKlinTheme.Primary,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedTextColor = KlinKlinTheme.Foreground,
        unfocusedTextColor = KlinKlinTheme.Foreground
    )

    Scaffold(
        containerColor = KlinKlinTheme.Background,
        topBar = {
            TopAppBar(
                title = { Text("Kelola Profil", fontWeight = FontWeight.Black, color = KlinKlinTheme.Foreground) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = KlinKlinTheme.Primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar + email + role
            Box(
                modifier = Modifier.size(88.dp).clip(CircleShape).background(KlinKlinTheme.Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    name.take(1).ifBlank { "?" }.uppercase(),
                    color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(email, fontSize = 14.sp, color = KlinKlinTheme.MutedForeground)
            Surface(color = KlinKlinTheme.Secondary, shape = RoundedCornerShape(8.dp)) {
                Text(
                    roleLabel,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = KlinKlinTheme.Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        singleLine = true, colors = fieldColors
                    )
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it },
                        label = { Text("Nomor Telepon") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        singleLine = true, colors = fieldColors
                    )
                    OutlinedTextField(
                        value = address, onValueChange = { address = it },
                        label = { Text("Alamat") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        minLines = 2, colors = fieldColors
                    )
                    HorizontalDivider(color = KlinKlinTheme.Background, thickness = 1.dp)
                    Text(
                        "Ganti Password (opsional)",
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KlinKlinTheme.MutedForeground
                    )
                    OutlinedTextField(
                        value = currentPassword, onValueChange = { currentPassword = it },
                        label = { Text("Password Saat Ini") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        singleLine = true, colors = fieldColors,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = newPassword, onValueChange = { newPassword = it },
                        label = { Text("Password Baru") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        singleLine = true, colors = fieldColors,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null, tint = KlinKlinTheme.MutedForeground
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onSave(name, phone, address, currentPassword, newPassword) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KlinKlinTheme.Primary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                } else {
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KlinKlinTheme.Foreground)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hapus Akun Permanen", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
