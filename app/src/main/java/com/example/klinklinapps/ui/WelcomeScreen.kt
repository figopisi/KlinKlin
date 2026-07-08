package com.example.klinklinapps.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.R

@Composable
fun WelcomeScreen(onLoginNavigate: () -> Unit, onRegisterNavigate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Gradient background lembut agar logo lebih stand out
            .background(Brush.verticalGradient(listOf(Color(0xFFF9FAFC), Color.White)))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).padding(top = 40.dp) // Sedikit naik biar wadah besarnya muat
        ) {
            // --- WADAH LOGO DIPERBESAR (220.dp) ---
            Surface(
                modifier = Modifier.size(220.dp),
                shape = RoundedCornerShape(64.dp), // Corner lebih tumpul biar proporsional
                color = Color.White,
                shadowElevation = 24.dp // Shadow lebih tebal biar berasa "mengambang"
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_klinklin),
                        contentDescription = "Logo",
                        modifier = Modifier.size(160.dp), // Logo juga diperbesar di dalam wadah
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Bersih Berkilau,\nHati Tenang.",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF001C3D), // Ubah ke Navy
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Serahkan urusan cucianmu pada ahlinya, dan nikmati waktu berhargamu bersama keluarga.",
                fontSize = 15.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 22.sp
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onLoginNavigate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                // --- WARNA TOMBOL NAVY BLUE ---
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001C3D)),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    "Mulai Sekarang",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            TextButton(
                onClick = onRegisterNavigate,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    text = "Belum punya akun? Daftar",
                    fontSize = 15.sp,
                    color = Color(0xFF001C3D),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
