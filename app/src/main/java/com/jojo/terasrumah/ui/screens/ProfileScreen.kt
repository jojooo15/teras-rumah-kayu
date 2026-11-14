package com.jojo.terasrumah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jojo.terasrumah.data.api.ApiResult
import com.jojo.terasrumah.data.model.*
import com.jojo.terasrumah.data.repository.ReviewRepository
import com.jojo.terasrumah.ui.theme.BackgroundCream
import com.jojo.terasrumah.ui.theme.PrimaryBrown
import com.jojo.terasrumah.ui.viewmodel.AuthViewModel
import com.jojo.terasrumah.ui.viewmodel.AuthViewModelFactory
import com.jojo.terasrumah.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToReviewHistory: () -> Unit = {},
    onNavigateToAddress: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(LocalContext.current))
) {
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)

    // Debug logging for currentUser changes
    LaunchedEffect(currentUser) {
        println("DEBUG: ProfileScreen - currentUser changed!")
        println("DEBUG: ProfileScreen - User: ${currentUser?.name}")
        println("DEBUG: ProfileScreen - Points: ${currentUser?.points}")
        println("DEBUG: ProfileScreen - Reviews: ${currentUser?.totalReviews}")
        println("DEBUG: ProfileScreen - Level: ${currentUser?.userLevel}")
    }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Refresh user data when screen is displayed
    LaunchedEffect(Unit) {
        println("DEBUG: ProfileScreen - Initial refresh")
        authViewModel.refreshCurrentUser()
        // Set loading to false after a timeout to prevent infinite loading
        kotlinx.coroutines.delay(3000)
        isLoading = false
    }



    // Refresh user data when returning to profile (to get updated addresses)
    LaunchedEffect(currentUser) {
        // Force refresh when user data changes
        val user = currentUser
        if (user != null) {
            println("DEBUG: ProfileScreen - User data updated, addresses count: ${user.addresses.size}")
            println("DEBUG: ProfileScreen - Default address ID: ${user.defaultAddressId}")
            val defaultAddr = user.getDefaultAddress()
            println("DEBUG: ProfileScreen - Default address: ${defaultAddr?.label}")
        }
    }

    // Update loading state when user data changes
    LaunchedEffect(currentUser, isLoggedIn) {
        if (currentUser != null || !isLoggedIn) {
            isLoading = false
        }
    }

    // Refresh user data when screen appears to ensure points are up to date
    LaunchedEffect(Unit) {
        println("DEBUG: ProfileScreen - LaunchedEffect triggered, refreshing user data")
        authViewModel.refreshCurrentUser()
    }

    // Get user reviews and calculate earned points
    val context = LocalContext.current
    val reviewRepository = remember { ReviewRepository.getInstance(context) }
    var userReviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoadingReviews by remember { mutableStateOf(false) }

    // Load user reviews to calculate earned points
    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { userId ->
            reviewRepository.getUserReviews(userId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        isLoadingReviews = true
                    }
                    is ApiResult.Success -> {
                        userReviews = result.data
                        isLoadingReviews = false
                    }
                    is ApiResult.Error -> {
                        isLoadingReviews = false
                    }
                }
            }
        }
    }

    // Calculate earned points from reviews
    val earnedPoints by remember(userReviews) {
        derivedStateOf {
            userReviews.sumOf { it.pointsAwarded }
        }
    }

    // Removed voucher system - using points only
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {
        // Header
        TopAppBar(
            title = {
                Text(
                    text = "Profil Saya",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PrimaryBrown
            )
        )

        // Content
        currentUser?.let { user ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // User Points Section with Review Button
                    UserPointsSection(
                        earnedPoints = earnedPoints,
                        totalReviews = userReviews.size,
                        isLoadingReviews = isLoadingReviews,
                        onNavigateToReviewHistory = onNavigateToReviewHistory
                    )
                }

                // Removed voucher section - using points only
                // Removed review history section - accessible via Review button

                item {
                    // Profile Info Cards
                    ProfileInfoCard(
                        user = user,
                        onNavigateToAddress = onNavigateToAddress
                    )
                }

                item {
                    // Logout Button
                    LogoutButton(onClick = { showLogoutDialog = true })
                }
            }
        } ?: run {
            // Handle loading state and not logged in state
            if (isLoading) {
                // Loading state when user data is not available
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryBrown,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memuat profil...",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                // Not logged in or failed to load user data
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tidak dapat memuat profil",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Silakan login ulang",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(
                            onClick = {
                                authViewModel.logout()
                                onLogout()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryBrown
                            )
                        ) {
                            Text("Login Ulang")
                        }
                    }
                }
            }
        }
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Konfirmasi Logout",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Apakah Anda yakin ingin keluar dari aplikasi?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        onLogout()
                    }
                ) {
                    Text(
                        text = "Ya, Keluar",
                        color = Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        text = "Batal",
                        color = PrimaryBrown
                    )
                }
            }
        )
    }
}

