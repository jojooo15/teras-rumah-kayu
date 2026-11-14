package com.jojo.terasrumah.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Review(
    @SerializedName("_id")
    val id: String = "",
    val userId: String = "",
    val foodItemId: Int = 0,
    val rating: Int = 0,
    val comment: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val pointsAwarded: Int = 10,
    val adminResponse: String? = null,
    val adminResponseDate: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val formattedDate: String = ""
)

data class ReviewRequest(
    val userId: String,
    val foodItemId: Int,
    val rating: Int,
    val comment: String,
    val userName: String,
    val userPhone: String
)

data class ReviewResponse(
    val success: Boolean,
    val message: String,
    val data: Review? = null
)

data class ReviewListResponse(
    val success: Boolean,
    val data: ReviewListData? = null,
    val message: String = ""
)

data class ReviewListData(
    val reviews: List<Review> = emptyList(),
    val pagination: ReviewPagination = ReviewPagination(),
    val stats: ReviewStats? = null
)

data class ReviewPagination(
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalReviews: Int = 0,
    val hasNext: Boolean = false,
    val hasPrev: Boolean = false
)

data class ReviewStats(
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0,
    val distribution: ReviewDistribution = ReviewDistribution()
)

data class ReviewDistribution(
    @SerializedName("5")
    val fiveStar: Int = 0,
    @SerializedName("4")
    val fourStar: Int = 0,
    @SerializedName("3")
    val threeStar: Int = 0,
    @SerializedName("2")
    val twoStar: Int = 0,
    @SerializedName("1")
    val oneStar: Int = 0
) {
    fun getStarCount(stars: Int): Int {
        return when (stars) {
            5 -> fiveStar
            4 -> fourStar
            3 -> threeStar
            2 -> twoStar
            1 -> oneStar
            else -> 0
        }
    }
    
    fun getTotalReviews(): Int {
        return fiveStar + fourStar + threeStar + twoStar + oneStar
    }
    
    fun getPercentage(stars: Int): Float {
        val total = getTotalReviews()
        if (total == 0) return 0f
        return (getStarCount(stars).toFloat() / total.toFloat()) * 100f
    }
}

data class ReviewStatsResponse(
    val success: Boolean,
    val data: ReviewStats? = null,
    val message: String = ""
)

// Extension functions for Review
fun Review.getStarRating(): Float = rating.toFloat()

fun Review.getFormattedRating(): String = "$rating/5"

fun Review.isHighRating(): Boolean = rating >= 4

fun Review.isLowRating(): Boolean = rating <= 2

fun Review.getTimeAgo(): String {
    val now = System.currentTimeMillis()
    val reviewTime = createdAt.time
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

fun Review.canEdit(): Boolean {
    // User can edit review within 24 hours
    val now = System.currentTimeMillis()
    val reviewTime = createdAt.time
    val hoursDiff = (now - reviewTime) / (1000 * 60 * 60)
    return hoursDiff < 24
}

// Validation functions
fun ReviewRequest.isValid(): Boolean {
    return userId.isNotBlank() &&
            foodItemId > 0 &&
            rating in 1..5 &&
            comment.isNotBlank() &&
            userName.isNotBlank() &&
            userPhone.isNotBlank()
}

fun ReviewRequest.getValidationError(): String? {
    return when {
        userId.isBlank() -> "User ID tidak boleh kosong"
        foodItemId <= 0 -> "Food Item ID tidak valid"
        rating !in 1..5 -> "Rating harus antara 1-5"
        comment.isBlank() -> "Komentar tidak boleh kosong"
        comment.length < 10 -> "Komentar minimal 10 karakter"
        comment.length > 500 -> "Komentar maksimal 500 karakter"
        userName.isBlank() -> "Nama tidak boleh kosong"
        userPhone.isBlank() -> "Nomor telepon tidak boleh kosong"
        else -> null
    }
}
