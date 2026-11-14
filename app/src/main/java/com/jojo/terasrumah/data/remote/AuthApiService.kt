package com.jojo.terasrumah.data.remote

import com.jojo.terasrumah.data.model.AuthResponse
import com.jojo.terasrumah.data.model.LoginRequest
import com.jojo.terasrumah.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/verify")
    suspend fun verifyPhone(@Body request: Map<String, String>): Response<AuthResponse>
}
