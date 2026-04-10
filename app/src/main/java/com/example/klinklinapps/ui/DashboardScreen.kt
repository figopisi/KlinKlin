package com.example.klinklinapps.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.data.ChatMessage
import com.example.klinklinapps.data.Order
import com.example.klinklinapps.ui.theme.White
import kotlinx.coroutines.delay
import java.util.Locale

// ==================== COLOR THEME ====================
object KlinKlinTheme {
    val Background = Color(0xFFF0F7FF)
    val Foreground = Color(0xFF1A2332)
    val Primary = Color(0xFF5B9BD5)
    val Secondary = Color(0xFFE3F2FD)
    val MutedForeground = Color(0xFF64748B)
    val Accent = Color(0xFF42A5F5)

    // Biru tua untuk teks promo carousel
    val PromoDarkText = Color(0xFF1565C0)
    val PromoDarkTextSub = Color(0xFF1976D2)

    // Service Card Colors
    val ServiceBg1 = Color(0xFFE3F2FD)
    val ServiceBg2 = Color(0xFFBBDEFB)
    val ServiceBg3 = Color(0xFFD1E8F7)
    val ServiceBg4 = Color(0xFFB3E5FC)

    // Promo Colors
    val PromoBg1 = Color(0xFFBBDEFB)
    val PromoBg2 = Color(0xFFC5E1F5)
    val PromoBg3 = Color(0xFFD4E9F7)
}

// ==================== DATA MODELS ====================
data class Service(
    val name: String,
    val desc: String,
    val icon: ImageVector,
    val bgColor: Color,
    val badge: String? = null
)

data class PromoData(
    val id: Int,
    val title: String,
    val desc: String,
    val discount: String,
    val bgColor: Color,
    val icon: ImageVector
)

data class WeatherDay(
    val day: String,
    val icon: ImageVector,
    val tempHigh: Int,
    val tempLow: Int,
    val condition: String,
    val laundryScore: Int, // 1-5, seberapa cocok untuk laundry
    val humidity: Int
)

// ==================== HEADER COMPONENT ====================
@Composable
fun Header(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(KlinKlinTheme.Secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = KlinKlinTheme.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Halo Bli,",
                    fontSize = 12.sp,
                    color = KlinKlinTheme.MutedForeground
                )
                Text(
                    text = userName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = KlinKlinTheme.Foreground
                )
            }
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = KlinKlinTheme.Primary
            )
        }
    }
}

