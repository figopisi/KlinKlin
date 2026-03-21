package com.example.klinklinapps.ui

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.klinklinapps.ui.theme.*
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderInputScreen(isSubscribed: Boolean, onBack: () -> Unit, onConfirmOrder: () -> Unit) {
    var selectedService by remember { mutableStateOf("Wash & Fold") }
    var weight by remember { mutableFloatStateOf(1.0f) }
    var specialNotes by remember { mutableStateOf("") }
    
    // Add-ons states
    var scentBooster by remember { mutableStateOf(false) }
    var insurance by remember { mutableStateOf(false) }
    var express by remember { mutableStateOf(false) }

    // Photo logic
    val context = LocalContext.current
    var hasImage by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            hasImage = success
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "temp_image.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            imageUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Dynamic Pricing Logic
    val baseRate = if (selectedService == "Wash & Fold") 10000 else 15000
    val subtotal = (weight * baseRate).toInt()
    val serviceFee = if (isSubscribed) 0 else 2000
    val deliveryFee = if (isSubscribed) 0 else 5000 
    val addOnTotal = (if (scentBooster) 5000 else 0) + (if (insurance) 2000 else 0) + (if (express) 10000 else 0)
    val totalPrice = subtotal + serviceFee + deliveryFee + addOnTotal

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Place New Order", fontWeight = FontWeight.Black, color = BrandBlue) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrandBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            PriceSummaryBar(totalPrice = totalPrice, onConfirm = onConfirmOrder)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Gray100)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            if (isSubscribed) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = SunYellow.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SunYellow)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = SunYellow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Klinklin Plus+ Aktif: Biaya Layanan & Ongkir Rp0!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                    }
                }
            }

            // Profile Section
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rita Mutiara", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Gray800)
                    Text("+62123456789", fontSize = 14.sp, color = Gray500)
                    Text("Jalan Merdeka No. 1, Denpasar, Bali", fontSize = 14.sp, color = Gray500)
                }
            }

            // Photo Section with Camera Access
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BrandBlueLight)
                    .clickable { 
                        val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                            val file = File(context.cacheDir, "temp_image.jpg")
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            imageUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (hasImage) Icons.Default.CheckCircle else Icons.Default.PhotoCamera, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp), 
                        tint = if (hasImage) Color(0xFF4CAF50) else BrandBlue
                    )
                    Text(
                        if (hasImage) "Foto Berhasil Diambil!" else "Ambil Foto Cucianmu!", 
                        fontWeight = FontWeight.Bold, 
                        color = Gray800
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Service Selection
            SectionTitle("Pilih Layanan")
            ServiceSelectionRow(
                selectedService = selectedService,
                onServiceSelected = { selectedService = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic Quantity / Weight
            if (selectedService == "Wash & Fold") {
                SectionTitle("Berat Cucian (Estimasi)")
                WeightStepper(weight = weight, onWeightChange = { weight = it })
            } else {
                SectionTitle("Daftar Item")
                ItemQuantityList()
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Add-ons Section (Upselling)
            SectionTitle("Layanan Tambahan (Add-ons)")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = scentBooster,
                    onClick = { scentBooster = !scentBooster },
                    label = { Text("Scent +5k") }
                )
                FilterChip(
                    selected = insurance,
                    onClick = { insurance = !insurance },
                    label = { Text("Insure +2k") }
                )
                FilterChip(
                    selected = express,
                    onClick = { express = !express },
                    label = { Text("Express +10k") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pricing Details Section
            SectionTitle("Rincian Biaya")
            PricingDetailsCard(subtotal, serviceFee, deliveryFee, addOnTotal)

            Spacer(modifier = Modifier.height(24.dp))

            // Special Notes
            SectionTitle("Instruksi Khusus")
            OutlinedTextField(
                value = specialNotes,
                onValueChange = { if (it.length <= 200) specialNotes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("Contoh: noda tinta di kerah...", fontSize = 14.sp) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = White,
                    focusedContainerColor = White,
                    focusedBorderColor = BrandBlue
                )
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun PricingDetailsCard(subtotal: Int, serviceFee: Int, deliveryFee: Int, addOns: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PriceRow("Subtotal", "Rp $subtotal")
            PriceRow("Service Fee", "Rp $serviceFee")
            PriceRow("Delivery Fee", "Rp $deliveryFee")
            if (addOns > 0) PriceRow("Add-ons", "Rp $addOns")
        }
    }
}

@Composable
fun PriceRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = Gray500)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (value == "Rp 0") BrandBlue else Gray800)
    }
}

@Composable
fun PriceSummaryBar(totalPrice: Int, onConfirm: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Total Pembayaran", fontSize = 12.sp, color = Gray500)
                Text("Rp $totalPrice", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BrandBlue)
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Pesan", color = White, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Gray800,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun ServiceSelectionRow(selectedService: String, onServiceSelected: (String) -> Unit) {
    val services = listOf(
        ServiceItemData("Wash & Fold", Icons.Default.LocalLaundryService, BrandBlueLight),
        ServiceItemData("Dry Cleaning", Icons.Default.DryCleaning, SunYellow.copy(alpha = 0.2f)),
        ServiceItemData("Ironing", Icons.Default.Iron, BrandBlueLight)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        services.forEach { service ->
            val isSelected = selectedService == service.name
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onServiceSelected(service.name) }
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) BrandBlue else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) White else Gray200.copy(alpha = 0.5f),
                shadowElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        service.icon,
                        contentDescription = null,
                        tint = if (isSelected) BrandBlue else Gray500,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        service.name,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) BrandBlue else Gray800,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun WeightStepper(weight: Float, onWeightChange: (Float) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { if (weight > 0.5f) onWeightChange(weight - 0.5f) },
                modifier = Modifier.background(BrandBlueLight, CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = BrandBlue)
            }
            
            Text(
                text = "${weight} kg",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 32.dp),
                color = BrandBlue
            )

            IconButton(
                onClick = { onWeightChange(weight + 0.5f) },
                modifier = Modifier.background(BrandBlueLight, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = BrandBlue)
            }
        }
    }
}

@Composable
fun ItemQuantityList() {
    val items = listOf("Jas", "Selimut", "Karpet", "Kebaya")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = White
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Gray500)
                        Text("0", modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = BrandBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleSection() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ScheduleBox(title = "Pickup", date = "22 Feb", time = "09:00 - 10:00", icon = Icons.Default.HomeRepairService, modifier = Modifier.weight(1f))
        ScheduleBox(title = "Delivery", date = "24 Feb", time = "15:00 - 16:00", icon = Icons.Default.LocalShipping, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ScheduleBox(title: String, date: String, time: String, icon: ImageVector, modifier: Modifier) {
    Surface(
        modifier = modifier.clickable { },
        shape = RoundedCornerShape(16.dp),
        color = White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gray500)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(date, fontWeight = FontWeight.Black, fontSize = 16.sp, color = BrandBlue)
            Text(time, fontSize = 11.sp, color = Gray800)
        }
    }
}

@Composable
fun AddressSelector(address: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { },
        shape = RoundedCornerShape(16.dp),
        color = White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Alamat Penjemputan", fontSize = 12.sp, color = Gray500, fontWeight = FontWeight.Bold)
                Text(address, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray800)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Gray500)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrderInputPreview() {
    KlinKlinAppsTheme {
        OrderInputScreen(isSubscribed = false, onBack = {}, onConfirmOrder = {})
    }
}
