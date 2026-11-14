package com.jojo.terasrumah.data.api

import com.jojo.terasrumah.data.model.*
import retrofit2.http.*

interface ApiService {
    
    // User endpoints
    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): UserResponse
    
    @POST("users")
    suspend fun createOrUpdateUser(@Body request: CreateUserRequest): UserResponse
    
    // Address endpoints
    @GET("users/{userId}/addresses")
    suspend fun getUserAddresses(@Path("userId") userId: String): AddressListResponse
    
    @POST("users/{userId}/addresses")
    suspend fun addAddress(
        @Path("userId") userId: String,
        @Body request: AddAddressRequest
    ): AddressResponse
    
    @PUT("users/{userId}/addresses/{addressId}")
    suspend fun updateAddress(
        @Path("userId") userId: String,
        @Path("addressId") addressId: String,
        @Body request: UpdateAddressRequest
    ): AddressResponse
    
    @DELETE("users/{userId}/addresses/{addressId}")
    suspend fun deleteAddress(
        @Path("userId") userId: String,
        @Path("addressId") addressId: String
    ): BaseResponse
    
    @PUT("users/{userId}/addresses/{addressId}/default")
    suspend fun setDefaultAddress(
        @Path("userId") userId: String,
        @Path("addressId") addressId: String
    ): AddressResponse
    
    // Points endpoints
    @POST("users/{userId}/points")
    suspend fun addPoints(
        @Path("userId") userId: String,
        @Body request: AddPointsRequest
    ): AddPointsResponse
}

// Request/Response models
data class CreateUserRequest(
    val id: String,
    val phoneNumber: String,
    val name: String,
    val isVerified: Boolean = false
)

data class UserResponse(
    val success: Boolean,
    val message: String,
    val data: User? = null
)

data class BaseResponse(
    val success: Boolean,
    val message: String
)
