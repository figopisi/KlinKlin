package com.example.klinklinapps.ui

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klinklinapps.data.Order
import com.example.klinklinapps.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlin.math.abs

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
        topBar = {
            TopAppBar(
                title = { Text("Detail Pesanan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // MAP TRACKING REAL-TIME (Fitur Canggih)
            if (shouldShowTracking(order.status)) {
                TrackingMapSection(order)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = BrandBlueLight,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = BrandBlue)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Status Pesanan", fontSize = 12.sp, color = Gray500)
                            Text(
                                text = order.status.replace("_", " ").uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pricing Detail Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Rincian Biaya", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DetailRow(Icons.Default.Calculate, "Estimasi Awal", "Rp ${order.estimatedPrice}")
                    DetailRow(Icons.Default.SupportAgent, "Biaya Jasa & Add-ons", "Rp ${order.serviceFee}")
                    DetailRow(Icons.Default.LocalShipping, "Ongkos Kirim", "Rp ${order.deliveryFee}")
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Gray100)

                    DetailRow(
                        Icons.Default.Scale, 
                        "Subtotal Laundry (kg)", 
                        if (order.laundrySubtotal > 0) "Rp ${order.laundrySubtotal}" else "Rp 0"
                    )

                    DetailRow(
                        Icons.AutoMirrored.Filled.ReceiptLong, 
                        "Total Harga Akhir", 
                        "Rp $finalPrice",
                        valueColor = if (finalPrice > 0) BrandBlue else Gray800
                    )

                    DetailRow(
                        Icons.Default.Difference, 
                        "Selisih Saldo", 
                        if (finalPrice > 0) {
                            if (diff > 0) "+ Rp ${abs(diff)}" else if (diff < 0) "- Rp ${abs(diff)}" else "Rp 0"
                        } else "Rp 0",
                        valueColor = if (diff > 0) Color.Red else if (diff < 0) Color(0xFF2E7D32) else Gray800
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (finalPrice == 0L) {
                        Surface(
                            color = BrandBlueLight.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Note Penting:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlue
                                )
                                Text(
                                    "Harga estimasi adalah Rp ${order.estimatedPrice}. Nanti laundry akan menginput berat asli, dan harga final akan otomatis dibayar menggunakan saldo tokenmu (Escrow).",
                                    fontSize = 12.sp,
                                    color = BrandBlue,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    } else if (diff != 0L) {
                        Surface(
                            color = if (diff > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (diff > 0) {
                                    "Harga akhir lebih mahal Rp ${abs(diff)} karena berat pakaian asli. Saldo tokenmu sudah dipotong otomatis."
                                } else {
                                    "Harga akhir lebih murah! Saldo tokenmu sebesar Rp ${abs(diff)} sudah dikembalikan."
                                },
                                fontSize = 12.sp,
                                color = if (diff > 0) Color.Red else Color(0xFF2E7D32),
                                modifier = Modifier.padding(12.dp),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Driver Info
            if (order.driverPhone.isNotEmpty() || order.status.contains("DRIVER")) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = BrandBlueLight, modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = BrandBlue)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Driver Anda", fontSize = 12.sp, color = Gray500)
                            Text(order.driverName.ifEmpty { "Kurir KlinKlin" }, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { /* Chat logic */ }) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = BrandBlue)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            if (order.status == "PESANAN_DITERIMA") {
                Button(
                    onClick = onFinishOrder,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Selesaikan Pesanan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TrackingMapSection(order: Order) {
    val customerPos = LatLng(order.customerLat, order.customerLng)
    val laundryPos = LatLng(order.laundryLat, order.laundryLng)
    val driverPos = LatLng(order.driverLat, order.driverLng)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            if (order.driverLat != 0.0) driverPos else laundryPos, 
            15f
        )
    }

    // Auto-update camera if driver moves
    LaunchedEffect(order.driverLat, order.driverLng) {
        if (order.driverLat != 0.0) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLng(driverPos)
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            // Marker User (Rumah)
            Marker(
                state = MarkerState(position = customerPos),
                title = "Lokasi Saya",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )

            // Marker Laundry (Toko)
            Marker(
                state = MarkerState(position = laundryPos),
                title = "Lokasi Laundry",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
            )

            // Marker Driver (Real-time Tracking)
            if (order.driverLat != 0.0) {
                Marker(
                    state = MarkerState(position = driverPos),
                    title = "Driver: ${order.driverName}",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
            }
        }
    }
}

fun shouldShowTracking(status: String): Boolean {
    return status in listOf(
        "MENUNGGU_PICKUP",
        "DRIVER_MENJEMPUT",
        "DI_LAUNDRY",
        "DRIVER_MENGANTAR",
        "DIPROSES"
    )
}

@Composable
fun DetailRow(
    icon: ImageVector, 
    label: String, 
    value: String,
    valueColor: Color = Gray800
) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Gray500, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), color = Gray500, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, color = valueColor, fontSize = 14.sp)
    }
}
