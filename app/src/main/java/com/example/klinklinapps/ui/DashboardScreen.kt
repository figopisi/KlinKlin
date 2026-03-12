package com.example.klinklinapps.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    balance: Int,
    hasActiveOrder: Boolean,
    onPlaceOrder: () -> Unit, 
    onOpenSubscription: () -> Unit,
    onTopUp: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { 
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:08123456789"))
                        context.startActivity(intent)
                    }) {
                        Surface(shape = CircleShape, color = BrandBlueLight, modifier = Modifier.size(36.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.SupportAgent, contentDescription = "CS", tint = BrandBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KlinKlinLogoIcon()
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("klinklin", fontSize = 22.sp, fontWeight = FontWeight.Black, color = BrandBlue, letterSpacing = (-1).sp)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Surface(shape = CircleShape, color = BrandBlueLight, modifier = Modifier.size(36.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = BrandBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = White,
                tonalElevation = 12.dp,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            ) {
                val items = listOf("Home", "Promos", "Orders", "Chat")
                val icons = listOf(Icons.Default.Home, Icons.Default.Percent, Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Filled.Chat)
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item, fontWeight = FontWeight.Bold) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = BrandBlue, selectedTextColor = BrandBlue, indicatorColor = BrandBlueLight)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(Gray100)) {
            when (selectedTab) {
                0 -> HomeScreen(balance, onPlaceOrder, onOpenSubscription, onTopUp)
                1 -> PromoScreen()
                2 -> OrdersScreen(hasActiveOrder)
                3 -> ChatScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(balance: Int, onPlaceOrder: () -> Unit, onOpenSubscription: () -> Unit, onTopUp: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(bottom = 32.dp)) {
        // Balance Card
        Column(modifier = Modifier.fillMaxWidth().background(White).padding(16.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = BrandBlue
            ) {
                Box(modifier = Modifier.background(DarkGradient)) {
                    Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SunYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("KlinPay", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("Rp", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(balance.toString(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Row {
                            ActionItem(icon = Icons.Default.AddCircle, label = "Top Up", onClick = onTopUp)
                            Spacer(modifier = Modifier.width(20.dp))
                            ActionItem(icon = Icons.Default.History, label = "History", onClick = {})
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        SubscriptionCard(onClick = onOpenSubscription)

        SectionHeader("Layanan Utama")
        val mainServices = listOf(
            ServiceItemData("Wash & Fold", Icons.Default.LocalLaundryService, BrandBlueLight),
            ServiceItemData("Dry Cleaning", Icons.Default.DryCleaning, SunYellow.copy(alpha = 0.2f)),
            ServiceItemData("Ironing", Icons.Default.Iron, BrandBlueLight),
            ServiceItemData("Express", Icons.Default.Speed, SunYellow.copy(alpha = 0.2f))
        )
        LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.height(120.dp).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), userScrollEnabled = false) {
            itemsIndexed(mainServices) { index, service ->
                AnimatedServiceItem(service, index, onClick = onPlaceOrder)
            }
        }

        SectionHeader("Laundry Satuan")
        val deepCleaningItems = listOf(
            ServiceItemData("Sepatu", Icons.Default.IceSkating, BrandBlueLight),
            ServiceItemData("Tas", Icons.Default.ShoppingBag, BrandBlueLight),
            ServiceItemData("Helm", Icons.Default.SportsMotorsports, BrandBlueLight),
            ServiceItemData("Karpet", Icons.Default.GridView, BrandBlueLight)
        )
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            deepCleaningItems.chunked(2).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            AnimatedDeepCleaningCard(item, 0, onClick = onPlaceOrder)
                        }
                    }
                }
            }
        }

        SectionHeader("Add-ons")
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            AddOnItem("Scent Booster", "Wangi 7 hari", "Rp 5.000", Icons.Default.AutoAwesome)
            AddOnItem("Insurance", "Proteksi 10x lipat", "Rp 2.000", Icons.Default.VerifiedUser)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BrandBlue, modifier = Modifier.padding(16.dp))
}

@Composable
fun SubscriptionCard(onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onClick() }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = SunYellow)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("klinklin Plus+", fontWeight = FontWeight.Black, fontSize = 20.sp, color = NavyBlue)
                Text("Hemat s/d 30% tiap bulan!", fontSize = 13.sp, color = NavyBlue.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.width(24.dp))
            Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = BrandBlue), shape = RoundedCornerShape(14.dp)) {
                Text("Daftar", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun AnimatedServiceItem(service: ServiceItemData, index: Int, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Surface(modifier = Modifier.size(60.dp), shape = RoundedCornerShape(18.dp), color = service.color) {
            Box(contentAlignment = Alignment.Center) { Icon(service.icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(28.dp)) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(service.name, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Gray800)
    }
}

@Composable
fun AnimatedDeepCleaningCard(service: ServiceItemData, index: Int, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(20.dp), color = White, shadowElevation = 4.dp) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(BrandBlueLight), contentAlignment = Alignment.Center) {
                Icon(service.icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(service.name, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Gray800)
        }
    }
}

@Composable
fun AddOnItem(title: String, desc: String, price: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(White, RoundedCornerShape(20.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = BrandBlue)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, fontSize = 11.sp, color = Gray500)
        }
        Text(price, fontWeight = FontWeight.Black, color = BrandBlue, fontSize = 14.sp)
    }
}

@Composable
fun ActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.2f)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = SunYellow, modifier = Modifier.size(20.dp)) }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun KlinKlinLogoIcon() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(38.dp)) {
        Surface(
            modifier = Modifier.size(32.dp).offset(y = 6.dp),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 10.dp, bottomEnd = 10.dp),
            color = BrandBlue
        ) {}
        Surface(
            modifier = Modifier.size(24.dp).offset(y = (-6).dp),
            shape = CircleShape,
            color = BrandBlue,
            border = androidx.compose.foundation.BorderStroke(2.dp, White)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = White) {}
            }
        }
        Box(modifier = Modifier.size(12.dp, 8.dp).offset(y = 2.dp).clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)).background(White))
    }
}

@Composable fun PromoScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No Promos") } }
@Composable fun ChatScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Chat Soon") } }
@Composable fun OrdersScreen(hasActiveOrder: Boolean) { 
    Column(Modifier.fillMaxSize().background(Gray100).padding(16.dp)) {
        Text("Pesanan Saya", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Gray800)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (hasActiveOrder) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = BrandBlueLight) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.LocalLaundryService, null, tint = BrandBlue) }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Order #KK-99281", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text("Wash & Fold • 5 kg", fontSize = 12.sp, color = Gray500)
                        }
                        Surface(color = BrandBlueLight, shape = RoundedCornerShape(8.dp)) {
                            Text("DIPROSES", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = BrandBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Gray100)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = SunYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Estimasi Selesai: Hari ini, 18:00", fontSize = 12.sp, color = Gray800, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada riwayat pesanan", color = Gray500)
            }
        }
    }
}

data class ServiceItemData(val name: String, val icon: ImageVector, val color: Color)
