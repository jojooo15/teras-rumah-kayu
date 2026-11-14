package com.jojo.terasrumah.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jojo.terasrumah.data.model.User
import com.jojo.terasrumah.data.model.UserAddress
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    
    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val PHONE_NUMBER = stringPreferencesKey("phone_number")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val IS_VERIFIED = booleanPreferencesKey("is_verified")
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val REMEMBER_ME = booleanPreferencesKey("remember_me")
        private val LOGIN_TIMESTAMP = stringPreferencesKey("login_timestamp")
        // Additional user data fields
        private val USER_POINTS = stringPreferencesKey("user_points")
        private val TOTAL_REVIEWS = stringPreferencesKey("total_reviews")
        private val USER_LEVEL = stringPreferencesKey("user_level")
        private val USER_ADDRESSES = stringPreferencesKey("user_addresses")
        private val DEFAULT_ADDRESS_ID = stringPreferencesKey("default_address_id")
    }
    
    val userFlow: Flow<User?> = context.dataStore.data.map { preferences ->
        val isLoggedIn = preferences[IS_LOGGED_IN] ?: false
        if (isLoggedIn) {
            val gson = Gson()
            val addressesJson = preferences[USER_ADDRESSES] ?: "[]"
            val addressListType = object : TypeToken<List<UserAddress>>() {}.type
            val addresses = try {
                gson.fromJson<List<UserAddress>>(addressesJson, addressListType) ?: emptyList()
            } catch (e: Exception) {
                emptyList<UserAddress>()
            }

            User(
                id = preferences[USER_ID] ?: "",
                phoneNumber = preferences[PHONE_NUMBER] ?: "",
                name = preferences[USER_NAME] ?: "",
                isVerified = preferences[IS_VERIFIED] ?: false,
                points = preferences[USER_POINTS]?.toIntOrNull() ?: 0,
                totalReviews = preferences[TOTAL_REVIEWS]?.toIntOrNull() ?: 0,
                userLevel = preferences[USER_LEVEL] ?: "BASIC",
                addresses = addresses,
                defaultAddressId = preferences[DEFAULT_ADDRESS_ID]
            )
        } else {
            null
        }
    }
    
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }
    
    val authTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }
    
    suspend fun saveUser(user: User, token: String? = null, rememberMe: Boolean = false) {
        println("DEBUG: UserPreferences - saveUser called")
        println("DEBUG: UserPreferences - Saving user: ${user.name}")
        println("DEBUG: UserPreferences - Saving points: ${user.points}")
        println("DEBUG: UserPreferences - Saving reviews: ${user.totalReviews}")
        println("DEBUG: UserPreferences - Saving level: ${user.userLevel}")

        context.dataStore.edit { preferences ->
            val gson = Gson()
            preferences[USER_ID] = user.id
            preferences[PHONE_NUMBER] = user.phoneNumber
            preferences[USER_NAME] = user.name
            preferences[IS_LOGGED_IN] = true
            preferences[IS_VERIFIED] = user.isVerified
            preferences[REMEMBER_ME] = rememberMe
            preferences[LOGIN_TIMESTAMP] = System.currentTimeMillis().toString()
            preferences[USER_POINTS] = user.points.toString()
            preferences[TOTAL_REVIEWS] = user.totalReviews.toString()
            preferences[USER_LEVEL] = user.userLevel
            preferences[USER_ADDRESSES] = gson.toJson(user.addresses)
            preferences[DEFAULT_ADDRESS_ID] = user.defaultAddressId ?: ""
            token?.let { preferences[AUTH_TOKEN] = it }
        }

        println("DEBUG: UserPreferences - User saved successfully")

        // Verify the save worked
        val savedUser = userFlow.first()
        println("DEBUG: UserPreferences - Verification - saved user points: ${savedUser?.points}")
        println("DEBUG: UserPreferences - Verification - saved user reviews: ${savedUser?.totalReviews}")
    }
    
    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    suspend fun updateVerificationStatus(isVerified: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_VERIFIED] = isVerified
        }
    }

    suspend fun getToken(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[AUTH_TOKEN]
    }

    suspend fun isSessionValid(): Boolean {
        val preferences = context.dataStore.data.first()
        val isLoggedIn = preferences[IS_LOGGED_IN] ?: false
        val rememberMe = preferences[REMEMBER_ME] ?: false
        val loginTimestamp = preferences[LOGIN_TIMESTAMP]?.toLongOrNull() ?: 0L

        if (!isLoggedIn) return false

        // If remember me is enabled, session is valid for 30 days
        // Otherwise, session is valid for 7 days
        val sessionDuration = if (rememberMe) {
            30L * 24 * 60 * 60 * 1000 // 30 days in milliseconds
        } else {
            7L * 24 * 60 * 60 * 1000 // 7 days in milliseconds
        }

        val currentTime = System.currentTimeMillis()
        return (currentTime - loginTimestamp) < sessionDuration
    }
}
