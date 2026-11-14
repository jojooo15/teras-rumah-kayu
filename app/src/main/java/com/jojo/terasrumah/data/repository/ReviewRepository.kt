package com.jojo.terasrumah.data.repository

import android.content.Context
import com.jojo.terasrumah.data.api.ApiClient
import com.jojo.terasrumah.data.api.ApiResult
import com.jojo.terasrumah.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class ReviewRepository private constructor(private val context: Context) {

    private val reviewApiService = ApiClient.reviewApiService
    private val userApiService = ApiClient.userApiService
    private val cacheMutex = Mutex()
    private val gson = Gson()

    // Cache variables
    private var cachedReviews: MutableMap<Int, List<Review>> = mutableMapOf()
    private var cachedReviewStats: MutableMap<Int, ReviewStats> = mutableMapOf()
    private var cacheTimestamp: Long = 0
    private val cacheValidityDuration = 2 * 60 * 1000L // 2 minutes

    // Persistent storage for simulated reviews
    private val simulatedReviewsPrefs = context.getSharedPreferences("simulated_reviews", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: ReviewRepository? = null

        fun getInstance(context: Context): ReviewRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ReviewRepository(context)
                INSTANCE = instance
                instance
            }
        }

        // For backward compatibility
        fun getInstance(): ReviewRepository {
            return INSTANCE ?: throw IllegalStateException("ReviewRepository must be initialized with context first")
        }
    }

    private fun isCacheValid(): Boolean {
        return System.currentTimeMillis() - cacheTimestamp < cacheValidityDuration
    }

    fun getReviewsByFoodItem(
        foodItemId: Int,
        page: Int = 1,
        limit: Int = 10,
        sortBy: String = "newest"
    ): Flow<ApiResult<ReviewListData>> = flow {
        emit(ApiResult.Loading)

        try {
            cacheMutex.withLock {
                // Return cached data if valid and it's the first page
                if (page == 1 && isCacheValid() && cachedReviews.containsKey(foodItemId)) {
                    val reviews = cachedReviews[foodItemId] ?: emptyList()
                    val stats = cachedReviewStats[foodItemId]
                    emit(ApiResult.Success(ReviewListData(
                        reviews = reviews,
                        stats = stats
                    )))
                    return@flow
                }
            }

            val response = reviewApiService.getReviewsByFoodItem(foodItemId, page, limit, sortBy)

            if (response.isSuccessful && response.body() != null) {
                val reviewListResponse = response.body()!!
                val reviewData = reviewListResponse.data ?: ReviewListData()

                cacheMutex.withLock {
                    if (page == 1) {
                        cachedReviews[foodItemId] = reviewData.reviews
                        reviewData.stats?.let { cachedReviewStats[foodItemId] = it }
                        cacheTimestamp = System.currentTimeMillis()
                    }
                }

                emit(ApiResult.Success(reviewData))
            } else {
                throw Exception("API Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            println("DEBUG: API call failed, using fallback: ${e.message}")

            // Fallback to local storage
            try {
                val localReviews = getSimulatedReviews(foodItemId)
                val localStats = getSimulatedReviewStats(foodItemId)

                println("DEBUG: Using local reviews: ${localReviews.size} reviews found")

                emit(ApiResult.Success(ReviewListData(
                    reviews = localReviews,
                    stats = localStats
                )))
            } catch (fallbackException: Exception) {
                println("DEBUG: Fallback also failed: ${fallbackException.message}")
                emit(ApiResult.Success(ReviewListData()))
            }
        }
    }

    fun createReview(request: ReviewRequest): Flow<ApiResult<Review>> = flow {
        emit(ApiResult.Loading)

        try {
            val response = reviewApiService.createReview(request)

            if (response.isSuccessful && response.body() != null) {
                val reviewResponse = response.body()!!
                if (reviewResponse.success && reviewResponse.data != null) {
                    // Clear cache for this food item to force refresh
                    cacheMutex.withLock {
                        cachedReviews.remove(request.foodItemId)
                        cachedReviewStats.remove(request.foodItemId)
                    }

                    emit(ApiResult.Success(reviewResponse.data))
                } else {
                    throw Exception(reviewResponse.message)
                }
            } else {
                throw Exception("API Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            println("DEBUG: API call failed, using fallback: ${e.message}")

            // Fallback to local storage
            try {
                // Create a simulated review
                val simulatedReview = Review(
                    id = UUID.randomUUID().toString(),
                    userId = request.userId,
                    foodItemId = request.foodItemId,
                    rating = request.rating,
                    comment = request.comment,
                    userName = request.userName,
                    userPhone = request.userPhone,
                    createdAt = java.util.Date(System.currentTimeMillis()),
                    pointsAwarded = 10
                )

                println("DEBUG: ReviewRepository - Created simulated review with ${simulatedReview.pointsAwarded} points for user ${request.userId}")

                // Save to local storage
                saveSimulatedReview(request.foodItemId, simulatedReview)

                // Update review stats after adding new review
                val allReviews = getSimulatedReviews(request.foodItemId)
                updateSimulatedReviewStats(request.foodItemId, allReviews)

                // Clear cache to force refresh
                cacheMutex.withLock {
                    cachedReviews.remove(request.foodItemId)
                    cachedReviewStats.remove(request.foodItemId)
                }

                println("DEBUG: Review saved to local storage")
                emit(ApiResult.Success(simulatedReview))
            } catch (fallbackException: Exception) {
                println("DEBUG: Fallback also failed: ${fallbackException.message}")
                emit(ApiResult.Error(fallbackException))
            }
        }
    }

    fun getReviewStats(foodItemId: Int): Flow<ApiResult<ReviewStats>> = flow {
        emit(ApiResult.Loading)

        try {
            cacheMutex.withLock {
                // Return cached stats if valid
                if (isCacheValid() && cachedReviewStats.containsKey(foodItemId)) {
                    emit(ApiResult.Success(cachedReviewStats[foodItemId]!!))
                    return@flow
                }
            }

            val response = reviewApiService.getReviewStats(foodItemId)

            if (response.isSuccessful && response.body() != null) {
                val statsResponse = response.body()!!
                if (statsResponse.success && statsResponse.data != null) {
                    cacheMutex.withLock {
                        cachedReviewStats[foodItemId] = statsResponse.data
                        cacheTimestamp = System.currentTimeMillis()
                    }
                    
                    emit(ApiResult.Success(statsResponse.data))
                } else {
                    throw Exception(statsResponse.message)
                }
            } else {
                throw Exception("API Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            println("DEBUG: API call failed, using fallback: ${e.message}")

            // Fallback to local storage
            try {
                val localStats = getSimulatedReviewStats(foodItemId)
                println("DEBUG: Using local stats: ${localStats.totalReviews} reviews")
                emit(ApiResult.Success(localStats))
            } catch (fallbackException: Exception) {
                println("DEBUG: Fallback also failed: ${fallbackException.message}")
                emit(ApiResult.Success(ReviewStats()))
            }
        }
    }

    // Simplified getUserPoints - returns basic user points info
    fun getUserPoints(userId: String): Flow<ApiResult<Map<String, Any>>> = flow {
        emit(ApiResult.Loading)

        try {
            val response = userApiService.getUserPoints(userId)

            if (response.isSuccessful && response.body() != null) {
                val pointsData = response.body()!!
                emit(ApiResult.Success(pointsData))
            } else {
                throw Exception("API Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e))
        }
    }

    fun clearCache() {
        cacheMutex.tryLock()
        try {
            cachedReviews.clear()
            cachedReviewStats.clear()
            cacheTimestamp = 0
        } finally {
            cacheMutex.unlock()
        }
    }

    // Generate random reviews for food items to make ratings realistic
    fun generateRandomReviewsForFood(foodItemId: Int, targetRating: Double) {
        val existingReviews = getSimulatedReviews(foodItemId)
        if (existingReviews.isNotEmpty()) {
            println("DEBUG: Food item $foodItemId already has reviews, skipping generation")
            return
        }

        val reviewCount = (15..35).random() // Random number of reviews between 15-35
        val reviews = mutableListOf<Review>()

        // Generate reviews with distribution that matches target rating
        for (i in 1..reviewCount) {
            val rating = generateRatingBasedOnTarget(targetRating)
            val review = Review(
                id = "random_${foodItemId}_$i",
                userId = "user_random_$i",
                foodItemId = foodItemId,
                rating = rating,
                comment = getRandomComment(rating),
                userName = getRandomUserName(),
                userPhone = "+628${(100000000..999999999).random()}",
                createdAt = generateRandomDate(),
                pointsAwarded = 10
            )
            reviews.add(review)
        }

        // Save all reviews
        val allReviews = existingReviews + reviews
        saveSimulatedReviews(foodItemId, allReviews)

        println("DEBUG: Generated ${reviews.size} random reviews for food item $foodItemId with target rating $targetRating")

        // Update stats
        updateSimulatedReviewStats(foodItemId, allReviews)
    }

    private fun generateRatingBasedOnTarget(targetRating: Double): Int {
        // Create weighted distribution based on target rating
        return when {
            targetRating >= 4.5 -> {
                // Mostly 5 stars, some 4 stars
                val weights = listOf(0, 0, 5, 25, 70) // 1,2,3,4,5 star weights
                getWeightedRandomRating(weights)
            }
            targetRating >= 4.0 -> {
                // Mix of 4 and 5 stars, some 3
                val weights = listOf(0, 2, 8, 45, 45)
                getWeightedRandomRating(weights)
            }
            targetRating >= 3.5 -> {
                // Balanced distribution around 3-4 stars
                val weights = listOf(2, 5, 25, 40, 28)
                getWeightedRandomRating(weights)
            }
            targetRating >= 3.0 -> {
                // More 3 stars, some lower
                val weights = listOf(5, 10, 35, 30, 20)
                getWeightedRandomRating(weights)
            }
            else -> {
                // Lower ratings
                val weights = listOf(15, 25, 30, 20, 10)
                getWeightedRandomRating(weights)
            }
        }
    }

    private fun getWeightedRandomRating(weights: List<Int>): Int {
        val totalWeight = weights.sum()
        val random = (1..totalWeight).random()
        var currentWeight = 0

        for (i in weights.indices) {
            currentWeight += weights[i]
            if (random <= currentWeight) {
                return i + 1 // Return 1-5 stars
            }
        }
        return 5 // Fallback
    }

    private fun getRandomComment(rating: Int): String {
        val comments = when (rating) {
            5 -> listOf(
                "Makanannya enak banget! Pasti pesan lagi.",
                "Rasanya mantap, porsi juga pas. Recommended!",
                "Pelayanan cepat dan makanan berkualitas. Top!",
                "Sangat puas dengan rasa dan pelayanannya.",
                "Makanan favorit keluarga. Selalu enak!",
                "Bumbu meresap sempurna, sangat lezat.",
                "Harga sebanding dengan kualitas. Excellent!",
                "Sudah langganan di sini, tidak pernah mengecewakan.",
                "Makanan homemade yang autentik. Love it!",
                "Porsi besar, rasa enak, harga terjangkau."
            )
            4 -> listOf(
                "Enak, tapi bisa lebih pedas lagi.",
                "Makanan bagus, pengiriman juga cepat.",
                "Rasa oke, porsi cukup mengenyangkan.",
                "Lumayan enak, akan coba menu lain.",
                "Bagus untuk harga segini. Recommended.",
                "Makanan segar dan enak. Good job!",
                "Rasa sesuai ekspektasi. Puas!",
                "Enak banget, cuma agak lama nunggunya.",
                "Makanan berkualitas dengan harga reasonable.",
                "Cukup memuaskan, akan pesan lagi."
            )
            3 -> listOf(
                "Biasa aja sih, tidak istimewa.",
                "Rasa standar, porsi juga standar.",
                "Lumayan lah untuk harga segini.",
                "Oke, tapi masih bisa ditingkatkan.",
                "Tidak buruk, tapi juga tidak wow.",
                "Cukup mengenyangkan, rasa biasa.",
                "Standar aja, mungkin coba menu lain.",
                "Lumayan enak, tapi kurang bumbu.",
                "Oke untuk sekali-kali makan.",
                "Biasa, sesuai harga yang dibayar."
            )
            2 -> listOf(
                "Kurang enak, bumbu kurang meresap.",
                "Agak hambar, perlu ditingkatkan.",
                "Tidak sesuai ekspektasi.",
                "Rasa kurang, porsi juga kecil.",
                "Biasa banget, agak kecewa.",
                "Kurang puas dengan rasanya.",
                "Makanan dingin saat sampai.",
                "Rasa tidak konsisten.",
                "Agak mengecewakan untuk harga segini.",
                "Kurang bumbu, agak bland."
            )
            1 -> listOf(
                "Sangat mengecewakan, tidak akan pesan lagi.",
                "Makanan tidak enak sama sekali.",
                "Kualitas buruk, tidak recommended.",
                "Rasa aneh, tidak bisa dihabiskan.",
                "Sangat tidak puas dengan pesanan ini.",
                "Makanan basi, sangat kecewa.",
                "Tidak layak untuk dijual.",
                "Terburuk yang pernah saya coba.",
                "Uang terbuang sia-sia.",
                "Jangan pesan menu ini!"
            )
            else -> listOf("Makanan lumayan.")
        }
        return comments.random()
    }

    private fun getRandomUserName(): String {
        val names = listOf(
            "Andi", "Budi", "Citra", "Dewi", "Eko", "Fitri", "Gita", "Hadi",
            "Indra", "Joko", "Kartika", "Lina", "Maya", "Nanda", "Oka", "Putri",
            "Qori", "Rina", "Sari", "Tono", "Udin", "Vina", "Wati", "Yani", "Zaki",
            "Agus", "Bayu", "Candra", "Dian", "Erni", "Fajar", "Gilang", "Hana",
            "Irma", "Jihan", "Kiki", "Lulu", "Mira", "Nina", "Oki", "Prita"
        )
        return names.random()
    }

    private fun generateRandomDate(): java.util.Date {
        // Generate random date within last 30 days
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
        val randomTime = (thirtyDaysAgo..now).random()
        return java.util.Date(randomTime)
    }

    private fun saveSimulatedReviews(foodItemId: Int, reviews: List<Review>) {
        val reviewsJson = gson.toJson(reviews)
        simulatedReviewsPrefs.edit()
            .putString("reviews_$foodItemId", reviewsJson)
            .apply()
    }

    // Local storage functions for fallback
    fun getSimulatedReviews(foodItemId: Int): List<Review> {
        val reviewsJson = simulatedReviewsPrefs.getString("reviews_$foodItemId", null)
        return if (reviewsJson != null) {
            val type = object : TypeToken<List<Review>>() {}.type
            gson.fromJson(reviewsJson, type)
        } else {
            emptyList()
        }
    }

    // Get user's reviews from local storage
    fun getUserReviews(userId: String): Flow<ApiResult<List<Review>>> = flow {
        emit(ApiResult.Loading)

        try {
            // Try to get from API first
            val response = reviewApiService.getReviewsByUser(userId)

            if (response.isSuccessful && response.body() != null) {
                val reviewListResponse = response.body()!!
                val reviews = reviewListResponse.data?.reviews ?: emptyList()
                emit(ApiResult.Success(reviews))
            } else {
                throw Exception("API Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            println("DEBUG: API call failed, using fallback for user reviews: ${e.message}")

            // Fallback to local storage - collect all reviews for this user
            try {
                val allUserReviews = mutableListOf<Review>()

                // Get all keys that start with "reviews_"
                val allKeys = simulatedReviewsPrefs.all.keys.filter { it.startsWith("reviews_") }

                // For each food item, check if there are reviews by this user
                for (key in allKeys) {
                    val foodItemReviews = getSimulatedReviews(key.removePrefix("reviews_").toInt())
                    val userReviews = foodItemReviews.filter { it.userId == userId }
                    allUserReviews.addAll(userReviews)
                }

                println("DEBUG: Found ${allUserReviews.size} reviews for user $userId in local storage")
                emit(ApiResult.Success(allUserReviews))
            } catch (fallbackException: Exception) {
                println("DEBUG: Fallback also failed: ${fallbackException.message}")
                emit(ApiResult.Success(emptyList()))
            }
        }
    }

    private fun getSimulatedReviewStats(foodItemId: Int): ReviewStats {
        val statsJson = simulatedReviewsPrefs.getString("stats_$foodItemId", null)
        return if (statsJson != null) {
            gson.fromJson(statsJson, ReviewStats::class.java)
        } else {
            ReviewStats()
        }
    }

    private fun saveSimulatedReview(foodItemId: Int, review: Review) {
        try {
            // Get existing reviews
            val existingReviews = getSimulatedReviews(foodItemId).toMutableList()

            // Add new review
            existingReviews.add(review)

            // Save updated reviews
            val updatedJson = gson.toJson(existingReviews)
            simulatedReviewsPrefs.edit().putString("reviews_$foodItemId", updatedJson).apply()

            // Update stats
            updateSimulatedReviewStats(foodItemId, existingReviews)

            println("DEBUG: Review saved to local storage for food item: $foodItemId")
        } catch (e: Exception) {
            println("DEBUG: Error saving review to SharedPreferences: ${e.message}")
        }
    }

    private fun updateSimulatedReviewStats(foodItemId: Int, reviews: List<Review>) {
        try {
            if (reviews.isEmpty()) {
                return
            }

            // Calculate stats
            val totalReviews = reviews.size
            val totalRating = reviews.sumOf { it.rating }
            val averageRating = totalRating.toFloat() / totalReviews

            // Create distribution
            val distribution = mutableMapOf<Int, Int>()
            for (i in 1..5) {
                distribution[i] = reviews.count { it.rating == i }
            }

            // Create stats object
            val stats = ReviewStats(
                totalReviews = totalReviews,
                averageRating = averageRating.toDouble(),
                distribution = ReviewDistribution(
                    oneStar = distribution[1] ?: 0,
                    twoStar = distribution[2] ?: 0,
                    threeStar = distribution[3] ?: 0,
                    fourStar = distribution[4] ?: 0,
                    fiveStar = distribution[5] ?: 0
                )
            )

            // Save stats
            val statsJson = gson.toJson(stats)
            simulatedReviewsPrefs.edit().putString("stats_$foodItemId", statsJson).apply()

            println("DEBUG: Review stats updated in local storage for food item: $foodItemId")
        } catch (e: Exception) {
            println("DEBUG: Error updating review stats in SharedPreferences: ${e.message}")
        }
    }

    // This is a duplicate companion object - removed
}
