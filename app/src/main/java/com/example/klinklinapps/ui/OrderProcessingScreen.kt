package com.example.klinklinapps.ui
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image // Import Image Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource // Import Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.R // IMPORT R PROJECT
import com.example.klinklinapps.ui.theme.*
import kotlinx.coroutines.delay
@Composable
fun OrderProcessingScreen(onFinish: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }

    // LOGIKA PROGRESS (PRESERVED)
    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(40)
            progress += 0.01f
        }
        delay(800)
        onFinish()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ILLUSTRATION BOX DENGAN GRADIENT SOFT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFF0EFFF), Color.White))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.logo_klinklin),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Estimasi Selesai",
                            fontSize = 10.sp,
                            color = Color(0xFF888888),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "20-30 Menit",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF7E72F2)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Laundry Menuju\nKesempurnaan",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF2C2C2C),
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Mitra laundry kami sedang memproses cucianmu dengan standar kebersihan tertinggi.",
            fontSize = 14.sp,
            color = Color(0xFF888888),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(48.dp))
        // MODERN PROGRESS BAR
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color(0xFF7E72F2),
                trackColor = Color(0xFFEEEDFF),
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // DYNAMIC STATUS CARD
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF8F9FA)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(Color(0xFF7E72F2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Jaminan KlinKlin",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color(0xFF2C2C2C)
                    )
                    Text(
                        "Bersih, Wangi, & Tepat Waktu",
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
        }
    }
}