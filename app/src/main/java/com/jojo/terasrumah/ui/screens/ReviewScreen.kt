package com.jojo.terasrumah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jojo.terasrumah.data.FoodItem
import com.jojo.terasrumah.data.model.Review
import com.jojo.terasrumah.data.model.ReviewRequest
import com.jojo.terasrumah.data.model.ReviewStats
import com.jojo.terasrumah.data.model.getTimeAgo
import com.jojo.terasrumah.ui.theme.PrimaryBrown
import com.jojo.terasrumah.ui.components.WriteReviewDialog
import com.jojo.terasrumah.ui.components.LoadMoreButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    foodItem: FoodItem,
    reviews: List<Review> = emptyList(),
    reviewStats: ReviewStats? = null,
    currentUserId: String = "",
    currentUserName: String = "",
    currentUserPhone: String = "",
    onBackClick: () -> Unit = {},
    onSubmitReview: (ReviewRequest) -> Unit = {},
    onLoadMoreReviews: () -> Unit = {}
) {
    var showWriteReview by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableStateOf(0) }
    var reviewComment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    
    // Check if user already reviewed this item
    val userHasReviewed = reviews.any { it.userId == currentUserId }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Review & Rating") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PrimaryBrown,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Food Item Info
            item {
                FoodItemReviewCard(foodItem = foodItem)
            }
            
            // Review Stats
            reviewStats?.let { stats ->
                item {
                    ReviewStatsCard(stats = stats)
                }
            }
            
            // Write Review Button
            if (!userHasReviewed && currentUserId.isNotEmpty()) {
                item {
                    WriteReviewButton(
                        onClick = { showWriteReview = true }
                    )
                }
            }
            
            // Reviews List
            item {
                Text(
                    text = "Review Pelanggan (${reviews.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (reviews.isEmpty()) {
                item {
                    EmptyReviewsCard()
                }
            } else {
                items(reviews) { review ->
                    ReviewItemCard(review = review)
                }
                
                // Load More Button
                if (reviews.size >= 10) {
                    item {
                        LoadMoreButton(onClick = onLoadMoreReviews)
                    }
                }
            }
        }
    }
    
    // Write Review Dialog
    if (showWriteReview) {
        WriteReviewDialog(
            foodItemName = foodItem.name,
            rating = selectedRating,
            comment = reviewComment,
            isSubmitting = isSubmitting,
            onRatingChange = { selectedRating = it },
            onCommentChange = { reviewComment = it },
            onDismiss = { 
                showWriteReview = false
                selectedRating = 0
                reviewComment = ""
            },
            onSubmit = {
                if (selectedRating > 0 && reviewComment.isNotBlank()) {
                    isSubmitting = true
                    val request = ReviewRequest(
                        userId = currentUserId,
                        foodItemId = foodItem.id,
                        rating = selectedRating,
                        comment = reviewComment.trim(),
                        userName = currentUserName,
                        userPhone = currentUserPhone
                    )
                    onSubmitReview(request)
                    showWriteReview = false
                    selectedRating = 0
                    reviewComment = ""
                    isSubmitting = false
                }
            }
        )
    }
}

@Composable
fun FoodItemReviewCard(foodItem: FoodItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Image Placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryBrown.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = PrimaryBrown,
                    modifier = Modifier.size(30.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = foodItem.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
                Text(
                    text = "Rp ${String.format("%,d", foodItem.price)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                // Current Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${foodItem.rating}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewStatsCard(stats: ReviewStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = String.format("%.1f", stats.averageRating),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown
                    )
                    
                    Row {
                        repeat(5) { index ->
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < stats.averageRating.toInt()) 
                                    Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.3f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = "${stats.totalReviews} review",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Rating Distribution
                Column(
                    modifier = Modifier.weight(2f)
                ) {
                    for (star in 5 downTo 1) {
                        RatingBar(
                            star = star,
                            count = stats.distribution.getStarCount(star),
                            percentage = stats.distribution.getPercentage(star)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingBar(star: Int, count: Int, percentage: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = "$star",
            fontSize = 12.sp,
            modifier = Modifier.width(12.dp)
        )
        
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(12.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = PrimaryBrown,
            trackColor = Color.Gray.copy(alpha = 0.2f)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "$count",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun WriteReviewButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBrown),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tulis Review",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyReviewsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.RateReview,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Belum ada review",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Text(
                text = "Jadilah yang pertama memberikan review!",
                fontSize = 14.sp,
                color = Color.Gray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ReviewItemCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // User Info and Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryBrown.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.userName.take(1).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.userName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryBrown
                    )
                    Text(
                        text = review.getTimeAgo(),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Rating Stars
                Row {
                    repeat(5) { index ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < review.rating)
                                Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Review Comment
            if (review.comment.isNotBlank()) {
                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
