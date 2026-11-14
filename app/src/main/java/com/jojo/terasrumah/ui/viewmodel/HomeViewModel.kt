package com.jojo.terasrumah.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jojo.terasrumah.data.FoodCategory
import com.jojo.terasrumah.data.FoodItem
import com.jojo.terasrumah.data.FoodRepository
import com.jojo.terasrumah.data.api.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val foodRepository: FoodRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()
    
    init {
        loadAllFoodItems()
    }
    
    fun loadAllFoodItems() {
        viewModelScope.launch {
            foodRepository.getAllFoodItems().collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is ApiResult.Success -> {
                        _foodItems.value = result.data
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Unknown error occurred"
                        )
                    }
                }
            }
        }
    }

    fun forceRefreshFoodItems() {
        viewModelScope.launch {
            foodRepository.forceRefreshAllFoodItems().collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is ApiResult.Success -> {
                        _foodItems.value = result.data
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Unknown error occurred"
                        )
                    }
                }
            }
        }
    }
    
    fun loadFoodItemsByCategory(category: FoodCategory) {
        viewModelScope.launch {
            foodRepository.getFoodItemsByCategory(category).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is ApiResult.Success -> {
                        _foodItems.value = result.data
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Unknown error occurred"
                        )
                    }
                }
            }
        }
    }
    
    fun refreshData() {
        viewModelScope.launch {
            // Clear cache to force fresh data from API
            foodRepository.clearCache()
            loadAllFoodItems()
        }
    }

    fun forceRefreshWithClearCache() {
        viewModelScope.launch {
            // Use the new force refresh function that clears cache automatically
            forceRefreshFoodItems()
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModelFactory(private val foodRepository: FoodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(foodRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
