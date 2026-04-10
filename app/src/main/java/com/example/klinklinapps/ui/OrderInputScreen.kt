package com.example.klinklinapps.ui

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.klinklinapps.ui.theme.*
import java.io.File

// TEMA WARNA KLINKLIN - Light Blue Theme (Match Dashboard)
private val KlinBackground = Color(0xFFF0F7FF)
private val KlinPrimary = Color(0xFF5B9BD5)
private val KlinSecondary = Color(0xFFE3F2FD)
private val KlinAccent = Color(0xFF42A5F5)
private val KlinTextMain = Color(0xFF1A2332)
private val KlinTextSub = Color(0xFF64748B)
private val KlinSuccess = Color(0xFF66BB6A)

// DEFINISI DATA CLASS
data class KlinServiceItemData(
    val name: String,
    val desc: String,
    val icon: ImageVector,
    val color: Color,
    val badge: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderInputScreen(
    userName: String,
    userPhone: String,
    userAddress: String,
    isSubscribed: Boolean,
    ordersViewModel: OrdersViewModel,
    onBack: () -> Unit,
    onConfirmOrder: () -> Unit
) {
    val isProcessing by ordersViewModel.isProcessing
    val errorMessage by ordersViewModel.errorMessage
    val context = LocalContext.current

    var selectedService by remember { mutableStateOf("Wash & Fold") }
    var weight by remember { mutableFloatStateOf(1.0f) }
    var specialNotes by remember { mutableStateOf("") }
    var scentBooster by remember { mutableStateOf(false) }
    var insurance by remember { mutableStateOf(false) }
    var express by remember { mutableStateOf(false) }
    var hasImage by remember { mutableStateOf(false) }

    var imageUriState by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success -> hasImage = success }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "temp_image.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            imageUriState = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    val baseRate = if (selectedService == "Wash & Fold") 10000 else 15000
    val subtotal = (weight * baseRate).toLong()
    val serviceFee = (if (isSubscribed) 0 else 2000).toLong()
    val deliveryFee = (if (isSubscribed) 0 else 5000).toLong()
    val addOnTotal = (if (scentBooster) 5000 else 0) + (if (insurance) 2000 else 0) + (if (express) 10000 else 0)
    val totalPrice = subtotal + serviceFee + deliveryFee + addOnTotal

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            ordersViewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(KlinBackground)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Buat Pesanan",
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
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 12.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(24.dp)
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Total Pembayaran",
                                fontSize = 12.sp,
                                color = KlinTextSub,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Rp ${totalPrice.toString().reversed().chunked(3).joinToString(".").reversed()}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = KlinPrimary
                            )
                        }
                        Button(
                            onClick = {
                                ordersViewModel.placeOrder(
                                    service = selectedService,
                                    estimatedPrice = totalPrice,
                                    serviceFee = serviceFee + addOnTotal,
                                    deliveryFee = deliveryFee,
                                    userName = userName,
                                    userPhone = userPhone,
                                    userAddress = userAddress,
                                    onSuccess = onConfirmOrder
                                )
                            },
                            modifier = Modifier
                                .height(60.dp)
                                .width(150.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    spotColor = KlinPrimary.copy(alpha = 0.3f)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KlinPrimary
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Pesan",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // Info Section - Modern Card
                OrderInputCard {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(48.dp)
                                .background(KlinSecondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                tint = KlinPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                userName,
                                fontWeight = FontWeight.Black,
                                color = KlinTextMain,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                userAddress,
                                fontSize = 12.sp,
                                color = KlinTextSub,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            Modifier
                                .size(32.dp)
                                .background(KlinSecondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                null,
                                tint = KlinPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Camera Card - Enhanced
                OrderInputCard(
                    modifier = Modifier.clickable {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        if (hasImage) Color(0xFFE8F5E9) else KlinSecondary,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (hasImage) Icons.Default.CheckCircle else Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = if (hasImage) KlinSuccess else KlinPrimary
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (hasImage) "Foto Cucian Berhasil!" else "Ambil Foto Cucian",
                                color = if (hasImage) KlinSuccess else KlinTextMain,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (hasImage) "Tap untuk ubah foto" else "Opsional",
                                color = KlinTextSub,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Service Selection
                OrderSectionTitle("Pilih Layanan")
                OrderServiceRow(
                    selectedService = selectedService,
                    onServiceSelected = { selectedService = it }
                )

                Spacer(Modifier.height(24.dp))

                // Weight Stepper
                if (selectedService == "Wash & Fold") {
                    OrderSectionTitle("Estimasi Berat Cucian")
                    OrderWeightStepper(
                        weight = weight,
                        onWeightChange = { weight = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = KlinSecondary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = KlinPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Estimasi ini akan diverifikasi saat penjemputan",
                                fontSize = 11.sp,
                                color = KlinPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Add-ons
                OrderSectionTitle("Layanan Tambahan")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OrderAddonChip(
                        selected = scentBooster,
                        icon = Icons.Default.LocalFlorist,
                        label = "Scent Booster",
                        price = "+5k"
                    ) { scentBooster = !scentBooster }

                    OrderAddonChip(
                        selected = insurance,
                        icon = Icons.Default.Shield,
                        label = "Asuransi Cucian",
                        price = "+2k"
                    ) { insurance = !insurance }

                    OrderAddonChip(
                        selected = express,
                        icon = Icons.Default.Bolt,
                        label = "Express 6 Jam",
                        price = "+10k"
                    ) { express = !express }
                }

                Spacer(Modifier.height(24.dp))

                // Notes Section
                OrderSectionTitle("Catatan Khusus")
                OutlinedTextField(
                    value = specialNotes,
                    onValueChange = { specialNotes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Contoh: Hati-hati bahan sutra, pisahkan pakaian putih...",
                            fontSize = 13.sp,
                            color = KlinTextSub
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = KlinPrimary,
                        unfocusedBorderColor = KlinSecondary
                    ),
                    minLines = 3,
                    maxLines = 5
                )

                // Price Breakdown
                Spacer(modifier = Modifier.height(24.dp))
                OrderSectionTitle("Rincian Harga")
                OrderInputCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        PriceRow("Subtotal Cucian", "Rp $subtotal")
                        if (serviceFee > 0) {
                            PriceRow("Biaya Layanan", "Rp $serviceFee")
                        }
                        if (deliveryFee > 0) {
                            PriceRow("Biaya Antar", "Rp $deliveryFee")
                        }
                        if (addOnTotal > 0) {
                            PriceRow("Layanan Tambahan", "Rp $addOnTotal")
                        }

                        if (isSubscribed) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = KlinSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Hemat Rp 7.000 dengan KlinKlin Plus+",
                                        fontSize = 11.sp,
                                        color = KlinSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Loading Overlay
        if (isProcessing) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = KlinPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Memproses pesanan...",
                            fontWeight = FontWeight.Bold,
                            color = KlinTextMain
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderInputCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = KlinPrimary.copy(alpha = 0.08f),
                spotColor = KlinPrimary.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        content = content
    )
}

@Composable
fun OrderSectionTitle(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(KlinPrimary, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = KlinTextMain,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
fun OrderServiceRow(selectedService: String, onServiceSelected: (String) -> Unit) {
    val services = listOf(
        KlinServiceItemData(
            "Wash & Fold",
            "Cuci Lipat",
            Icons.Default.LocalLaundryService,
            KlinSecondary
        ),
        KlinServiceItemData(
            "Dry Cleaning",
            "Cuci Kering",
            Icons.Default.DryCleaning,
            Color(0xFFE1F5FE)
        ),
        KlinServiceItemData(
            "Ironing",
            "Setrika Saja",
            Icons.Default.Iron,
            Color(0xFFE0F2F1)
        )
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        services.forEach { s ->
            val isSel = selectedService == s.name
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onServiceSelected(s.name) }
                    .border(
                        width = if (isSel) 3.dp else 0.dp,
                        color = KlinPrimary,
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = if (isSel) KlinSecondary else Color.White,
                shadowElevation = if (isSel) 0.dp else 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (isSel) Color.White else KlinSecondary,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            s.icon,
                            null,
                            tint = KlinPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        s.desc,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) KlinPrimary else KlinTextMain,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun OrderWeightStepper(weight: Float, onWeightChange: (Float) -> Unit) {
    OrderInputCard {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { if (weight > 0.5f) onWeightChange(weight - 0.5f) },
                modifier = Modifier
                    .size(48.dp)
                    .background(KlinSecondary, CircleShape)
            ) {
                Icon(Icons.Default.Remove, null, tint = KlinPrimary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${weight}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = KlinPrimary
                )
                Text(
                    "Kilogram",
                    fontSize = 12.sp,
                    color = KlinTextSub,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(
                onClick = { onWeightChange(weight + 0.5f) },
                modifier = Modifier
                    .size(48.dp)
                    .background(KlinPrimary, CircleShape)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun OrderAddonChip(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    price: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) KlinPrimary else Color.White,
        border = if (!selected)
            androidx.compose.foundation.BorderStroke(2.dp, KlinSecondary)
        else null,
        shadowElevation = if (selected) 4.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (selected) Color.White.copy(alpha = 0.2f) else KlinSecondary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else KlinPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                label,
                color = if (selected) Color.White else KlinTextMain,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                price,
                color = if (selected) Color.White else KlinPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun PriceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = KlinTextSub,
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            fontSize = 14.sp,
            color = KlinTextMain,
            fontWeight = FontWeight.Bold
        )
    }
}
