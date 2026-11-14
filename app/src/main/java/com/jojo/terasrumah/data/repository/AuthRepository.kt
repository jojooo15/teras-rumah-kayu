package com.jojo.terasrumah.data.repository

import android.content.Context
import com.jojo.terasrumah.data.api.ApiClient
import com.jojo.terasrumah.data.local.UserPreferences
import com.jojo.terasrumah.data.model.AuthResponse
import com.jojo.terasrumah.data.model.LoginRequest
import com.jojo.terasrumah.data.model.RegisterRequest
import com.jojo.terasrumah.data.model.User
import com.jojo.terasrumah.data.model.UserAddress
import com.jojo.terasrumah.data.remote.AuthApiService
import com.jojo.terasrumah.utils.PhoneNumberValidator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class AuthRepository(context: Context) {

    private val userPreferences = UserPreferences(context)
    private val userApiService = ApiClient.userApiService

    // Persistent storage for simulated users
    private val simulatedUsersPrefs = context.getSharedPreferences("simulated_users", Context.MODE_PRIVATE)
    private val gson = Gson()

    // For demo purposes, we'll simulate API calls
    // In production, you would use real API endpoints
    private val authApiService: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://your-api-base-url.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
    
    val currentUser: Flow<User?> = userPreferences.userFlow
    val isLoggedIn: Flow<Boolean> = userPreferences.isLoggedInFlow
    
    suspend fun login(phoneNumber: String, rememberMe: Boolean = false): Result<AuthResponse> {
        return try {
            val normalizedPhone = PhoneNumberValidator.normalizePhoneNumber(phoneNumber)

            println("DEBUG: Login - Original phone: $phoneNumber")
            println("DEBUG: Login - Normalized phone: $normalizedPhone")

            if (!PhoneNumberValidator.isValidPhoneNumberFormat(normalizedPhone)) {
                return Result.failure(Exception("Format nomor telepon tidak valid"))
            }

            // Simulate API call - In production, replace with actual API call
            val response = simulateLogin(normalizedPhone)

            if (response.success && response.user != null) {
                userPreferences.saveUser(response.user, response.token, rememberMe)
            }

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(phoneNumber: String, name: String): Result<AuthResponse> {
        return try {
            val normalizedPhone = PhoneNumberValidator.normalizePhoneNumber(phoneNumber)

            println("DEBUG: Register - Original phone: $phoneNumber")
            println("DEBUG: Register - Normalized phone: $normalizedPhone")

            if (!PhoneNumberValidator.isValidPhoneNumberFormat(normalizedPhone)) {
                return Result.failure(Exception("Format nomor telepon tidak valid"))
            }

            if (name.isBlank()) {
                return Result.failure(Exception("Nama tidak boleh kosong"))
            }
            
            // Real API call to MongoDB
            val userId = "user_${UUID.randomUUID()}"
            val createUserRequest = mapOf(
                "id" to userId,
                "phoneNumber" to normalizedPhone,
                "name" to name,
                "isVerified" to true
            )

            try {
                val apiResponse = userApiService.createOrUpdateUser(createUserRequest)

                if (apiResponse.isSuccessful && apiResponse.body() != null) {
                    val authResponse = apiResponse.body()!!
                    if (authResponse.success && authResponse.user != null) {
                        val token = "token_${UUID.randomUUID()}"
                        userPreferences.saveUser(authResponse.user, token)

                        Result.success(AuthResponse(
                            success = true,
                            message = "Registrasi berhasil",
                            user = authResponse.user,
                            token = token
                        ))
                    } else {
                        Result.success(AuthResponse(
                            success = false,
                            message = authResponse.message,
                            user = null,
                            token = null
                        ))
                    }
                } else {
                    Result.success(AuthResponse(
                        success = false,
                        message = "Gagal mendaftar: ${apiResponse.message()}",
                        user = null,
                        token = null
                    ))
                }
            } catch (apiException: Exception) {
                // If API call fails, fall back to simulation
                val simulatedResponse = simulateRegister(normalizedPhone, name)
                Result.success(simulatedResponse)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        userPreferences.clearUser()
    }
    
    suspend fun getCurrentUser(): User? {
        return userPreferences.userFlow.first()
    }

    suspend fun refreshCurrentUser() {
        try {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                // Try to fetch updated user data from API
                try {
                    val response = userApiService.getUserProfile(currentUser.id)
                    if (response.isSuccessful && response.body() != null) {
                        val authResponse = response.body()!!
                        if (authResponse.success && authResponse.user != null) {
                            // Update stored user data with API response
                            val currentToken = userPreferences.getToken()
                            userPreferences.saveUser(authResponse.user, currentToken)
                            println("DEBUG: User data refreshed from API")
                            return
                        }
                    }
                } catch (apiException: Exception) {
                    println("DEBUG: API refresh failed, using local data: ${apiException.message}")
                }

                // If API fails, just reload from local storage to trigger UI update
                val currentToken = userPreferences.getToken()
                val localUser = userPreferences.userFlow.first()
                if (localUser != null) {
                    // Re-save to trigger any observers
                    userPreferences.saveUser(localUser, currentToken)
                    println("DEBUG: User data refreshed from local storage")
                }
            }
        } catch (e: Exception) {
            println("DEBUG: Error in refreshCurrentUser: ${e.message}")
        }
    }

    suspend fun isSessionValid(): Boolean {
        return userPreferences.isSessionValid()
    }

    // Debug function to clear all simulated users (for testing)
    suspend fun clearSimulatedUsers() {
        simulatedUsersPrefs.edit().clear().apply()
        println("DEBUG: All simulated users cleared")
    }

    // Debug function to get all simulated users
    suspend fun getAllSimulatedUsers(): List<User> {
        return getSimulatedUsers()
    }

    // Function to check if a phone number exists in any format
    suspend fun checkPhoneNumberExists(phoneNumber: String): Boolean {
        val normalizedPhone = PhoneNumberValidator.normalizePhoneNumber(phoneNumber)
        val allUsers = getSimulatedUsers()
        val exists = allUsers.any { it.phoneNumber == normalizedPhone }

        println("DEBUG: Checking phone existence - Input: $phoneNumber")
        println("DEBUG: Checking phone existence - Normalized: $normalizedPhone")
        println("DEBUG: Checking phone existence - Exists: $exists")

        return exists
    }

    // Function to update user addresses in local storage
    suspend fun updateUserAddresses(userId: String, addresses: List<UserAddress>) {
        try {
            val currentUser = getCurrentUser()
            if (currentUser != null && currentUser.id == userId) {
                // Find default address
                val defaultAddress = addresses.find { it.isDefault }
                println("DEBUG: Default address found: ${defaultAddress?.label}, ID: ${defaultAddress?.id}")

                val updatedUser = currentUser.copy(
                    addresses = addresses,
                    defaultAddressId = defaultAddress?.id
                )

                println("DEBUG: Updated user defaultAddressId: ${updatedUser.defaultAddressId}")

                // Save updated user data
                val currentToken = userPreferences.getToken()
                userPreferences.saveUser(updatedUser, currentToken)

                println("DEBUG: User addresses updated in AuthRepository")
            }
        } catch (e: Exception) {
            println("DEBUG: Error updating user addresses: ${e.message}")
        }
    }

    // Function to update user points and review count in local storage
    suspend fun updateUserPointsAndReviews(userId: String, pointsToAdd: Int) {
        try {
            val currentUser = getCurrentUser()
            println("DEBUG: updateUserPointsAndReviews called - userId: $userId, pointsToAdd: $pointsToAdd")
            println("DEBUG: Current user: ${currentUser?.name}, points: ${currentUser?.points}, reviews: ${currentUser?.totalReviews}")

            if (currentUser != null && currentUser.id == userId) {
                // Update points and review count
                val oldPoints = currentUser.points
                val oldReviews = currentUser.totalReviews
                val oldLevel = currentUser.userLevel

                val newPoints = oldPoints + pointsToAdd
                val newTotalReviews = oldReviews + 1

                // Calculate new user level
                val newUserLevel = when {
                    newPoints >= 1000 -> "GOLD"
                    newPoints >= 500 -> "SILVER"
                    newPoints >= 100 -> "BRONZE"
                    else -> "BASIC"
                }

                val updatedUser = currentUser.copy(
                    points = newPoints,
                    totalReviews = newTotalReviews,
                    userLevel = newUserLevel
                )

                // Save updated user data
                val currentToken = userPreferences.getToken()
                println("DEBUG: About to save user with points: $newPoints, reviews: $newTotalReviews")
                userPreferences.saveUser(updatedUser, currentToken)
                println("DEBUG: User data saved to preferences")

                println("DEBUG: User points updated successfully!")
                println("DEBUG: Points: $oldPoints -> $newPoints (+$pointsToAdd)")
                println("DEBUG: Reviews: $oldReviews -> $newTotalReviews (+1)")
                println("DEBUG: Level: $oldLevel -> $newUserLevel")

                // Force multiple verification attempts to ensure save worked
                for (i in 1..3) {
                    kotlinx.coroutines.delay(50) // Small delay between checks
                    val verifyUser = getCurrentUser()
                    println("DEBUG: Verification attempt $i - saved points: ${verifyUser?.points}, saved reviews: ${verifyUser?.totalReviews}")
                    if (verifyUser?.points == newPoints) {
                        println("DEBUG: Points update verified successfully!")
                        break
                    }
                }
            } else {
                println("DEBUG: User not found or ID mismatch. Current user ID: ${currentUser?.id}, requested ID: $userId")
            }
        } catch (e: Exception) {
            println("DEBUG: Error updating user points: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Simulate API responses for demo purposes
    private suspend fun simulateLogin(phoneNumber: String): AuthResponse {
        // Simulate network delay
        kotlinx.coroutines.delay(1000)

        println("DEBUG: Attempting to login user: $phoneNumber")

        // Get all users (predefined + saved users)
        val allUsers = getSimulatedUsers()
        val user = allUsers.find { it.phoneNumber == phoneNumber }

        println("DEBUG: User found for login: ${user != null}")
        if (user != null) {
            println("DEBUG: Login user details: ${user.phoneNumber} - ${user.name}")
        } else {
            println("DEBUG: Available phone numbers:")
            allUsers.forEach { u ->
                println("DEBUG: - ${u.phoneNumber} (${u.name})")
            }
        }

        return if (user != null) {
            AuthResponse(
                success = true,
                message = "Login berhasil",
                user = user,
                token = "demo_token_${UUID.randomUUID()}"
            )
        } else {
            AuthResponse(
                success = false,
                message = "Nomor telepon belum terdaftar. Silakan daftar terlebih dahulu.",
                user = null,
                token = null
            )
        }
    }
    
    private suspend fun simulateRegister(phoneNumber: String, name: String): AuthResponse {
        // Simulate network delay
        kotlinx.coroutines.delay(1000)

        println("DEBUG: Attempting to register user: $phoneNumber - $name")

        // Check if user already exists
        val existingUsers = getSimulatedUsers()
        val existingUser = existingUsers.find { it.phoneNumber == phoneNumber }

        println("DEBUG: Existing user found: ${existingUser != null}")
        if (existingUser != null) {
            println("DEBUG: Existing user details: ${existingUser.phoneNumber} - ${existingUser.name}")
        }

        return if (existingUser != null) {
            AuthResponse(
                success = false,
                message = "Nomor telepon sudah terdaftar. Silakan login.",
                user = null,
                token = null
            )
        } else {
            val newUser = User(
                id = "user_${UUID.randomUUID()}",
                phoneNumber = phoneNumber,
                name = name,
                isVerified = true, // For demo, auto-verify
                points = 0,
                totalReviews = 0,
                userLevel = "BASIC"
            )

            println("DEBUG: Creating new user: ${newUser.phoneNumber} - ${newUser.name}")

            // Save new user to persistent storage
            saveSimulatedUser(newUser)

            AuthResponse(
                success = true,
                message = "Registrasi berhasil",
                user = newUser,
                token = "demo_token_${UUID.randomUUID()}"
            )
        }
    }
    
    // Get simulated users from persistent storage and predefined users
    private fun getSimulatedUsers(): List<User> {
        val predefinedUsers = listOf(
            User(
                id = "user_001",
                phoneNumber = "+6281234567890",
                name = "Budi Santoso",
                isVerified = true,
                points = 50,
                totalReviews = 3,
                userLevel = "BRONZE",
                addresses = listOf(
                    UserAddress(
                        id = "addr_001",
                        label = "Rumah",
                        phoneNumber = "+6281234567890",
                        fullAddress = "Jl. Merdeka No. 123, RT 01/RW 02",
                        city = "Jakarta Pusat",
                        notes = "Rumah cat hijau, ada pagar putih",
                        isDefault = true
                    )
                ),
                defaultAddressId = "addr_001"
            ),
            User(
                id = "user_002",
                phoneNumber = "+6281234567891",
                name = "Siti Nurhaliza",
                isVerified = true,
                points = 120,
                totalReviews = 5,
                userLevel = "SILVER",
                addresses = listOf(
                    UserAddress(
                        id = "addr_002",
                        label = "Kantor",
                        phoneNumber = "+6281234567891",
                        fullAddress = "Jl. Sudirman No. 45, Lantai 12",
                        city = "Jakarta Pusat",
                        notes = "Gedung perkantoran, lobby utama",
                        isDefault = true
                    ),
                    UserAddress(
                        id = "addr_003",
                        label = "Rumah",
                        phoneNumber = "+6281234567891",
                        fullAddress = "Jl. Kebon Jeruk Raya No. 88",
                        city = "Jakarta Barat",
                        notes = "Komplek perumahan, blok A",
                        isDefault = false
                    )
                ),
                defaultAddressId = "addr_002"
            ),
            User(
                id = "user_003",
                phoneNumber = "+6281234567892",
                name = "Ahmad Rizki",
                isVerified = true,
                points = 30,
                totalReviews = 1,
                userLevel = "BASIC",
                addresses = emptyList(),
                defaultAddressId = null
            )
        )

        // Get saved users from SharedPreferences
        val savedUsersJson = simulatedUsersPrefs.getString("users", "[]")
        val savedUsers = try {
            val type = object : TypeToken<List<User>>() {}.type
            gson.fromJson<List<User>>(savedUsersJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList<User>()
        }

        // Debug logging
        println("DEBUG: Predefined users count: ${predefinedUsers.size}")
        println("DEBUG: Saved users count: ${savedUsers.size}")
        savedUsers.forEach { user ->
            println("DEBUG: Saved user: ${user.phoneNumber} - ${user.name}")
        }

        return predefinedUsers + savedUsers
    }

    // Save a new simulated user to persistent storage
    private fun saveSimulatedUser(user: User) {
        try {
            val savedUsersJson = simulatedUsersPrefs.getString("users", "[]")
            val savedUsers = try {
                val type = object : TypeToken<MutableList<User>>() {}.type
                gson.fromJson<MutableList<User>>(savedUsersJson, type) ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf<User>()
            }

            // Add new user if not already exists
            if (savedUsers.none { it.phoneNumber == user.phoneNumber }) {
                savedUsers.add(user)
                val updatedJson = gson.toJson(savedUsers)
                simulatedUsersPrefs.edit().putString("users", updatedJson).apply()

                // Debug log
                println("DEBUG: User saved to SharedPreferences: ${user.phoneNumber} - ${user.name}")
                println("DEBUG: Total saved users: ${savedUsers.size}")
            } else {
                println("DEBUG: User already exists in SharedPreferences: ${user.phoneNumber}")
            }
        } catch (e: Exception) {
            println("DEBUG: Error saving user to SharedPreferences: ${e.message}")
        }
    }
}