// ==================== ACTIVE ORDER REMINDER ====================
@Composable
fun ActiveOrderReminder(message: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KlinKlinTheme.Primary)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = KlinKlinTheme.Accent
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cucianmu Selesai!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = message,
                    color = Color.White.copy(0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ==================== WALLET CARD ====================
@Composable
fun WalletCard(balance: Int, onTopUp: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(KlinKlinTheme.Secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = KlinKlinTheme.Primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Saldo KlinPay",
                    fontSize = 11.sp,
                    color = KlinKlinTheme.MutedForeground
                )
                Text(
                    text = "Rp ${String.format(Locale("id", "ID"), "%,d", balance)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = KlinKlinTheme.Foreground
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = KlinKlinTheme.Primary,
                modifier = Modifier.clickable { onTopUp() }
            ) {
                Text(
                    text = "Top Up",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

// ==================== PROMO CAROUSEL (UPDATED COLORS) ====================
@Composable
fun PromoCarousel() {
    val promos = listOf(
        PromoData(1, "Bikin Sepatu Kinclong!", "Cuci sepatu premium dengan teknologi terbaru", "20%", KlinKlinTheme.PromoBg1, Icons.Default.AutoAwesome),
        PromoData(2, "Diskon Merchant", "Hemat lebih banyak untuk member setia", "30%", KlinKlinTheme.PromoBg2, Icons.Default.Percent),
        PromoData(3, "Gratis Ongkir", "Untuk pemesanan minimal Rp 50.000", "FREE", KlinKlinTheme.PromoBg3, Icons.Default.LocalShipping)
    )

    var currentPage by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "iconWobble")
    val iconRotation by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconRotation"
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            currentPage = (currentPage + 1) % promos.size
        }
    }

    Column(modifier = Modifier.padding(vertical = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Promo Minggu Ini",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = KlinKlinTheme.Foreground
            )
            Text(
                text = "Lihat Semua",
                fontSize = 12.sp,
                color = KlinKlinTheme.Primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .padding(horizontal = 24.dp)
        ) {
            promos.forEachIndexed { index, promo ->
                AnimatedVisibility(
                    visible = currentPage == index,
                    enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                    exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val cardScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.965f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        ),
                        label = "cardScale"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(cardScale)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = promo.bgColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = KlinKlinTheme.PromoDarkText.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "PROMO SPESIAL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        // ← BIRU TUA untuk badge label
                                        color = KlinKlinTheme.PromoDarkText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = promo.title,
                                    fontWeight = FontWeight.Black,
                                    // ← BIRU TUA untuk judul utama
                                    color = KlinKlinTheme.PromoDarkText,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = promo.desc,
                                    fontSize = 12.sp,
                                    // ← Biru tua opacity diturunin untuk subtitle
                                    color = KlinKlinTheme.PromoDarkText.copy(alpha = 0.55f),
                                    lineHeight = 14.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    // ← Tombol pakai biru tua
                                    color = KlinKlinTheme.PromoDarkText
                                ) {
                                    Text(
                                        text = "KLAIM SEKARANG",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Card(
                                modifier = Modifier.size(90.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(0.4f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = promo.icon,
                                        contentDescription = null,
                                        tint = KlinKlinTheme.Primary,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .rotate(iconRotation)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = promo.discount,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = KlinKlinTheme.Primary
                                    )
                                    Text(
                                        text = "OFF",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KlinKlinTheme.Primary.copy(0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(promos.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (currentPage == index) 16.dp else 6.dp, 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (currentPage == index) KlinKlinTheme.Primary
                            else KlinKlinTheme.Primary.copy(0.2f)
                        )
                )
            }
        }
    }
}

// ==================== SERVICE CARD ====================
@Composable
fun ServiceCard(service: Service, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(service.bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = service.icon,
                        contentDescription = null,
                        tint = KlinKlinTheme.Primary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = service.name,
                    fontWeight = FontWeight.Black,
                    color = KlinKlinTheme.Foreground,
                    fontSize = 14.sp
                )
                Text(
                    text = service.desc,
                    color = KlinKlinTheme.MutedForeground,
                    fontSize = 11.sp
                )
            }

            if (service.badge != null) {
                Surface(
                    color = KlinKlinTheme.Accent,
                    shape = RoundedCornerShape(topEnd = 24.dp, bottomStart = 14.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = service.badge,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// ==================== SUBSCRIPTION CARD ====================
@Composable
fun SubscriptionCard(onUpgrade: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable { onUpgrade() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = KlinKlinTheme.Primary)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bersih Plus+",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = "Hemat s/d 30% tiap bulan!",
                    color = Color.White.copy(0.7f),
                    fontSize = 12.sp
                )
            }
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Upgrade",
                    color = KlinKlinTheme.Primary,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ==================== SMART PLANNER CARD ====================
@Composable
fun SmartPlannerCard(onOpen: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable { onOpen() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(KlinKlinTheme.Secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = KlinKlinTheme.Primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Smart Planner",
                    fontWeight = FontWeight.Bold,
                    color = KlinKlinTheme.Foreground
                )
                Text(
                    text = "Atur jadwal laundry otomatis",
                    fontSize = 12.sp,
                    color = KlinKlinTheme.MutedForeground
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = KlinKlinTheme.MutedForeground
            )
        }
    }
}

// ==================== WEATHER ANALYTICS CARD (NEW) ====================
@Composable
fun WeatherAnalyticsCard() {
    // Mock data — ganti dengan data real dari API cuaca
    val weatherDays = listOf(
        WeatherDay("Hari ini", Icons.Default.WbSunny, 32, 26, "Cerah", 5, 65),
        WeatherDay("Besok", Icons.Default.WbCloudy, 29, 25, "Berawan", 3, 75),
        WeatherDay("Lusa", Icons.Default.Thunderstorm, 27, 24, "Hujan", 1, 88),
        WeatherDay("Kamis", Icons.Default.WbSunny, 33, 27, "Cerah", 5, 60),
        WeatherDay("Jumat", Icons.Default.WbCloudy, 30, 26, "Mendung", 2, 80)
    )

    val todayScore = weatherDays.first().laundryScore

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(KlinKlinTheme.Secondary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Cuaca & Laundry",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = KlinKlinTheme.Foreground
                        )
                        Text(
                            text = "Denpasar, Bali",
                            fontSize = 11.sp,
                            color = KlinKlinTheme.MutedForeground
                        )
                    }
                }
                // Laundry score hari ini
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        todayScore >= 4 -> Color(0xFF43A047).copy(0.12f)
                        todayScore >= 3 -> Color(0xFFFFB300).copy(0.12f)
                        else -> Color(0xFFE53935).copy(0.12f)
                    }
                ) {
                    Text(
                        text = when {
                            todayScore >= 4 -> "✓ Ideal Laundry"
                            todayScore >= 3 -> "~ Cukup Baik"
                            else -> "✗ Kurang Ideal"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            todayScore >= 4 -> Color(0xFF43A047)
                            todayScore >= 3 -> Color(0xFFFF8F00)
                            else -> Color(0xFFE53935)
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5-day forecast row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weatherDays.forEach { day ->
                    WeatherDayItem(day = day)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Laundry advice banner
            val advice = when {
                weatherDays[0].laundryScore >= 4 ->
                    "Hari ini cuaca cerah, sempurna untuk laundry! Pakaian kering lebih cepat."
                weatherDays[0].laundryScore >= 3 ->
                    "Cuaca cukup mendukung, tapi persiapkan pengering jika mendung tiba-tiba."
                else ->
                    "Cuaca kurang ideal hari ini. Pertimbangkan untuk laundry besok lusa."
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = KlinKlinTheme.Secondary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = KlinKlinTheme.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = advice,
                        fontSize = 11.sp,
                        color = KlinKlinTheme.Primary,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDayItem(day: WeatherDay) {
    val laundryColor = when {
        day.laundryScore >= 4 -> Color(0xFF43A047)
        day.laundryScore >= 3 -> Color(0xFFFFB300)
        else -> Color(0xFFE53935)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(56.dp)
    ) {
        Text(
            text = day.day.take(6),
            fontSize = 10.sp,
            color = KlinKlinTheme.MutedForeground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Icon(
            imageVector = day.icon,
            contentDescription = null,
            tint = when (day.icon) {
                Icons.Default.WbSunny -> Color(0xFFFFB300)
                Icons.Default.Thunderstorm -> Color(0xFF5C6BC0)
                else -> Color(0xFF90A4AE)
            },
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${day.tempHigh}°",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = KlinKlinTheme.Foreground
        )
        Text(
            text = "${day.tempLow}°",
            fontSize = 10.sp,
            color = KlinKlinTheme.MutedForeground
        )
        Spacer(modifier = Modifier.height(6.dp))
        // Laundry score dots
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(5) { i ->
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (i < day.laundryScore) laundryColor
                            else laundryColor.copy(alpha = 0.2f)
                        )
                )
            }
        }
    }
}

// ==================== BOTTOM NAVIGATION (WITH OPACITY FLOW) ====================
@Composable
fun BottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val navItems = listOf(
        Pair(Icons.Default.Home, "Home"),
        Pair(Icons.Default.LocalOffer, "Promo"),
        Pair(Icons.AutoMirrored.Filled.ReceiptLong, "Pesanan"),
        Pair(Icons.AutoMirrored.Filled.Chat, "Chat"),
        Pair(Icons.Default.Person, "Profil")
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(72.dp)
            .shadow(20.dp, RoundedCornerShape(36.dp)),
        shape = RoundedCornerShape(36.dp),
        color = KlinKlinTheme.Primary
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, (icon, label) ->
                val isSelected = selectedTab == index
                // Opacity makin tinggi makin dekat ke tab aktif
                val distanceFromSelected = Math.abs(index - selectedTab)
                val iconAlpha = when (distanceFromSelected) {
                    0 -> 1f
                    1 -> 0.55f
                    else -> 0.3f
                }

                val animatedAlpha by animateFloatAsState(
                    targetValue = iconAlpha,
                    animationSpec = tween(200),
                    label = "navAlpha$index"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSelected(index) }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) KlinKlinTheme.Accent
                        else Color.White.copy(alpha = animatedAlpha),
                        modifier = Modifier.size(24.dp)
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(KlinKlinTheme.Accent)
                        )
                    }
                }
            }
        }
    }
}

