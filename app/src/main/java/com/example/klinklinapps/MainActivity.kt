package com.example.klinklinapps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.klinklinapps.ui.*
import com.example.klinklinapps.ui.theme.KlinKlinAppsTheme

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val ordersViewModel: OrdersViewModel by viewModels()
    private val laundryPlanViewModel: LaundryPlanViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Izin notifikasi ditolak. Anda tidak akan menerima pengingat laundry.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askNotificationPermission()

        setContent {
            KlinKlinAppsTheme {
                val context = LocalContext.current
                val currentUser by authViewModel.currentUser
                val userRole by authViewModel.userRole
                val isLoading by authViewModel.isLoading
                val balance by authViewModel.balance

                val userName = remember(currentUser) {
                    val displayName = currentUser?.displayName
                    if (!displayName.isNullOrBlank()) displayName
                    else currentUser?.email?.substringBefore("@") ?: "User"
                }

                val userEmail = currentUser?.email ?: ""
                // Pass UID so ChatScreen knows which messages are "mine"
                val currentUserId = currentUser?.uid ?: ""

                var currentScreen by remember {
                    mutableStateOf(if (currentUser != null) "dashboard" else "welcome")
                }

                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        currentScreen = "welcome"
                    } else {
                        ordersViewModel.listenToOrders()
                        laundryPlanViewModel.loadPlans()
                        chatViewModel.listenToMessages()
                    }
                }

                var isSubscribed by remember { mutableStateOf(false) }
                var subscriptionPackage by remember { mutableStateOf<String?>(null) }
                var hasActiveOrder by remember { mutableStateOf(false) }
                var selectedTopUpMethod by remember { mutableStateOf<TopUpMethod?>(null) }

                val userPhone = "08123456789"
                val userAddress = "Jl. Sudirman No. 123, Denpasar, Bali"

                when (currentScreen) {
                    "welcome" -> WelcomeScreen(
                        onLoginNavigate = { currentScreen = "login" },
                        onRegisterNavigate = { currentScreen = "register" }
                    )
                    "login" -> LoginScreen(
                        viewModel = authViewModel,
                        onBack = { currentScreen = "welcome" },
                        onLoginSuccess = { currentScreen = "dashboard" }
                    )
                    "register" -> RegisterScreen(
                        viewModel = authViewModel,
                        onBack = { currentScreen = "welcome" },
                        onRegisterSuccess = { currentScreen = "dashboard" }
                    )
                    "dashboard" -> {
                        if (isLoading && userRole == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            when (userRole) {
                                "driver" -> DriverDashboardScreen(onLogout = { authViewModel.logout() })
                                "laundry" -> LaundryDashboardScreen(onLogout = { authViewModel.logout() })
                                "admin" -> AdminDashboardScreen(onLogout = { authViewModel.logout() })
                                else -> DashboardScreen(
                                    userName = userName,
                                    userEmail = userEmail,
                                    userPhone = userPhone,
                                    userAddress = userAddress,
                                    isSubscribed = isSubscribed,
                                    subscriptionPackage = subscriptionPackage,
                                    balance = balance.toInt(),
                                    hasActiveOrder = hasActiveOrder,
                                    ordersViewModel = ordersViewModel,
                                    chatViewModel = chatViewModel,
                                    currentUserId = currentUserId,        // ← BARU
                                    onPlaceOrder = { currentScreen = "laundry_selection" },
                                    onOpenSubscription = { currentScreen = "subscription" },
                                    onTopUp = { currentScreen = "top_up" },
                                    onOpenPlanner = { currentScreen = "laundry_planner" },
                                    onLogout = { authViewModel.logout() }
                                )
                            }
                        }
                    }
                    "laundry_planner" -> LaundryPlannerScreen(
                        viewModel = laundryPlanViewModel,
                        onBack = { currentScreen = "dashboard" }
                    )
                    "laundry_selection" -> LaundrySelectionScreen(
                        onBack = { currentScreen = "dashboard" },
                        onShopSelected = { currentScreen = "order_input" }
                    )
                    "order_input" -> OrderInputScreen(
                        userName = userName,
                        userPhone = userPhone,
                        userAddress = userAddress,
                        isSubscribed = isSubscribed,
                        ordersViewModel = ordersViewModel,
                        onBack = { currentScreen = "laundry_selection" },
                        onConfirmOrder = { currentScreen = "order_processing" }
                    )
                    "order_processing" -> OrderProcessingScreen(
                        onFinish = {
                            hasActiveOrder = true
                            currentScreen = "dashboard"
                        }
                    )
                    "subscription" -> SubscriptionScreen(
                        onBack = { currentScreen = "dashboard" },
                        onSubscribeSuccess = {
                            isSubscribed = true
                            subscriptionPackage = "Bulanan Plus+"
                            currentScreen = "dashboard"
                        }
                    )
                    "top_up" -> TopUpScreen(
                        onBack = { currentScreen = "dashboard" },
                        onMethodSelected = { method ->
                            selectedTopUpMethod = method
                            currentScreen = "top_up_amount"
                        }
                    )
                    "top_up_amount" -> {
                        selectedTopUpMethod?.let { method ->
                            TopUpAmountScreen(
                                method = method,
                                onBack = { currentScreen = "top_up" },
                                onConfirm = { amount ->
                                    authViewModel.topUp(amount.toLong()) {
                                        Toast.makeText(context, "Top Up Berhasil!", Toast.LENGTH_LONG).show()
                                        currentScreen = "dashboard"
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}