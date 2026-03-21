package com.example.klinklinapps.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.ui.theme.*

data class TopUpMethod(val id: String, val name: String, val iconText: String, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen(onBack: () -> Unit, onMethodSelected: (TopUpMethod) -> Unit) {
    val methods = listOf(
        TopUpMethod("1", "DANA", "D", Color(0xFF118EEA)),
        TopUpMethod("2", "DEPOSIT", "DEP", BrandBlue),
        TopUpMethod("3", "GOPAY", "G", Color(0xFF00AED6)),
        TopUpMethod("4", "OTTOCASH", "O", Color(0xFFE53935)),
        TopUpMethod("5", "OVO", "O", Color(0xFF4C2A86))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Layanan Isi Saldo", fontWeight = FontWeight.Black, color = BrandBlue) },
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
        ) {
            // Search Bar Placeholder
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Cari...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = White,
                    focusedContainerColor = White
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(methods) { method ->
                    TopUpMethodItem(method) { onMethodSelected(method) }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Gray200, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun TopUpMethodItem(method: TopUpMethod, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = method.color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(method.iconText, color = method.color, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(method.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Gray800)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Gray500)
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
                title = { Text("Isi Saldo", fontWeight = FontWeight.Black, color = BrandBlue) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrandBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            Button(
                onClick = { if (amount.isNotEmpty()) onConfirm(amount.toInt()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lanjut", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Gray100)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = method.color) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(method.iconText, color = White, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(method.name, fontWeight = FontWeight.Black, fontSize = 18.sp, color = Gray800)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = White) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nomor Pelanggan", fontSize = 12.sp, color = Gray500)
                    TextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Pilih Nominal", fontWeight = FontWeight.Bold, color = Gray800)
            Spacer(modifier = Modifier.height(12.dp))

            amounts.forEach { amt ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { amount = amt.toString() },
                    shape = RoundedCornerShape(12.dp),
                    color = White,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        if (amount == amt.toString()) BrandBlue else Color.Transparent
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${method.name} ${amt / 1000}.000", fontWeight = FontWeight.Bold)
                        Text("Rp${amt + 1500}", fontSize = 12.sp, color = Gray500)
                    }
                }
            }
        }
    }
}