// ==================== ORDERS SCREEN ====================
@Composable
fun KlinOrdersScreen(ordersViewModel: OrdersViewModel, onClick: (Order) -> Unit) {
    val orders by ordersViewModel.orders

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 32.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Riwayat Pesanan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = KlinKlinTheme.Foreground
            )
        }
        if (orders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.AutoMirrored.Filled.ReceiptLong,
                            null,
                            modifier = Modifier.size(56.dp),
                            tint = KlinKlinTheme.MutedForeground.copy(0.3f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Belum ada pesanan",
                            color = KlinKlinTheme.MutedForeground,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Mulai laundry pertamamu!",
                            color = KlinKlinTheme.MutedForeground.copy(0.6f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(orders) { order ->
                KlinCustomerOrderCard(order) { onClick(order) }
            }
        }
    }
}

@Composable
fun KlinCustomerOrderCard(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = KlinKlinTheme.Secondary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocalLaundryService, null, tint = KlinKlinTheme.Primary)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Order #${order.id.takeLast(5).uppercase()}", fontWeight = FontWeight.Bold)
                    Text("Layanan Laundry", fontSize = 10.sp, color = KlinKlinTheme.MutedForeground)
                }
                Surface(color = KlinKlinTheme.Secondary, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        order.status.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = KlinKlinTheme.Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ==================== PROFILE SCREEN ====================
@Composable
fun KlinProfileScreen(
    userName: String,
    userEmail: String,
    userPhone: String,
    userAddress: String,
    subscriptionPackage: String?,
    onLogout: () -> Unit,
    onOpenSubscription: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .padding(bottom = 120.dp)
    ) {
        Text(
            "Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = KlinKlinTheme.Foreground
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                ProfileInfoRow("Nama", userName)
                Spacer(modifier = Modifier.height(16.dp))
                ProfileInfoRow("Email", userEmail)
                Spacer(modifier = Modifier.height(16.dp))
                ProfileInfoRow("Telepon", userPhone)
                Spacer(modifier = Modifier.height(16.dp))
                ProfileInfoRow("Alamat", userAddress)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenSubscription() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (subscriptionPackage != null) KlinKlinTheme.Primary else Color.White
            )
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (subscriptionPackage != null) "Member $subscriptionPackage" else "Belum Berlangganan",
                        fontWeight = FontWeight.Bold,
                        color = if (subscriptionPackage != null) Color.White else KlinKlinTheme.Foreground
                    )
                    Text(
                        text = if (subscriptionPackage != null) "Aktif" else "Upgrade sekarang",
                        fontSize = 12.sp,
                        color = if (subscriptionPackage != null) Color.White.copy(0.7f) else KlinKlinTheme.MutedForeground
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = if (subscriptionPackage != null) Color.White else KlinKlinTheme.MutedForeground
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red.copy(alpha = 0.1f),
                contentColor = Color.Red
            )
        ) {
            Text("Keluar", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = KlinKlinTheme.MutedForeground)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, color = KlinKlinTheme.Foreground)
    }
}

