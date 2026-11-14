package com.jojo.terasrumah.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

// Simplified User data class focusing on core user data: name, phone, address
data class User(
    val id: String = "",
    val phoneNumber: String = "",
    val name: String = "",
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    // Simplified points system
    val points: Int = 0,
    // Review count for basic statistics
    val totalReviews: Int = 0,
    // Simplified user addresses
    val addresses: List<UserAddress> = emptyList(),
    val defaultAddressId: String? = null,
    val lastLoginAt: Long = System.currentTimeMillis(),
    val lastOrderAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Virtual fields computed from points
    val userLevel: String = "BASIC",
    val nextLevelPoints: Int = 0,
    val formattedPhoneNumber: String = ""
)

data class AuthRequest(
    val phoneNumber: String,
    val name: String? = null // Only for registration
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null,
    val token: String? = null
)

data class LoginRequest(
    val phoneNumber: String
)

data class RegisterRequest(
    val phoneNumber: String,
    val name: String
)

// Removed complex point history and preferences for simplicity

// Simplified UserAddress - core delivery information
data class UserAddress(
    val id: String = "",
    val label: String = "", // "Rumah", "Kantor", "Kos", etc.
    val fullAddress: String = "",
    val city: String = "", // Kota
    val phoneNumber: String = "",
    val notes: String = "", // Catatan tambahan (patokan, dll)
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// Simplified points request
data class AddPointsRequest(
    val points: Int
)

// Simplified address request classes
data class AddAddressRequest(
    val label: String,
    val fullAddress: String,
    val city: String,
    val phoneNumber: String,
    val notes: String = "",
    val isDefault: Boolean = false
)

data class UpdateAddressRequest(
    val label: String,
    val fullAddress: String,
    val city: String,
    val phoneNumber: String,
    val notes: String = "",
    val isDefault: Boolean = false
)

data class AddressResponse(
    val success: Boolean,
    val message: String,
    val data: UserAddress? = null
)

data class AddressListResponse(
    val success: Boolean,
    val data: List<UserAddress> = emptyList(),
    val message: String = ""
)

data class AddPointsResponse(
    val success: Boolean,
    val message: String,
    val data: AddPointsData? = null
)

data class AddPointsData(
    val currentPoints: Int = 0,
    val pointsAdded: Int = 0
)

data class LeaderboardResponse(
    val success: Boolean,
    val data: List<LeaderboardUser> = emptyList(),
    val message: String = ""
)

data class LeaderboardUser(
    val name: String = "",
    val phoneNumber: String = "",
    val totalReviews: Int = 0,
    val points: Int = 0,
    val userLevel: String = "BASIC"
)

// Simplified user update request
data class UpdateUserRequest(
    val name: String? = null
)

data class LoginUpdateRequest(
    val userId: String
)

// Extension functions for User
fun User.getUserLevelInfo(): UserLevelInfo {
    return when (userLevel) {
        "GOLD" -> UserLevelInfo("Gold", "🥇", "#FFD700", 1000)
        "SILVER" -> UserLevelInfo("Silver", "🥈", "#C0C0C0", 500)
        "BRONZE" -> UserLevelInfo("Bronze", "🥉", "#CD7F32", 100)
        else -> UserLevelInfo("Basic", "⭐", "#808080", 0)
    }
}

fun User.getPointsToNextLevel(): Int {
    return when (userLevel) {
        "BASIC" -> 100 - points
        "BRONZE" -> 500 - points
        "SILVER" -> 1000 - points
        "GOLD" -> 0
        else -> 0
    }.coerceAtLeast(0)
}

fun User.getDefaultAddress(): UserAddress? {
    // First try to find by defaultAddressId
    val byId = addresses.find { it.id == defaultAddressId }
    if (byId != null) {
        return byId
    }

    // Then try to find by isDefault flag
    val byFlag = addresses.find { it.isDefault }
    if (byFlag != null) {
        return byFlag
    }

    // If no default is set but we have addresses, return the first one
    if (addresses.isNotEmpty()) {
        return addresses.first()
    }

    // No addresses available
    return null
}

fun User.hasAddresses(): Boolean {
    return addresses.isNotEmpty()
}

// Simplified address formatting functions
fun UserAddress.getFormattedAddress(): String {
    return buildString {
        append(fullAddress)
        if (city.isNotBlank()) append(", $city")
    }
}

fun UserAddress.getShortAddress(): String {
    return city.ifBlank { "Alamat" }
}

fun User.getProgressToNextLevel(): Float {
    val currentLevelMin = when (userLevel) {
        "BASIC" -> 0
        "BRONZE" -> 100
        "SILVER" -> 500
        "GOLD" -> 1000
        else -> 0
    }

    val nextLevelMin = when (userLevel) {
        "BASIC" -> 100
        "BRONZE" -> 500
        "SILVER" -> 1000
        "GOLD" -> 1000 // Already at max level
        else -> 100
    }

    if (userLevel == "GOLD") return 1.0f

    val progress = (points - currentLevelMin).toFloat() / (nextLevelMin - currentLevelMin).toFloat()
    return progress.coerceIn(0f, 1f)
}

// Removed voucher system - using points only

data class UserLevelInfo(
    val name: String,
    val icon: String,
    val color: String,
    val minPoints: Int
)
