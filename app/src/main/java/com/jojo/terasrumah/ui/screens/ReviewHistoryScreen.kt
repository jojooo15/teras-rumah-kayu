package com.jojo.terasrumah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jojo.terasrumah.data.api.ApiResult
import com.jojo.terasrumah.data.model.Review
import com.jojo.terasrumah.data.repository.ReviewRepository
import com.jojo.terasrumah.ui.theme.PrimaryBrown
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewHistoryScreen(
    userId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val reviewRepository = remember { ReviewRepository.getInstance(context) }

    var userReviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load user reviews when screen opens
    LaunchedEffect(userId) {
        reviewRepository.getUserReviews(userId).collect { result ->
            when (result) {
                is ApiResult.Loading -> {
                    isLoading = true
                    errorMessage = null
                }
                is ApiResult.Success -> {
                    userReviews = result.data
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Riwayat Review",
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
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBrown)
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (userReviews.isEmpty()) {
                    item {
                        EmptyReviewHistoryCard()
                    }
                } else {
                    // Summary Card
                    item {
                        ReviewSummaryCard(
                            totalReviews = userReviews.size,
                            totalPoints = userReviews.sumOf { it.pointsAwarded },
                            averageRating = userReviews.map { it.rating }.average().toFloat()
                        )
                    }

                    // Reviews List
                    items(userReviews) { review ->
                        RealReviewHistoryItemCard(review = review)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewSummaryCard(
    totalReviews: Int,
    totalPoints: Int,
    averageRating: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ringkasan Review",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    title = "Total Review",
                    value = totalReviews.toString()
                )
                SummaryItem(
                    title = "Poin Earned",
                    value = totalPoints.toString()
                )
                SummaryItem(
                    title = "Rata-rata Rating",
                    value = String.format("%.1f", averageRating)
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBrown
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun ReviewHistoryItemCard(
    review: MockReview
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with food name and points
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.foodItemName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryBrown,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "+${review.points} poin",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Rating and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                
                Text(
                    text = review.date,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Comment
            Text(
                text = review.comment,
                fontSize = 13.sp,
                color = Color.Black,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun EmptyReviewHistoryCard() {
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
                Icons.Default.Star,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Belum Ada Review",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            
            Text(
                text = "Mulai berikan review untuk mendapatkan poin!",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Mock data class - kept for reference
data class MockReview(
    val id: String,
    val foodItemName: String,
    val rating: Int,
    val comment: String,
    val date: String,
    val points: Int
)

@Composable
fun RealReviewHistoryItemCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with food item name and points
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Food ID: ${review.foodItemId}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimaryBrown.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                            text = "+${review.pointsAwarded} poin",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryBrown
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rating and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Text(
                    text = formatTimeAgo(review.createdAt),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment
            Text(
                text = review.comment,
                fontSize = 13.sp,
                color = Color.Black,
                lineHeight = 18.sp
            )
        }
    }
}

// Helper function to format time ago
private fun formatTimeAgo(date: Date): String {
    val now = System.currentTimeMillis()
    val reviewTime = date.time
    val diff = now - reviewTime

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = days / 30

    return when {
        months > 0 -> "${months} bulan lalu"
        weeks > 0 -> "${weeks} minggu lalu"
        days > 0 -> "${days} hari lalu"
        hours > 0 -> "${hours} jam lalu"
        minutes > 0 -> "${minutes} menit lalu"
        else -> "Baru saja"
    }
}
