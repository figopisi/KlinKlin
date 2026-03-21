package com.example.klinklinapps.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.R
import com.example.klinklinapps.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun OrderProcessingScreen(onFinish: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        // Simulate processing progress
        while (progress < 1f) {
            delay(50)
            progress += 0.01f
        }
        delay(1000)
        onFinish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration Box (Using Logo as identity)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(BrandBlueLight),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Main Illustration Placeholder
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(150.dp),
                    contentScale = ContentScale.Fit
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = White,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Sampai dalam",
                            fontSize = 12.sp,
                            color = Gray500,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "21-31 min",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandBlue
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Laundrymu lagi disiapin",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = Gray800,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Mitra laundry kami sedang memproses pesananmu dengan sepenuh hati.",
            fontSize = 16.sp,
            color = Gray500,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Modern Progress Bar
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = BrandBlue,
                trackColor = BrandBlueLight,
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Dynamic Status Info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Gray100
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(BrandBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Bakal dapat driver pas",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Gray800
                    )
                    Text(
                        "laundrymu mau jadi",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Gray800
                    )
                    Text(
                        "Kami antar sesuai estimasi waktu",
                        fontSize = 13.sp,
                        color = Gray500
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrderProcessingPreview() {
    KlinKlinAppsTheme {
        OrderProcessingScreen(onFinish = {})
    }
}