// ==================== CHAT SCREEN (UPDATED WITH ChatMessage) ====================
@Composable
fun KlinChatScreen(
    chatViewModel: ChatViewModel,
    currentUserId: String
) {
    val messages by chatViewModel.messages
    var inputText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(messages.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KlinKlinTheme.Background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(KlinKlinTheme.Secondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        null,
                        tint = KlinKlinTheme.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Bersih.in Support",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = KlinKlinTheme.Foreground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF43A047), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Online",
                            fontSize = 11.sp,
                            color = Color(0xFF43A047)
                        )
                    }
                }
            }
        }

        // Messages area
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        null,
                        modifier = Modifier.size(56.dp),
                        tint = KlinKlinTheme.MutedForeground.copy(0.25f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Belum ada pesan",
                        color = KlinKlinTheme.MutedForeground,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Tanya apapun tentang laundry-mu!",
                        color = KlinKlinTheme.MutedForeground.copy(0.6f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                messages.forEach { msg ->
                    when (msg.type) {
                        "SYSTEM_REMINDER" -> SystemReminderBubble(msg)
                        else -> {
                            val isMe = msg.senderId == currentUserId
                            ChatBubble(msg = msg, isMe = isMe)
                        }
                    }
                }
            }
        }

        // Input area
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            color = White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Ketik pesan...",
                            color = KlinKlinTheme.MutedForeground.copy(0.5f),
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KlinKlinTheme.Primary,
                        unfocusedBorderColor = KlinKlinTheme.Secondary
                    ),
                    maxLines = 3,
                    singleLine = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (inputText.isNotBlank()) KlinKlinTheme.Primary
                            else KlinKlinTheme.Secondary,
                            CircleShape
                        )
                        .clickable {
                            if (inputText.isNotBlank()) {
                                chatViewModel.sendMessage(inputText.trim())
                                inputText = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        null,
                        tint = if (inputText.isNotBlank()) Color.White else KlinKlinTheme.MutedForeground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: com.example.klinklinapps.data.ChatMessage, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(KlinKlinTheme.Secondary, CircleShape)
                    .align(Alignment.Bottom),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SupportAgent,
                    null,
                    tint = KlinKlinTheme.Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 260.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isMe) 20.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 20.dp
                ),
                color = if (isMe) KlinKlinTheme.Primary else Color.White
            ) {
                Text(
                    text = msg.message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = if (isMe) Color.White else KlinKlinTheme.Foreground,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = msg.timestamp?.toDate()?.let {
                    java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                } ?: "",
                fontSize = 10.sp,
                color = KlinKlinTheme.MutedForeground
            )
        }
    }
}

