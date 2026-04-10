package com.example.klinklinapps.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.R
import com.example.klinklinapps.data.Order
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlin.math.abs

// TEMA WARNA KLINKLIN - Light Blue Theme (Match Dashboard)
private val KlinBackground = Color(0xFFF0F7FF)
private val KlinPrimary = Color(0xFF5B9BD5)
private val KlinSecondary = Color(0xFFE3F2FD)
private val KlinAccent = Color(0xFF42A5F5)
private val KlinTextMain = Color(0xFF1A2332)
private val KlinTextSub = Color(0xFF64748B)
private val KlinSuccess = Color(0xFF66BB6A)
private val KlinWarning = Color(0xFFFFB74D)
private val KlinError = Color(0xFFEF5350)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    order: Order,
    onBack: () -> Unit,
    onFinishOrder: () -> Unit
) {
    val finalPrice = if (order.totalPrice > 0) order.totalPrice
    else if (order.laundrySubtotal > 0) (order.laundrySubtotal + order.serviceFee + order.deliveryFee)
    else 0L

    val diff = if (finalPrice > 0) (finalPrice - order.estimatedPrice) else 0L

    Scaffold(
        containerColor = KlinBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Detail Pesanan",
                        fontWeight = FontWeight.Black,
                        color = KlinTextMain,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(KlinSecondary, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = KlinPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(2.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // FITUR MAP TRACKING
            if (detailShouldShowTracking(order.status)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .shadow(8.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    DetailTrackingMap(order)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // STATUS CARD - Modern Design
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = KlinPrimary.copy(alpha = 0.1f),
                        spotColor = KlinPrimary.copy(alpha = 0.1f)
                    ),
                color = Color.White,
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(KlinSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = KlinPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Status Saat Ini",
                            fontSize = 11.sp,
                            color = KlinTextSub,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = order.status.replace("_", " "),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = KlinPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PRICING DETAIL CARD - Enhanced Design
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = KlinPrimary.copy(alpha = 0.08f),
                        spotColor = KlinPrimary.copy(alpha = 0.08f)
                    ),
                color = Color.White,
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(KlinSecondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = KlinPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Rincian Biaya",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = KlinTextMain
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    DetailPriceItem(
                        Icons.Default.Calculate,
                        "Estimasi Awal",
                        "Rp ${order.estimatedPrice}"
                    )
                    DetailPriceItem(
                        Icons.AutoMirrored.Filled.PlaylistAddCheck,
                        "Biaya Jasa & Add-ons",
                        "Rp ${order.serviceFee}"
                    )
                    DetailPriceItem(
                        Icons.Default.LocalShipping,
                        "Ongkos Kirim",
                        "Rp ${order.deliveryFee}"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = KlinBackground,
                        thickness = 2.dp
                    )

                    DetailPriceItem(
                        Icons.Default.Scale,
                        "Subtotal Laundry (kg)",
                        if (order.laundrySubtotal > 0) "Rp ${order.laundrySubtotal}" else "Rp 0",
                        valueWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Total Akhir - Highlighted
                    Surface(
                        color = KlinSecondary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                null,
                                tint = KlinPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Total Akhir",
                                modifier = Modifier.weight(1f),
                                color = KlinTextMain,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Rp $finalPrice",
                                fontWeight = FontWeight.Black,
                                color = KlinPrimary,
                                fontSize = 18.sp
                            )
                        }
                    }

                    if (finalPrice > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val color = if (diff > 0) KlinError else if (diff < 0) KlinSuccess else KlinTextSub
                        val bgColor = if (diff > 0) Color(0xFFFFEBEE) else if (diff < 0) Color(0xFFE8F5E9) else KlinSecondary

                        Surface(
                            color = bgColor,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Difference,
                                    null,
                                    tint = color,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Selisih Saldo",
                                    modifier = Modifier.weight(1f),
                                    color = color,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    if (diff > 0) "+ Rp ${abs(diff)}"
                                    else if (diff < 0) "- Rp ${abs(diff)}"
                                    else "Pas",
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info Note
                    val noteBg = if (finalPrice == 0L) KlinSecondary
                    else if (diff > 0) Color(0xFFFFEBEE)
                    else Color(0xFFE8F5E9)

                    val noteColor = if (finalPrice == 0L) KlinPrimary
                    else if (diff > 0) KlinError
                    else KlinSuccess

                    Surface(
                        color = noteBg,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp)) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = noteColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (finalPrice == 0L) {
                                    "Nanti mitra akan input berat asli, harga final otomatis dibayar via Escrow."
                                } else if (diff > 0) {
                                    "Harga akhir naik Rp ${abs(diff)} karena berat baju asli. Saldo terpotong otomatis."
                                } else if (diff < 0) {
                                    "Harga akhir turun! Saldo Rp ${abs(diff)} sudah dikembalikan ke KlinPay."
                                } else "Pembayaran pas sesuai estimasi.",
                                fontSize = 11.sp,
                                color = noteColor,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DRIVER INFO - Modern Card
            if (order.driverPhone.isNotEmpty() || order.status.contains("DRIVER")) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = KlinPrimary.copy(alpha = 0.08f)
                        ),
                    color = Color.White,
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(KlinSecondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Driver Kurir",
                                fontSize = 11.sp,
                                color = KlinTextSub,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                order.driverName.ifEmpty { "Mitra KlinKlin" },
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = KlinTextMain
                            )
                        }
                        IconButton(
                            onClick = { /* Chat */ },
                            modifier = Modifier
                                .size(44.dp)
                                .background(KlinSecondary, CircleShape)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Chat",
                                tint = KlinPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // FINISH BUTTON
            if (order.status == "PESANAN_DITERIMA") {
                Button(
                    onClick = onFinishOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = KlinPrimary.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KlinPrimary
                    )
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Konfirmasi Selesai",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun DetailTrackingMap(order: Order) {
    val customerPos = LatLng(order.customerLat, order.customerLng)
    val laundryPos = LatLng(order.laundryLat, order.laundryLng)
    val driverPos = LatLng(order.driverLat, order.driverLng)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            if (order.driverLat != 0.0) driverPos else laundryPos,
            14f
        )
    }

    LaunchedEffect(order.driverLat, order.driverLng) {
        if (order.driverLat != 0.0) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(driverPos))
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(zoomControlsEnabled = false)
    ) {
        Marker(
            state = MarkerState(position = customerPos),
            title = "Rumah",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        )
        Marker(
            state = MarkerState(position = laundryPos),
            title = "Outlet",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
        )
        if (order.driverLat != 0.0) {
            Marker(
                state = MarkerState(position = driverPos),
                title = "Driver",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            )
        }
    }
}

fun detailShouldShowTracking(status: String): Boolean =
    status in listOf(
        "MENUNGGU_PICKUP",
        "DRIVER_MENJEMPUT",
        "DI_LAUNDRY",
        "DRIVER_MENGANTAR",
        "DIPROSES"
    )

@Composable
fun DetailPriceItem(
    icon: ImageVector,
    label: String,
    value: String,
    vColor: Color = KlinTextMain,
    valueWeight: FontWeight = FontWeight.Bold
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = KlinTextSub,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            modifier = Modifier.weight(1f),
            color = KlinTextSub,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            fontWeight = valueWeight,
            color = vColor,
            fontSize = 14.sp
        )
    }
}