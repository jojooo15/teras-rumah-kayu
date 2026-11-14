package com.jojo.terasrumah.data.api

import com.jojo.terasrumah.data.FoodItem
import com.jojo.terasrumah.data.FoodCategory
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodApiService {

    // MongoDB Legacy endpoints (compatible format)
    @GET("foodItems")
    suspend fun getAllFoodItemsLegacy(): Response<List<FoodItem>>

    @GET("foodItems")
    suspend fun getFoodItemsByCategoryLegacy(@Query("category") category: String): Response<List<FoodItem>>

    @GET("foodItems/{id}")
    suspend fun getFoodItemByIdLegacy(@Path("id") id: Int): Response<FoodItem>

    @GET("categories")
    suspend fun getCategoriesLegacy(): Response<List<CategoryResponse>>

    // Health check
    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
}

data class CategoryResponse(
    val id: String,
    val displayName: String,
    val description: String,
    val sortOrder: Int? = null,
    val itemCount: Int? = null
)

data class HealthResponse(
    val success: Boolean,
    val message: String,
    val timestamp: String,
    val environment: String
)