@Composable
fun SystemReminderBubble(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = KlinKlinTheme.Secondary
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    null,
                    tint = KlinKlinTheme.Primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = msg.message,
                    fontSize = 12.sp,
                    color = KlinKlinTheme.Primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ==================== PROMO SCREEN (FULL) ====================
@Composable
fun KlinPromoScreen() {
    val promoList = listOf(
        Triple("Flash Sale Sepatu", "Diskon 40% cuci \nsepatu all type", "40%"),
        Triple("Member Baru", "Gratis ongkir 3x untuk pendaftar baru", "FREE"),
        Triple("Flash Sale Sepatu", "Diskon 40% cuci \nsepatu all type", "40%"),
        Triple("Flash Sale Sepatu", "Diskon 40% cuci \nsepatu all type", "40%"),
        Triple("Paket Hemat", "Cuci + setrika 5kg \nhanya Rp 35.000", "35K")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 32.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Promo & Penawaran",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = KlinKlinTheme.Foreground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Hemat lebih banyak dengan promo eksklusif",
                fontSize = 13.sp,
                color = KlinKlinTheme.MutedForeground
            )
        }
        items(promoList) { (title, desc, badge) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(KlinKlinTheme.Secondary, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badge,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = KlinKlinTheme.Primary,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KlinKlinTheme.Foreground)
                        Text(desc, fontSize = 12.sp, color = KlinKlinTheme.MutedForeground, lineHeight = 16.sp)
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = KlinKlinTheme.Primary
                    ) {
                        Text(
                            "Klaim",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==================== MAIN DASHBOARD SCREEN ====================
@Composable
fun DashboardScreen(
    userName: String,
    userEmail: String,
    userPhone: String,
    userAddress: String,
    isSubscribed: Boolean,
    subscriptionPackage: String?,
    balance: Int,
    hasActiveOrder: Boolean,
    ordersViewModel: OrdersViewModel,
    chatViewModel: ChatViewModel,
    currentUserId: String = "",
    onPlaceOrder: () -> Unit,
    onOpenSubscription: () -> Unit,
    onTopUp: () -> Unit,
    onOpenPlanner: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    val reminder by ordersViewModel.reminder

    val services = listOf(
        Service("Cuci Lipat", "Bersih & Harum", Icons.Default.LocalLaundryService, KlinKlinTheme.ServiceBg1),
        Service("Setrika", "Rapi & Licin", Icons.Default.Air, KlinKlinTheme.ServiceBg2, "DISKON"),
        Service("Dry Clean", "Perawatan Extra", Icons.Default.DryCleaning, KlinKlinTheme.ServiceBg3),
        Service("Express", "Kilat 6 Jam", Icons.Default.Speed, KlinKlinTheme.ServiceBg4, "HOT")
    )

    if (selectedOrder != null) {
        OrderDetailScreen(
            order = selectedOrder!!,
            onBack = { selectedOrder = null },
            onFinishOrder = {
                ordersViewModel.completeOrder(selectedOrder!!.id) { selectedOrder = null }
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KlinKlinTheme.Background)
        ) {
            when (selectedTab) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 120.dp)
                    ) {
                        Header(userName = userName)

                        if (hasActiveOrder || reminder != null) {
                            ActiveOrderReminder(
                                message = reminder?.message ?: "Pakaianmu siap diambil hari ini!",
                                onClick = onPlaceOrder
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        WalletCard(balance = balance, onTopUp = onTopUp)

                        PromoCarousel()

                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(
                                text = "Layanan Utama",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = KlinKlinTheme.Foreground
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ServiceCard(service = services[0], onClick = onPlaceOrder, modifier = Modifier.weight(1f))
                                ServiceCard(service = services[1], onClick = onPlaceOrder, modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ServiceCard(service = services[2], onClick = onPlaceOrder, modifier = Modifier.weight(1f))
                                ServiceCard(service = services[3], onClick = onPlaceOrder, modifier = Modifier.weight(1f))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        SubscriptionCard(onUpgrade = onOpenSubscription)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Smart Planner + Weather side by side feel → stacked with same padding
                        SmartPlannerCard(onOpen = onOpenPlanner)
                        Spacer(modifier = Modifier.height(16.dp))
                        WeatherAnalyticsCard()

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
                1 -> KlinPromoScreen()
                2 -> KlinOrdersScreen(ordersViewModel) { order -> selectedOrder = order }
                3 -> KlinChatScreen(
                    chatViewModel = chatViewModel,
                    currentUserId = currentUserId
                )
                4 -> KlinProfileScreen(
                    userName = userName,
                    userEmail = userEmail,
                    userPhone = userPhone,
                    userAddress = userAddress,
                    subscriptionPackage = if (isSubscribed) subscriptionPackage else null,
                    onLogout = onLogout,
                    onOpenSubscription = onOpenSubscription
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                BottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
    }
}