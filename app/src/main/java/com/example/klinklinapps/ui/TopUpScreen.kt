package com.example.klinklinapps.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.ui.theme.*
import com.example.klinklinapps.R

data class TopUpMethod(val id: String, val name: String, val imageRes: Int, val color: Color)

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
        topBar = {
            TopAppBar(
                title = { Text("Layanan Isi Saldo", fontWeight = FontWeight.Black, color = Color(0xFF001C3D)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF001C3D))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF9FBFF))
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Cari metode pembayaran...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )

            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(methods) { method ->
                    TopUpMethodItem(method) { onMethodSelected(method) }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun TopUpMethodItem(method: TopUpMethod, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.background(Color.White).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- UKURAN LOGO DI LIST DIPERBESAR (60.dp) ---
        Surface(
            modifier = Modifier.size(60.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF5F7F9) // Background tipis biar logo putih nggak ilang
        ) {
            Image(
                painter = painterResource(id = method.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(8.dp), // Padding biar nggak mepet
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(method.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Color(0xFF1A2332), fontSize = 16.sp)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF7A869A))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpAmountScreen(method: TopUpMethod, onBack: () -> Unit, onConfirm: (Int) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("08123456789") }
    val amounts = listOf(20000, 50000, 75000, 100000)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Isi Saldo", fontWeight = FontWeight.Black, color = Color(0xFF001C3D)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF001C3D))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = { if (amount.isNotEmpty()) onConfirm(amount.toInt()) },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A4DA2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lanjut Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF9FBFF)).padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // --- UKURAN LOGO DI HEADER DETAIL DIPERBESAR (72.dp) ---
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Image(
                        painter = painterResource(id = method.imageRes),
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(method.name, fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF1A2332))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nomor HP Terdaftar", fontSize = 12.sp, color = Color(0xFF7A869A))
                    TextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Pilih Nominal Isi Saldo", fontWeight = FontWeight.Bold, color = Color(0xFF1A2332))
            Spacer(modifier = Modifier.height(12.dp))

            amounts.forEach { amt ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { amount = amt.toString() },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(2.dp, if (amount == amt.toString()) Color(0xFF0A4DA2) else Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${method.name} Top Up Rp ${amt / 1000}rb", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Total Bayar: Rp${amt + 1500}", fontSize = 12.sp, color = Color(0xFF7A869A))
                    }
                }
            }
        }
    }
}
