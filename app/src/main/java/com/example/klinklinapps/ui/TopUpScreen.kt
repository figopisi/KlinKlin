package com.example.klinklinapps.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.R
import java.util.Locale

data class TopUpMethod(val id: String, val name: String, val imageRes: Int, val color: Color)

private const val ADMIN_FEE = 1500

private fun formatRp(value: Int): String =
    "Rp ${String.format(Locale("id", "ID"), "%,d", value)}"

// ==================== PILIH METODE ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen(onBack: () -> Unit, onMethodSelected: (TopUpMethod) -> Unit) {
    val methods = listOf(
        TopUpMethod("1", "DANA", R.drawable.logo_dana, Color(0xFF118EEA)),
        TopUpMethod("2", "BCA", R.drawable.logo_bca, Color(0xFF0A4DA2)),
        TopUpMethod("3", "GOPAY", R.drawable.logo_gopay, Color(0xFF00AED6)),
        TopUpMethod("4", "OVO", R.drawable.logo_ovo, Color(0xFF4C2A86))
    )

    Scaffold(
        containerColor = KlinKlinTheme.Background,
        topBar = {
            TopAppBar(
                title = { Text("Isi Saldo KlinPay", fontWeight = FontWeight.Black, color = KlinKlinTheme.Foreground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = KlinKlinTheme.Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionTitle("Pilih Metode Pembayaran")
                Spacer(modifier = Modifier.height(4.dp))
            }
            items(methods) { method ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick { onMethodSelected(method) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = KlinKlinTheme.Background
                        ) {
                            Image(
                                painter = painterResource(id = method.imageRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                method.name,
                                fontWeight = FontWeight.Black,
                                color = KlinKlinTheme.Foreground,
                                fontSize = 15.sp
                            )
                            Text(
                                "Instan • Tanpa antre",
                                fontSize = 11.sp,
                                color = KlinKlinTheme.MutedForeground
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = KlinKlinTheme.MutedForeground
                        )
                    }
                }
            }
        }
    }
}

// ==================== PILIH NOMINAL ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpAmountScreen(method: TopUpMethod, onBack: () -> Unit, onConfirm: (Int) -> Unit) {
    var customText by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf<Int?>(null) }
    val presets = listOf(20000, 50000, 75000, 100000, 150000, 200000)

    // Nominal custom mengalahkan preset; minimal Rp 10.000
    val amount = customText.toIntOrNull() ?: selectedPreset
    val isValid = amount != null && amount >= 10000

    Scaffold(
        containerColor = KlinKlinTheme.Background,
        topBar = {
            TopAppBar(
                title = { Text("Isi Saldo", fontWeight = FontWeight.Black, color = KlinKlinTheme.Foreground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = KlinKlinTheme.Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 12.dp) {
                Button(
                    onClick = { if (isValid) onConfirm(amount!!) },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    contentPadding = PaddingValues(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isValid) Brush.horizontalGradient(
                                    listOf(KlinKlinTheme.Primary, KlinKlinTheme.Accent)
                                )
                                else Brush.horizontalGradient(
                                    listOf(
                                        KlinKlinTheme.Primary.copy(0.4f),
                                        KlinKlinTheme.Accent.copy(0.4f)
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isValid) "Bayar ${formatRp(amount!! + ADMIN_FEE)}" else "Pilih Nominal Dulu",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header metode terpilih
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = KlinKlinTheme.Background
                    ) {
                        Image(
                            painter = painterResource(id = method.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            method.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = KlinKlinTheme.Foreground
                        )
                        Text(
                            "Metode pembayaran terpilih",
                            fontSize = 11.sp,
                            color = KlinKlinTheme.MutedForeground
                        )
                    }
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = KlinKlinTheme.Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("Pilih Nominal")
            Spacer(modifier = Modifier.height(14.dp))

            // Grid chip nominal 2 kolom
            presets.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { preset ->
                        val selected = customText.isEmpty() && selectedPreset == preset
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .bounceClick {
                                    selectedPreset = preset
                                    customText = ""
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) KlinKlinTheme.Primary else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (selected) KlinKlinTheme.Primary else KlinKlinTheme.Secondary
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    formatRp(preset),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (selected) Color.White else KlinKlinTheme.Foreground
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nominal custom
            Text(
                "Atau masukkan nominal lain",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = KlinKlinTheme.MutedForeground
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customText,
                onValueChange = { input ->
                    customText = input.filter { it.isDigit() }.take(9)
                    if (customText.isNotEmpty()) selectedPreset = null
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Minimal Rp 10.000", color = KlinKlinTheme.MutedForeground.copy(0.5f)) },
                prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = KlinKlinTheme.Foreground) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KlinKlinTheme.Primary,
                    unfocusedBorderColor = KlinKlinTheme.Secondary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = KlinKlinTheme.Foreground,
                    unfocusedTextColor = KlinKlinTheme.Foreground,
                    cursorColor = KlinKlinTheme.Primary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Rincian pembayaran
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(KlinKlinTheme.Secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = KlinKlinTheme.Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Rincian Pembayaran",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = KlinKlinTheme.Foreground
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    PaymentDetailRow("Nominal Top Up", if (amount != null) formatRp(amount) else "—")
                    Spacer(modifier = Modifier.height(8.dp))
                    PaymentDetailRow("Biaya Admin", formatRp(ADMIN_FEE))
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = KlinKlinTheme.Background, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "Total Bayar",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = KlinKlinTheme.Foreground
                        )
                        Text(
                            if (amount != null) formatRp(amount + ADMIN_FEE) else "—",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = KlinKlinTheme.Primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PaymentDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = KlinKlinTheme.MutedForeground)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KlinKlinTheme.Foreground)
    }
}
