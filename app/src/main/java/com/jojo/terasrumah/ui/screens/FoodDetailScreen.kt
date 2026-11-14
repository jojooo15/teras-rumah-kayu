package com.jojo.terasrumah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jojo.terasrumah.data.CartRepository
import com.jojo.terasrumah.data.FoodItem
import com.jojo.terasrumah.data.api.ApiResult
import com.jojo.terasrumah.data.model.*
import com.jojo.terasrumah.data.repository.ReviewRepository
import com.jojo.terasrumah.data.repository.AuthRepository
import com.jojo.terasrumah.ui.theme.PrimaryBrown
import com.jojo.terasrumah.ui.viewmodel.AuthViewModel
import com.jojo.terasrumah.ui.viewmodel.AuthViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private fun formatPrice(price: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(price).replace("IDR", "Rp")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    foodItem: FoodItem,
    onBackClick: () -> Unit,
    onAddToCart: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {}
) {
    var quantity by remember { mutableStateOf(1) }
    var specialNotes by remember { mutableStateOf("") }
    var showAddedToCart by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Review related state
    val context = LocalContext.current
    val reviewRepository = remember { ReviewRepository.getInstance(context) }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)

    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var reviewStats by remember { mutableStateOf<ReviewStats?>(null) }
    var isLoadingReviews by remember { mutableStateOf(false) }
    var showWriteReview by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableStateOf(0) }
    var reviewComment by remember { mutableStateOf("") }
    var isSubmittingReview by remember { mutableStateOf(false) }

    // Load reviews when screen opens
    LaunchedEffect(foodItem.id) {
        reviewRepository.getReviewsByFoodItem(foodItem.id, limit = 5).collect { result ->
            when (result) {
                is ApiResult.Loading -> {
                    isLoadingReviews = true
                }
                is ApiResult.Success -> {
                    reviews = result.data.reviews
                    reviewStats = result.data.stats
                    isLoadingReviews = false
                }
                is ApiResult.Error -> {
                    isLoadingReviews = false
                }
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Fixed bottom button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quantity selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- }
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = PrimaryBrown
                            )
                        }
                        
                        Text(
                            text = quantity.toString(),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(
                            onClick = { quantity++ }
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = PrimaryBrown
                            )
                        }
                    }
                    
                    // Add to cart button
                    Button(
                        onClick = {
                            repeat(quantity) {
                                CartRepository.addToCart(foodItem, specialNotes)
                            }
                            showAddedToCart = true
                            // Show snackbar notification
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "${quantity}x ${foodItem.name} ditambahkan ke keranjang",
                                    duration = SnackbarDuration.Short
                                )
                            }
                            onAddToCart()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBrown
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Tambah - ${formatPrice(foodItem.price * quantity)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Makanan",
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
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Food Image
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(foodItem.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = foodItem.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Food Name
                Text(
                    text = foodItem.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Rating and Delivery Time
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = " ${foodItem.rating}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = "⏱ ${foodItem.deliveryTime}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                Text(
                    text = "Deskripsi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = foodItem.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
                
                // Ingredients Section
                if (foodItem.ingredients.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Bahan-bahan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Ingredients chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(foodItem.ingredients) { ingredient ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = PrimaryBrown.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = ingredient,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    color = PrimaryBrown
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category
                Text(
                    text = "Kategori: ${foodItem.category.displayName}",
                    fontSize = 14.sp,
                    color = PrimaryBrown,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Special Notes Input Section
                Text(
                    text = "Catatan Khusus (Opsional)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = specialNotes,
                    onValueChange = { specialNotes = it },
                    placeholder = { Text("Contoh: Tidak pedas, tanpa bawang, dll") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBrown,
                        focusedLabelColor = PrimaryBrown,
                        cursorColor = PrimaryBrown
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatPrice(foodItem.price),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Reviews Section
                ReviewSection(
                    reviews = reviews,
                    reviewStats = reviewStats,
                    isLoading = isLoadingReviews,
                    currentUser = currentUser,
                    onWriteReview = { showWriteReview = true },
                    onNavigateToReviews = onNavigateToReviews
                )

                Spacer(modifier = Modifier.height(100.dp)) // Space for bottom button
            }
        }

        // Write Review Dialog
        if (showWriteReview) {
            WriteReviewDialog(
                foodItemName = foodItem.name,
                rating = selectedRating,
                comment = reviewComment,
                isSubmitting = isSubmittingReview,
                onRatingChange = { selectedRating = it },
                onCommentChange = { reviewComment = it },
                onDismiss = {
                    showWriteReview = false
                    selectedRating = 0
                    reviewComment = ""
                },
                onSubmit = {
                    val user = currentUser
                    println("DEBUG: onSubmit called - selectedRating: $selectedRating, reviewComment: '$reviewComment', user: $user")

                    if (selectedRating > 0 && reviewComment.isNotBlank()) {
                        // If user is not logged in, show login prompt
                        if (user == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Silakan login terlebih dahulu untuk memberikan review",
                                    duration = SnackbarDuration.Long
                                )
                            }
                            return@WriteReviewDialog
                        }

                        val request = ReviewRequest(
                            userId = user.id,
                            foodItemId = foodItem.id,
                            rating = selectedRating,
                            comment = reviewComment.trim(),
                            userName = user.name,
                            userPhone = user.phoneNumber
                        )
                        isSubmittingReview = true
                        scope.launch {
                            reviewRepository.createReview(request).collect { result ->
                                when (result) {
                                    is ApiResult.Loading -> {
                                        // Keep loading state
                                    }
                                    is ApiResult.Success -> {
                                        isSubmittingReview = false
                                        showWriteReview = false
                                        selectedRating = 0
                                        reviewComment = ""

                                        // Add the new review to the current list immediately for real-time update
                                        val newReview = result.data
                                        reviews = listOf(newReview) + reviews

                                        // Update review stats
                                        val currentStats = reviewStats ?: ReviewStats()
                                        val newTotalReviews = currentStats.totalReviews + 1
                                        val newAverageRating = if (currentStats.totalReviews == 0) {
                                            newReview.rating.toDouble()
                                        } else {
                                            ((currentStats.averageRating * currentStats.totalReviews) + newReview.rating) / newTotalReviews
                                        }

                                        reviewStats = currentStats.copy(
                                            averageRating = newAverageRating,
                                            totalReviews = newTotalReviews
                                        )

                                        // Show success message with points earned
                                        snackbarHostState.showSnackbar(
                                            message = "Review berhasil ditambahkan! Anda mendapat ${newReview.pointsAwarded} poin.",
                                            duration = SnackbarDuration.Long
                                        )

                                        // Update user points in local storage
                                        println("DEBUG: About to update user points for user: ${user.id}, points: ${newReview.pointsAwarded}")
                                        authViewModel.updateUserPointsAndReviews(user.id, newReview.pointsAwarded)

                                        // Small delay to ensure the update is processed
                                        kotlinx.coroutines.delay(100)

                                        // Refresh user data to update UI
                                        println("DEBUG: About to refresh current user")
                                        authViewModel.refreshCurrentUser()

                                        // Additional delay to ensure refresh is processed
                                        kotlinx.coroutines.delay(100)

                                        // Refresh reviews from repository to ensure consistency
                                        reviewRepository.getReviewsByFoodItem(foodItem.id, limit = 5).collect { refreshResult ->
                                            if (refreshResult is ApiResult.Success) {
                                                reviews = refreshResult.data.reviews
                                                reviewStats = refreshResult.data.stats
                                            }
                                        }
                                    }
                                    is ApiResult.Error -> {
                                        isSubmittingReview = false
                                        snackbarHostState.showSnackbar(
                                            message = result.exception.message ?: "Gagal menambahkan review",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ReviewSection(
    reviews: List<Review>,
    reviewStats: ReviewStats?,
    isLoading: Boolean,
    currentUser: User?,
    onWriteReview: () -> Unit,
    onNavigateToReviews: () -> Unit = {}
) {
    Column {
        // Reviews Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Review Pelanggan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Write Review Button - Only show for logged in users who haven't reviewed
            if (currentUser != null && !reviews.any { it.userId == currentUser.id }) {
                OutlinedButton(
                    onClick = onWriteReview,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryBrown
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PrimaryBrown)
                    )
                ) {
                    Icon(
                        Icons.Default.RateReview,
                        contentDescription = "Tulis Review",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tulis Review", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Review Stats
        reviewStats?.let { stats ->
            if (stats.totalReviews > 0) {
                ReviewStatsCard(stats = stats)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Reviews List
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryBrown,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else if (reviews.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada review untuk makanan ini.\nJadilah yang pertama memberikan review!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reviews.take(3)) { review ->
                    ReviewCard(review = review)
                }

                if (reviews.size > 3) {
                    item {
                        TextButton(
                            onClick = onNavigateToReviews,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = PrimaryBrown
                            )
                        ) {
                            Text("Lihat semua review (${reviews.size})")
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // User info and rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = review.userName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatReviewDate(review.createdAt),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Rating stars
                Row {
                    repeat(5) { index ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < review.rating) Color(0xFFFFD700) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Review comment
            Text(
                text = review.comment,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            // Points earned indicator
            if (review.pointsAwarded > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+${review.pointsAwarded} poin",
                        fontSize = 12.sp,
                        color = PrimaryBrown,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun formatReviewDate(date: Date): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return formatter.format(date)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewDialog(
    foodItemName: String,
    rating: Int,
    comment: String,
    isSubmitting: Boolean,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tulis Review",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Review untuk: $foodItemName",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rating Section
                Text(
                    text = "Rating",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { onRatingChange(index + 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Rating ${index + 1}",
                                tint = if (index < rating) Color(0xFFFFD700) else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Comment Section
                Text(
                    text = "Komentar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    placeholder = { Text("Bagikan pengalaman Anda...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBrown,
                        cursorColor = PrimaryBrown
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Points info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Dapatkan 10 poin untuk setiap review!",
                        fontSize = 12.sp,
                        color = PrimaryBrown,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    println("DEBUG: Button clicked - rating: $rating, comment: '$comment', isSubmitting: $isSubmitting")
                    onSubmit()
                },
                enabled = rating > 0 && comment.isNotBlank() && !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBrown
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                } else {
                    Text("Kirim Review")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
