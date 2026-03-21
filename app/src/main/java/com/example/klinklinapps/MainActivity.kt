package com.example.klinklinapps

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.klinklinapps.ui.*
import com.example.klinklinapps.ui.theme.KlinKlinAppsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KlinKlinAppsTheme {
                val context = LocalContext.current
                var currentScreen by remember { mutableStateOf("welcome") }
                var isSubscribed by remember { mutableStateOf(false) }
                var balance by remember { mutableIntStateOf(150000) }
                var hasActiveOrder by remember { mutableStateOf(false) }
                var selectedTopUpMethod by remember { mutableStateOf<TopUpMethod?>(null) }

                when (currentScreen) {
                    "welcome" -> WelcomeScreen(
                        onLoginNavigate = { currentScreen = "login" },
                        onRegisterNavigate = { }
                    )
                    "login" -> LoginScreen(
                        onBack = { currentScreen = "welcome" },
                        onLoginSuccess = { currentScreen = "dashboard" }
                    )
                    "dashboard" -> DashboardScreen(
                        balance = balance,
                        hasActiveOrder = hasActiveOrder,
                        onPlaceOrder = { currentScreen = "laundry_selection" },
                        onOpenSubscription = { currentScreen = "subscription" },
                        onTopUp = { currentScreen = "top_up" },
                        onLogout = { currentScreen = "welcome" }
                    )
                    "laundry_selection" -> LaundrySelectionScreen(
                        onBack = { currentScreen = "dashboard" },
                        onShopSelected = { currentScreen = "order_input" }
                    )
                    "order_input" -> OrderInputScreen(
                        isSubscribed = isSubscribed,
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
                                    balance += amount
                                    Toast.makeText(context, "Top Up Berhasil! Saldo bertambah Rp $amount", Toast.LENGTH_LONG).show()
                                    currentScreen = "dashboard"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
