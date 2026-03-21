package com.example.klinklinapps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.ui.theme.*

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val price: Int,
    val description: String,
    val benefits: List<String>,
    val isBestSeller: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(onBack: () -> Unit, onSubscribeSuccess: () -> Unit) {
    val plans = listOf(
        SubscriptionPlan(
            "lite", "Lite", 80000, "Cocok untuk jomblo / individu",
            listOf("Kuota 15kg/bulan", "2x Gratis Antar-Jemput", "Layanan Standar")
        ),
        SubscriptionPlan(
            "family", "Family", 300000, "Pilihan terbaik untuk keluarga",
            listOf("Kuota 40kg/bulan", "4x Gratis Antar-Jemput", "Prioritas 24 Jam", "Cuci Wangi Premium"),
            isBestSeller = true
        ),
        SubscriptionPlan(
            "sultan", "Sultan", 500000, "Layanan tanpa kompromi",
            listOf("Kuota 80kg/bulan", "Unlimited Antar-Jemput", "Penanganan Khusus (Separation)", "Asuransi Inklusif")
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Klinklin Plus+", fontWeight = FontWeight.Black, color = BrandBlue) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrandBlue)
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
                .background(Gray100)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Current Subscription Status
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = BrandBlueLight
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = SunYellow)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Status Langganan", fontSize = 12.sp, color = BrandBlue)
                        Text("Belum Berlangganan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Pilih Paket Klinklin Plus+", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = NavyBlue)
            Spacer(modifier = Modifier.height(16.dp))

            plans.forEach { plan ->
                SubscriptionCard(plan, onSubscribe = onSubscribeSuccess)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SubscriptionCard(plan: SubscriptionPlan, onSubscribe: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    plan.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NavyBlue,
                    modifier = Modifier.weight(1f)
                )
                if (plan.isBestSeller) {
                    Surface(
                        color = SunYellow,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "BEST SELLER",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = NavyBlue
                        )
                    }
                }
            }
            Text(plan.description, fontSize = 14.sp, color = Gray500)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            plan.benefits.forEach { benefit ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(benefit, fontSize = 13.sp, color = Gray800)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Gray200)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mulai dari", fontSize = 12.sp, color = Gray500)
                    Text("Rp ${plan.price}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BrandBlue)
                }
                Button(
                    onClick = onSubscribe,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Berlangganan", color = White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubscriptionPreview() {
    KlinKlinAppsTheme {
        SubscriptionScreen(onBack = {}, onSubscribeSuccess = {})
    }
}
