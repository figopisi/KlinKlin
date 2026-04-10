package com.example.klinklinapps.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.ui.theme.*
import kotlinx.coroutines.delay

data class LaundryShop(
    val id: String,
    val name: String,
    val rating: Double,
    val distance: String,
    val isRecommended: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaundrySelectionScreen(onBack: () -> Unit, onShopSelected: (LaundryShop) -> Unit) {
    val shops = listOf(
        LaundryShop("1", "KlinKlin Express", 4.9, "0.5 km", isRecommended = true),
        LaundryShop("2", "Laundry Spongebob", 4.7, "1.2 km"),
        LaundryShop("3", "Clean & Fresh Laundry", 4.5, "2.0 km"),
        LaundryShop("4", "Mutiara Laundry", 4.8, "0.8 km"),
        LaundryShop("5", "Bubble Pop Laundry", 4.6, "1.5 km")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pilih Laundry", fontWeight = FontWeight.Black, color = BrandBlue) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrandBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Gray100),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Recommendation Option — index 0, delay 0ms
            item {
                AnimatedListItem(index = 0) {
                    RecommendationItem(onClick = { onShopSelected(shops[0]) })
                }
            }

            item {
                AnimatedListItem(index = 1) {
                    Text(
                        "Laundry Terdekat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Gray800,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            itemsIndexed(shops) { index, shop ->
                AnimatedListItem(index = index + 2) {
                    LaundryShopCard(shop = shop, onClick = { onShopSelected(shop) })
                }
            }
        }
    }
}

// ── Staggered entrance: fade + slide up per item ──
@Composable
fun AnimatedListItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 70L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(
            animationSpec = tween(320, easing = EaseOutCubic),
            initialOffsetY = { it / 3 }
        )
    ) {
        content()
    }
}

@Composable
fun RecommendationItem(onClick: () -> Unit) {
    // Press scale animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "recoScale"
    )

    // Infinite pulse on the icon ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandBlue)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing outer ring + icon
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .scale(pulseScale)
                        .background(White.copy(alpha = 0.12f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = SunYellow)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "Rekomendasi Klinklin",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = White
                )
                Text(
                    "Klinklin pilihkan laundry terbaik untukmu",
                    fontSize = 12.sp,
                    color = White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun LaundryShopCard(shop: LaundryShop, onClick: () -> Unit) {
    // Press scale animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "shopScale"
    )

    // Animated star rating color blink on first appear
    val starAlpha by rememberInfiniteTransition(label = "star").animateFloat(
        initialValue = 1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(12.dp),
                color = BrandBlueLight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = BrandBlue)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(shop.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Gray800)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = SunYellow.copy(alpha = if (shop.isRecommended) starAlpha else 1f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(" ${shop.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gray800)
                    Text(" • ${shop.distance}", fontSize = 12.sp, color = Gray500)
                }
            }
            if (shop.isRecommended) {
                // Subtle scale pulse on TOP badge
                val badgePulse by rememberInfiniteTransition(label = "badge").animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(700, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "badgePulse"
                )
                Surface(
                    color = BrandBlueLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.scale(badgePulse)
                ) {
                    Text(
                        "TOP",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = BrandBlue
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LaundrySelectionPreview() {
    KlinKlinAppsTheme {
        LaundrySelectionScreen(onBack = {}, onShopSelected = {})
    }
}