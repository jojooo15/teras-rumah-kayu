package com.jojo.terasrumah.data.api

import com.jojo.terasrumah.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ReviewApiService {
    
    // Get reviews for a specific food item
    @GET("reviews/food/{foodItemId}")
    suspend fun getReviewsByFoodItem(
        @Path("foodItemId") foodItemId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("sortBy") sortBy: String = "newest"
    ): Response<ReviewListResponse>
    
    // Get reviews by a specific user
    @GET("reviews/user/{userId}")
    suspend fun getReviewsByUser(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<ReviewListResponse>
    
    // Create a new review
    @POST("reviews")
    suspend fun createReview(
        @Body request: ReviewRequest
    ): Response<ReviewResponse>
    
    // Update a review
    @PUT("reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: String,
        @Body request: Map<String, Any>
    ): Response<ReviewResponse>
    
    // Delete a review (soft delete)
    @HTTP(method = "DELETE", path = "reviews/{reviewId}", hasBody = true)
    suspend fun deleteReview(
        @Path("reviewId") reviewId: String,
        @Body request: Map<String, String>
    ): Response<ReviewResponse>
    
    // Get review statistics for a food item
    @GET("reviews/stats/{foodItemId}")
    suspend fun getReviewStats(
        @Path("foodItemId") foodItemId: Int
    ): Response<ReviewStatsResponse>
}

interface UserApiService {
    
    // Get user profile with points
    @GET("users/{userId}")
    suspend fun getUserProfile(
        @Path("userId") userId: String
    ): Response<AuthResponse>
    
    // Get user points (simplified)
    @GET("users/{userId}/points")
    suspend fun getUserPoints(
        @Path("userId") userId: String
    ): Response<Map<String, Any>>
    
    // Add points to user (simplified)
    @POST("users/{userId}/points/add")
    suspend fun addPointsToUser(
        @Path("userId") userId: String,
        @Body request: AddPointsRequest
    ): Response<Map<String, Any>>
    
    // Get top reviewers leaderboard
    @GET("users/leaderboard/reviewers")
    suspend fun getTopReviewers(
        @Query("limit") limit: Int = 10
    ): Response<LeaderboardResponse>

    // Get user addresses
    @GET("users/{userId}/addresses")
    suspend fun getUserAddresses(
        @Path("userId") userId: String
    ): Response<AddressListResponse>

    // Add new address
    @POST("users/{userId}/addresses")
    suspend fun addUserAddress(
        @Path("userId") userId: String,
        @Body request: AddAddressRequest
    ): Response<AddressResponse>

    // Update address
    @PUT("users/{userId}/addresses/{addressId}")
    suspend fun updateUserAddress(
        @Path("userId") userId: String,
        @Path("addressId") addressId: String,
        @Body request: UpdateAddressRequest
    ): Response<AddressResponse>

    // Delete address
    @DELETE("users/{userId}/addresses/{addressId}")
    suspend fun deleteUserAddress(
        @Path("userId") userId: String,
        @Path("addressId") addressId: String
    ): Response<AddressResponse>

    // Set default address
    @PUT("users/{userId}/addresses/{addressId}/default")
    suspend fun setDefaultAddress(
        @Path("userId") userId: String,
        @Path("addressId") addressId: String
    ): Response<AddressResponse>
    
    // This endpoint was removed - users by level functionality simplified
    
    // Create or update user
    @POST("users")
    suspend fun createOrUpdateUser(
        @Body request: Map<String, Any>
    ): Response<AuthResponse>
    
    // Update user profile
    @PUT("users/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: String,
        @Body request: UpdateUserRequest
    ): Response<AuthResponse>
    
    // Update last login time
    @POST("users/{userId}/login")
    suspend fun updateLoginTime(
        @Path("userId") userId: String
    ): Response<Map<String, Any>>
}

// Removed VoucherApiService - using points system only

// Extension functions for API responses
fun Response<ReviewListResponse>.getReviews(): List<Review> {
    return body()?.data?.reviews ?: emptyList()
}

fun Response<ReviewListResponse>.getReviewStats(): ReviewStats? {
    return body()?.data?.stats
}

// Removed voucher extension functions - using points system only

// Removed - UserPointsResponse no longer exists

fun Response<LeaderboardResponse>.getLeaderboard(): List<LeaderboardUser> {
    return body()?.data ?: emptyList()
}

// Helper functions for error handling
fun <T> Response<T>.getErrorMessage(): String {
    return if (isSuccessful) {
        "Unknown error"
    } else {
        try {
            val errorBody = errorBody()?.string()
            // Parse error message from JSON if needed
            errorBody ?: "Network error: ${code()}"
        } catch (e: Exception) {
            "Network error: ${code()}"
        }
    }
}

fun <T> Response<T>.isSuccess(): Boolean {
    return isSuccessful && body() != null
}
