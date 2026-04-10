package com.example.klinklinapps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = KlinKlinTheme.Primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KlinKlinTheme.Background
                )
            )
        },
        containerColor = KlinKlinTheme.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(KlinKlinTheme.Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Selamat",
                fontSize = 14.sp,
                color = KlinKlinTheme.MutedForeground
            )
            Text(
                text = "Datang Kembali!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = KlinKlinTheme.Foreground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Silahkan masukkan email yang telah didaftarkan!",
                fontSize = 14.sp,
                color = KlinKlinTheme.MutedForeground,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Email Field ──
            Text(
                text = "Email",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = KlinKlinTheme.Foreground
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Email Anda",
                        color = KlinKlinTheme.MutedForeground.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                },
                trailingIcon = {
                    if (email.isNotEmpty()) {
                        IconButton(onClick = { email = "" }) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                tint = KlinKlinTheme.MutedForeground.copy(alpha = 0.4f)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KlinKlinTheme.Primary,
                    unfocusedBorderColor = KlinKlinTheme.Secondary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = KlinKlinTheme.Foreground,
                    unfocusedTextColor = KlinKlinTheme.Foreground,
                    cursorColor = KlinKlinTheme.Primary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Password Field ──
            Text(
                text = "Kata Sandi",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = KlinKlinTheme.Foreground
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Password Anda",
                        color = KlinKlinTheme.MutedForeground.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                trailingIcon = {
                    if (password.isNotEmpty()) {
                        IconButton(onClick = { password = "" }) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                tint = KlinKlinTheme.MutedForeground.copy(alpha = 0.4f)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KlinKlinTheme.Primary,
                    unfocusedBorderColor = KlinKlinTheme.Secondary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = KlinKlinTheme.Foreground,
                    unfocusedTextColor = KlinKlinTheme.Foreground,
                    cursorColor = KlinKlinTheme.Primary
                ),
                singleLine = true
            )

            // ── Error Message ──
            errorMessage?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFFFEDED),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = it,
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Lupa Kata Sandi ──
            Text(
                text = "Lupa kata sandi?",
                color = KlinKlinTheme.Primary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(48.dp))

            // ── Login Button ──
            Button(
                onClick = { viewModel.login(email, password, onLoginSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KlinKlinTheme.Primary,
                    disabledContainerColor = KlinKlinTheme.Primary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        "Lanjutkan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Clear error when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearError()
        }
    }
}