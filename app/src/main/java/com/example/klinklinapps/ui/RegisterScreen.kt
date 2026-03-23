package com.example.klinklinapps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.ui.theme.BrandBlue
import com.example.klinklinapps.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(White)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Daftar",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Silahkan lengkapi data diri Anda untuk mendaftar!",
                fontSize = 16.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(text = "Email*", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Email Anda") },
                trailingIcon = {
                    if (email.isNotEmpty()) {
                        IconButton(onClick = { email = "" }) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Gray)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = "Kata Sandi*", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Password Anda") },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = "Konfirmasi Kata Sandi*", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Konfirmasi Password Anda") },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp)
            )
            
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red, fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    if (password != confirmPassword) {
                        // Error message handled by ViewModel normally, but we can check here too
                        // Let's just pass it to register and handle logic there or here.
                        // For simplicity, let's just use the VM.
                        viewModel.register(email, password, onRegisterSuccess)
                    } else {
                        viewModel.register(email, password, onRegisterSuccess)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Daftar Sekarang", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearError()
        }
    }
}