@Composable
private fun ProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBrown,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun ProfileInfoCard(
    user: User,
    onNavigateToAddress: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Informasi Profil",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ProfileInfoItem(
                icon = Icons.Default.Person,
                label = "Nama",
                value = user.name
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileInfoItem(
                icon = Icons.Default.Phone,
                label = "Nomor Telepon",
                value = user.phoneNumber
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Address Section - Always show, with default or empty state
            val defaultAddress = user.getDefaultAddress()

            if (defaultAddress != null) {
                // Full Address with Edit Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        ProfileInfoItem(
                            icon = Icons.Default.LocationOn,
                            label = "Alamat Lengkap",
                            value = defaultAddress.fullAddress
                        )
                    }

                    IconButton(onClick = onNavigateToAddress) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Alamat",
                            tint = PrimaryBrown,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // City (simplified address structure)
                if (defaultAddress.city.isNotBlank()) {
                    ProfileInfoItem(
                        icon = Icons.Default.LocationCity,
                        label = "Kota",
                        value = defaultAddress.city
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Delivery Notes
                if (defaultAddress.notes.isNotBlank()) {
                    ProfileInfoItem(
                        icon = Icons.Default.Note,
                        label = "Catatan Pengiriman",
                        value = defaultAddress.notes
                    )
                }
            } else {
                // No address available with Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alamat",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Belum ada alamat tersimpan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }

                    IconButton(onClick = onNavigateToAddress) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Tambah Alamat",
                            tint = PrimaryBrown,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserPointsSection(
    earnedPoints: Int,
    totalReviews: Int,
    isLoadingReviews: Boolean,
    onNavigateToReviewHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Text(
                text = "Poin Saya",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoadingReviews) {
                // Loading state
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = PrimaryBrown
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Memuat poin...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            } else {
                // Earned Points Display
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "$earnedPoints Poin",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBrown
                        )
                        Text(
                            text = "Dari $totalReviews review",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Points Rewards Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF8F00),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tukar Poin:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF8F00)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 50 poin = Es Teh Manis\n• 100 poin = Tahu Goreng\n• 150 poin = Tempe Mendoan\n• 200 poin = Nasi Gudeg Mini",
                        fontSize = 11.sp,
                        color = Color(0xFF6D4C41),
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Review Button
            Button(
                onClick = onNavigateToReviewHistory,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBrown
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Review",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

        }
    }
}

// Removed VouchersSection - using points only

// Removed VoucherCard - using points only

@Composable
private fun ReviewButton(
    onNavigateToReviewHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Button(
            onClick = onNavigateToReviewHistory,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBrown
            )
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Review",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ReviewHistorySection(
    userReviews: List<Review>,
    isLoadingReviews: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Riwayat Review",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoadingReviews) {
                // Loading state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = PrimaryBrown
                    )
                }
            } else if (userReviews.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.RateReview,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum ada review",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            } else {
                // Show recent reviews (max 3)
                val recentReviews = userReviews.take(3)

                recentReviews.forEach { review ->
                    ReviewHistoryItem(review = review)
                    if (review != recentReviews.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                if (userReviews.size > 3) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Dan ${userReviews.size - 3} review lainnya...",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewHistoryItem(
    review: Review
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Rating stars
                Row {
                    repeat(5) { index ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < review.rating) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Comment
                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 2
                )
            }

            // Points earned
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "+${review.pointsAwarded}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
                Text(
                    text = "poin",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.Red
        )
    ) {
        Icon(
            Icons.Default.ExitToApp,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Keluar",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
