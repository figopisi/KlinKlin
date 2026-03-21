package com.example.klinklinapps.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.R
import com.example.klinklinapps.ui.theme.BrandBlue
import com.example.klinklinapps.ui.theme.KlinKlinAppsTheme
import com.example.klinklinapps.ui.theme.White

@Composable
fun WelcomeScreen(onLoginNavigate: () -> Unit, onRegisterNavigate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_klinklin),
            contentDescription = "Logo",
            modifier = Modifier.size(140.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Selamat datang di klinklin!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            textAlign = TextAlign.Start,
            lineHeight = 36.sp, // Increased line spacing
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Satu langkah kecil untuk merencanakan perjalanan hari ini, adalah langkah besar untuk pengalaman bermakna esok hari. Mulai bersama Klinklin!",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp, // Increased line spacing
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onLoginNavigate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Masuk", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onRegisterNavigate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, BrandBlue),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Daftar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomePreview() {
    KlinKlinAppsTheme {
        WelcomeScreen({}, {})
    }
}
