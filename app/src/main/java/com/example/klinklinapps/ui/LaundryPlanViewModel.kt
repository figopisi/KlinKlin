package com.example.klinklinapps.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.klinklinapps.data.LaundryPlan
import com.example.klinklinapps.data.LaundryPlanRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class LaundryPlanViewModel : ViewModel() {
    private val repository = LaundryPlanRepository()
    private val auth = Firebase.auth

    private val _plans = mutableStateOf<List<LaundryPlan>>(emptyList())
    val plans: State<List<LaundryPlan>> = _plans

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        loadPlans()
    }

    fun loadPlans() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _plans.value = repository.getLaundryPlans(userId)
            _isLoading.value = false
        }
    }

    fun addPlan(eventName: String, eventDate: Long, serviceType: String, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.saveLaundryPlan(userId, eventName, eventDate, serviceType)
            if (result.isSuccess) {
                loadPlans()
                onSuccess()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Gagal menyimpan rencana"
            }
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
