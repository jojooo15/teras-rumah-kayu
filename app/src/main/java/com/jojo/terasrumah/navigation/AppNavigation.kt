package com.jojo.terasrumah.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jojo.terasrumah.data.FoodItem
import com.jojo.terasrumah.data.FoodRepository
import com.jojo.terasrumah.data.api.ApiResult
import com.jojo.terasrumah.data.model.*
import com.jojo.terasrumah.utils.CheckoutUtils
import com.jojo.terasrumah.ui.screens.AddressScreen
import com.jojo.terasrumah.ui.screens.AddEditAddressScreen
import com.jojo.terasrumah.ui.screens.EditAddressScreenWrapper
import com.jojo.terasrumah.ui.screens.CartScreen
import com.jojo.terasrumah.ui.screens.CheckoutScreen
import com.jojo.terasrumah.ui.screens.FoodDetailScreen
import com.jojo.terasrumah.ui.screens.HomeScreen
import com.jojo.terasrumah.ui.screens.LoginScreen
import com.jojo.terasrumah.ui.theme.PrimaryBrown
import com.jojo.terasrumah.ui.screens.ProfileScreen
import com.jojo.terasrumah.ui.screens.RegisterScreen
import com.jojo.terasrumah.ui.screens.ReviewScreen
import com.jojo.terasrumah.ui.screens.ReviewHistoryScreen
import com.jojo.terasrumah.ui.screens.SplashScreen
import com.jojo.terasrumah.ui.viewmodel.AuthViewModel
import com.jojo.terasrumah.ui.viewmodel.AuthViewModelFactory

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    try {
                        // Simplified navigation - always go to login first
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        // If navigation fails, try again
                        navController.navigate("login")
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                authViewModel = authViewModel
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                authViewModel = authViewModel
            )
        }
        composable("home") {
            HomeScreen(
                onFoodItemClick = { foodItem ->
                    navController.navigate("food_detail/${foodItem.id}")
                },
                onCartClick = {
                    navController.navigate("cart")
                },
                onProfileClick = {
                    navController.navigate("profile")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable("profile") {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToAddress = {
                    navController.navigate("address")
                },
                onNavigateToReviewHistory = {
                    navController.navigate("review_history")
                },
                authViewModel = authViewModel
            )
        }

        composable("cart") {
            CartScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCheckout = { _ ->
                    // Navigate to checkout screen instead of directly processing
                    navController.navigate("checkout")
                },
                onLoginRequired = {
                    navController.navigate("login")
                },
                authViewModel = authViewModel
            )
        }

        composable("checkout") {
            val currentUser by authViewModel.currentUser.collectAsState(initial = null)

            CheckoutScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onOrderComplete = { paymentMethod: String ->
                    // Get current user and address info
                    val defaultAddress = currentUser?.getDefaultAddress()

                    val customerName = currentUser?.name ?: "Customer"
                    val deliveryAddress = defaultAddress?.let { address ->
                        "${customerName}\n${address.getFormattedAddress()}\n${if (address.notes.isNotBlank()) "Catatan: ${address.notes}" else ""}"
                    }
                    val customerPhone = defaultAddress?.phoneNumber ?: currentUser?.phoneNumber

                    when (paymentMethod) {
                        "transfer" -> {
                            CheckoutUtils.openWhatsAppForTransfer(context, deliveryAddress, customerPhone)
                        }
                        "cod" -> {
                            CheckoutUtils.generateCODReceipt(context, deliveryAddress, customerPhone, customerName)
                        }
                    }
                    // Navigate back to home after checkout
                    navController.popBackStack("home", inclusive = false)
                },
                authViewModel = authViewModel
            )
        }

        composable("address") {
            AddressScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAddAddressClick = {
                    navController.navigate("add_address")
                },
                onEditAddressClick = { address ->
                    navController.navigate("edit_address/${address.id}")
                }
            )
        }

        composable("add_address") {
            AddEditAddressScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable("edit_address/{addressId}") { backStackEntry ->
            val addressId = backStackEntry.arguments?.getString("addressId")
            EditAddressScreenWrapper(
                addressId = addressId,
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable("food_detail/{foodId}") { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId")?.toIntOrNull()

            if (foodId != null) {
                FoodDetailScreenWrapper(
                    foodId = foodId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAddToCart = {
                        // Optional: Show a snackbar or navigate to cart
                    },
                    onNavigateToReviews = { foodItem ->
                        navController.navigate("reviews/${foodItem.id}")
                    }
                )
            }
        }

        composable("reviews/{foodId}") { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId")?.toIntOrNull()
            val currentUser by authViewModel.currentUser.collectAsState(initial = null)

            if (foodId != null) {
                ReviewScreenWrapper(
                    foodId = foodId,
                    currentUserId = currentUser?.id ?: "",
                    currentUserName = currentUser?.name ?: "",
                    currentUserPhone = currentUser?.phoneNumber ?: "",
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("review_history") {
            val currentUser by authViewModel.currentUser.collectAsState(initial = null)

            // For now, show empty review history - can be implemented later
            // This is a placeholder screen that shows user's review history
            ReviewHistoryScreen(
                userId = currentUser?.id ?: "",
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun FoodDetailScreenWrapper(
    foodId: Int,
    onBackClick: () -> Unit,
    onAddToCart: () -> Unit,
    onNavigateToReviews: (FoodItem) -> Unit = {}
) {
    var foodItem by remember { mutableStateOf<FoodItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    LaunchedEffect(foodId) {
        FoodRepository.getInstance(context).getFoodItemById(foodId).collect { result ->
            when (result) {
                is ApiResult.Loading -> {
                    isLoading = true
                    errorMessage = null
                }
                is ApiResult.Success -> {
                    foodItem = result.data
                    isLoading = false
                    errorMessage = null
                }
                is ApiResult.Error -> {
                    isLoading = false
                    errorMessage = result.exception.message
                }
            }
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBrown)
            }
        }
        errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        foodItem != null -> {
            FoodDetailScreen(
                foodItem = foodItem!!,
                onBackClick = onBackClick,
                onAddToCart = onAddToCart,
                onNavigateToReviews = { onNavigateToReviews(foodItem!!) }
            )
        }
    }
}

@Composable
fun ReviewScreenWrapper(
    foodId: Int,
    currentUserId: String,
    currentUserName: String,
    currentUserPhone: String,
    onBackClick: () -> Unit
) {
    var foodItem by remember { mutableStateOf<FoodItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    LaunchedEffect(foodId) {
        FoodRepository.getInstance(context).getFoodItemById(foodId).collect { result ->
            when (result) {
                is ApiResult.Loading -> {
                    isLoading = true
                    errorMessage = null
                }
                is ApiResult.Success -> {
                    foodItem = result.data
                    isLoading = false
                    errorMessage = null
                }
                is ApiResult.Error -> {
                    isLoading = false
                    errorMessage = result.exception.message
                }
            }
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBrown)
            }
        }
        errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        foodItem != null -> {
            ReviewScreen(
                foodItem = foodItem!!,
                currentUserId = currentUserId,
                currentUserName = currentUserName,
                currentUserPhone = currentUserPhone,
                onBackClick = onBackClick
            )
        }
    }
}
