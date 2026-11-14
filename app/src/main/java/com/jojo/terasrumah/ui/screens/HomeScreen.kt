package com.jojo.terasrumah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.jojo.terasrumah.data.CartRepository
import com.jojo.terasrumah.data.FoodCategory
import com.jojo.terasrumah.data.FoodItem
import com.jojo.terasrumah.data.FoodRepository
import com.jojo.terasrumah.ui.components.MenuCard
import com.jojo.terasrumah.ui.components.EnhancedCategoryChip
import com.jojo.terasrumah.ui.theme.PrimaryBrown
import com.jojo.terasrumah.ui.viewmodel.AuthViewModel
import com.jojo.terasrumah.ui.viewmodel.AuthViewModelFactory
import com.jojo.terasrumah.ui.viewmodel.HomeViewModel
import com.jojo.terasrumah.ui.viewmodel.HomeViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onFoodItemClick: (FoodItem) -> Unit = {},
    onCartClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(LocalContext.current)),
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(FoodRepository.getInstance(LocalContext.current)))
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf(FoodCategory.ALL) }
    var searchText by remember { mutableStateOf("") }

    // 🔄 PULL-TO-REFRESH STATE
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    val foodItems by homeViewModel.foodItems.collectAsState()
    val uiState by homeViewModel.uiState.collectAsState()
    val cartCount by CartRepository.cartCount

    // Force refresh on first load to ensure fresh data
    LaunchedEffect(Unit) {
        homeViewModel.forceRefreshFoodItems()
    }

    // Stop refreshing when loading is complete
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && isRefreshing) {
            isRefreshing = false
        }
    }

    // Load food items when category changes
    LaunchedEffect(selectedCategory) {
        if (selectedCategory == FoodCategory.ALL) {
            homeViewModel.forceRefreshFoodItems() // Use force refresh instead of regular load
        } else {
            homeViewModel.loadFoodItemsByCategory(selectedCategory)
        }
    }

    // Filter food items based on search text
    val filteredFoodItems = remember(foodItems, searchText) {
        if (searchText.isBlank()) {
            foodItems
        } else {
            foodItems.filter { foodItem ->
                foodItem.name.contains(searchText, ignoreCase = true) ||
                foodItem.description.contains(searchText, ignoreCase = true)
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Teras Rumah Kayu",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    // Cart Icon with Badge
                    Box {
                        IconButton(onClick = onCartClick) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = Color.White
                            )
                        }
                        if (cartCount > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-8).dp, y = 8.dp),
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = cartCount.toString(),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Profile Icon
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBrown
                )
            )
        }
    ) { paddingValues ->
        // 🔄 SWIPE REFRESH WRAPPER
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                isRefreshing = true
                homeViewModel.forceRefreshWithClearCache()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = {
                        Text(
                            text = "Cari makanan favorit...",
                            color = Color.Gray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBrown,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )

                // Category Tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 20.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(FoodCategory.values()) { category ->
                        EnhancedCategoryChip(
                            category = category,
                            isSelected = category == selectedCategory,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
                
                // Loading, Error, or Food Items Grid
                when {
                    uiState.isLoading && !isRefreshing -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryBrown)
                        }
                    }
                    uiState.errorMessage != null -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Terjadi kesalahan: ${uiState.errorMessage}",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { homeViewModel.refreshData() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrown)
                            ) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(filteredFoodItems) { foodItem ->
                                MenuCard(
                                    foodItem = foodItem,
                                    onAddToCart = {
                                        CartRepository.addToCart(foodItem)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "${foodItem.name} ditambahkan ke keranjang",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    },
                                    onClick = { onFoodItemClick(foodItem) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: FoodCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = category.displayName,
                color = if (isSelected) Color.White else PrimaryBrown,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        selected = isSelected,
        enabled = true,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimaryBrown,
            containerColor = Color.White,
            selectedLabelColor = Color.White,
            labelColor = PrimaryBrown
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = PrimaryBrown,
            selectedBorderColor = PrimaryBrown,
            enabled = true,
            selected = isSelected
        )
    )
}
