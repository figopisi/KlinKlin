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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.ui.theme.BrandBlue
import com.example.klinklinapps.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onBack: () -> Unit, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                text = "Masuk",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Silahkan masukkan email yang telah didaftarkan!",
                fontSize = 16.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(text = "Email*", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            TextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (email.isNotEmpty()) {
                        IconButton(onClick = { email = "" }) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(text = "Kata Sandi*", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            TextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (password.isNotEmpty()) {
                        IconButton(onClick = { password = "" }) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Lupa kata sandi?",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onLoginSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Lanjutkan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
