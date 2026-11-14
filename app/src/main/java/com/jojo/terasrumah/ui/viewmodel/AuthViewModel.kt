package com.jojo.terasrumah.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jojo.terasrumah.data.model.User
import com.jojo.terasrumah.data.model.UserAddress
import com.jojo.terasrumah.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Manual StateFlow for currentUser to ensure UI updates
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val isLoggedIn = authRepository.isLoggedIn

    init {
        // Initialize currentUser from repository and keep it synced
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                println("DEBUG: AuthViewModel - Repository user changed: ${user?.name}, points: ${user?.points}")
                _currentUser.value = user
            }
        }
    }
    
    fun login(phoneNumber: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            authRepository.login(phoneNumber, rememberMe)
                .onSuccess { response ->
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            successMessage = response.message
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = response.message
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Terjadi kesalahan"
                    )
                }
        }
    }
    
    fun register(phoneNumber: String, name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            authRepository.register(phoneNumber, name)
                .onSuccess { response ->
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isRegistrationSuccessful = true,
                            successMessage = response.message
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = response.message
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Terjadi kesalahan"
                    )
                }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState() // Reset state
        }
    }

    fun refreshCurrentUser() {
        viewModelScope.launch {
            try {
                authRepository.refreshCurrentUser()
            } catch (e: Exception) {
                // Handle error silently for refresh
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null,
            isLoginSuccessful = false,
            isRegistrationSuccessful = false
        )
    }

    suspend fun isSessionValid(): Boolean {
        return authRepository.isSessionValid()
    }

    fun updateUserAddresses(userId: String, addresses: List<UserAddress>) {
        viewModelScope.launch {
            authRepository.updateUserAddresses(userId, addresses)
        }
    }

    fun updateUserPointsAndReviews(userId: String, pointsToAdd: Int) {
        viewModelScope.launch {
            println("DEBUG: AuthViewModel - updateUserPointsAndReviews called")

            // Update in repository
            authRepository.updateUserPointsAndReviews(userId, pointsToAdd)

            // Force immediate UI update by manually updating StateFlow
            val currentUserValue = _currentUser.value
            if (currentUserValue != null && currentUserValue.id == userId) {
                val newPoints = currentUserValue.points + pointsToAdd
                val newTotalReviews = currentUserValue.totalReviews + 1
                val newUserLevel = when {
                    newPoints >= 1000 -> "GOLD"
                    newPoints >= 500 -> "SILVER"
                    newPoints >= 100 -> "BRONZE"
                    else -> "BASIC"
                }

                val updatedUser = currentUserValue.copy(
                    points = newPoints,
                    totalReviews = newTotalReviews,
                    userLevel = newUserLevel
                )

                println("DEBUG: AuthViewModel - Force updating StateFlow with new points: $newPoints")
                _currentUser.value = updatedUser
            }

            // Also refresh from repository to ensure consistency
            delay(100)
            authRepository.refreshCurrentUser()
        }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoginSuccessful: Boolean = false,
    val isRegistrationSuccessful: Boolean = false
)

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(AuthRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
