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

class AddressRepository private constructor(private val context: Context) {

    private val userApiService = ApiClient.userApiService
    private val cacheMutex = Mutex()
    private val gson = Gson()

    // Persistent storage for simulated addresses
    private val simulatedAddressesPrefs = context.getSharedPreferences("simulated_addresses", Context.MODE_PRIVATE)

    // Callback to update user data when addresses change
    private var onAddressesUpdated: ((String, List<UserAddress>) -> Unit)? = null

    // Cache variables
    private var cachedAddresses: MutableMap<String, List<UserAddress>> = mutableMapOf()
    private var cacheTimestamp: Long = 0
    private val cacheValidityDuration = 5 * 60 * 1000L // 5 minutes

    private fun isCacheValid(): Boolean {
        return System.currentTimeMillis() - cacheTimestamp < cacheValidityDuration
    }

    companion object {
        @Volatile
        private var INSTANCE: AddressRepository? = null

        fun getInstance(context: Context): AddressRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AddressRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        // For backward compatibility
        fun getInstance(): AddressRepository {
            return INSTANCE ?: throw IllegalStateException("AddressRepository must be initialized with context first")
        }
    }

    fun setAddressUpdateCallback(callback: (String, List<UserAddress>) -> Unit) {
        onAddressesUpdated = callback
    }

    fun getUserAddresses(userId: String): Flow<ApiResult<List<UserAddress>>> = flow {
        emit(ApiResult.Loading)

        try {
            // Check cache first
            cacheMutex.withLock {
                if (isCacheValid() && cachedAddresses.containsKey(userId)) {
                    emit(ApiResult.Success(cachedAddresses[userId] ?: emptyList()))
                    return@flow
                }
            }

            // Try real API call to MongoDB first
            val response = userApiService.getUserAddresses(userId)

            if (response.isSuccessful && response.body() != null) {
                val addressResponse = response.body()!!
                if (addressResponse.success) {
                    // Cache the result
                    cacheMutex.withLock {
                        cachedAddresses[userId] = addressResponse.data
                        cacheTimestamp = System.currentTimeMillis()
                    }

                    emit(ApiResult.Success(addressResponse.data))
                } else {
                    emit(ApiResult.Error(Exception(addressResponse.message)))
                }
            } else {
                emit(ApiResult.Error(Exception("Failed to fetch addresses: ${response.message()}")))
            }
        } catch (e: Exception) {
            println("DEBUG: API call failed, using fallback: ${e.message}")

            // Fallback to local storage
            try {
                val addresses = getSimulatedAddresses(userId)
                emit(ApiResult.Success(addresses))
            } catch (fallbackException: Exception) {
                println("DEBUG: Fallback also failed: ${fallbackException.message}")
                emit(ApiResult.Error(fallbackException))
            }
        }
    }

    fun addAddress(userId: String, request: AddAddressRequest): Flow<ApiResult<UserAddress>> = flow {
        emit(ApiResult.Loading)

        try {
            // Try real API call to MongoDB first
            val response = userApiService.addUserAddress(userId, request)

            if (response.isSuccessful && response.body() != null) {
                val addressResponse = response.body()!!
                if (addressResponse.success && addressResponse.data != null) {
                    // Clear cache to force refresh
                    cacheMutex.withLock {
                        cachedAddresses.remove(userId)
                        cacheTimestamp = 0 // Invalidate cache
                    }

                    emit(ApiResult.Success(addressResponse.data))
                } else {
                    emit(ApiResult.Error(Exception(addressResponse.message)))
                }
            } else {
                emit(ApiResult.Error(Exception("Failed to add address: ${response.message()}")))
            }
        } catch (e: Exception) {
            println("DEBUG: API call failed, using fallback: ${e.message}")

            // Fallback to local storage
            try {
                val newAddress = simulateAddAddress(userId, request)
                emit(ApiResult.Success(newAddress))
            } catch (fallbackException: Exception) {
                println("DEBUG: Fallback also failed: ${fallbackException.message}")
                emit(ApiResult.Error(fallbackException))
            }
        }
    }

    fun updateAddress(userId: String, addressId: String, request: UpdateAddressRequest): Flow<ApiResult<UserAddress>> = flow {
        emit(ApiResult.Loading)

        try {
            // Real API call to MongoDB
            val response = userApiService.updateUserAddress(userId, addressId, request)

            if (response.isSuccessful && response.body() != null) {
                val addressResponse = response.body()!!
                if (addressResponse.success && addressResponse.data != null) {
                    // Clear cache to force refresh
                    cacheMutex.withLock {
                        cachedAddresses.remove(userId)
                        cacheTimestamp = 0 // Invalidate cache
                    }

                    emit(ApiResult.Success(addressResponse.data))
                } else {
                    emit(ApiResult.Error(Exception(addressResponse.message)))
                }
            } else {
                emit(ApiResult.Error(Exception("Failed to update address: ${response.message()}")))
            }
        } catch (e: Exception) {
            println("DEBUG: API call failed, using fallback: ${e.message}")

            // Fallback to local storage
            try {
                val updatedAddress = simulateUpdateAddress(userId, addressId, request)
                emit(ApiResult.Success(updatedAddress))
            } catch (fallbackException: Exception) {
                println("DEBUG: Fallback also failed: ${fallbackException.message}")
                emit(ApiResult.Error(fallbackException))
            }
        }
    }

    fun deleteAddress(userId: String, addressId: String): Flow<ApiResult<Boolean>> = flow {
        emit(ApiResult.Loading)

        try {
            // Real API call to MongoDB
            val response = userApiService.deleteUserAddress(userId, addressId)

            if (response.isSuccessful && response.body() != null) {
                val addressResponse = response.body()!!
                if (addressResponse.success) {
                    // Clear cache to force refresh
                    cacheMutex.withLock {
                        cachedAddresses.remove(userId)
                        cacheTimestamp = 0 // Invalidate cache
                    }

                    emit(ApiResult.Success(true))
                } else {
                    emit(ApiResult.Error(Exception(addressResponse.message)))
                }
            } else {
                emit(ApiResult.Error(Exception("Failed to delete address: ${response.message()}")))
            }
        } catch (e: Exception) {
            println("DEBUG: API call failed, using fallback: ${e.message}")

            // Fallback to local storage
            try {
                val success = simulateDeleteAddress(userId, addressId)
                emit(ApiResult.Success(success))
            } catch (fallbackException: Exception) {
                println("DEBUG: Fallback also failed: ${fallbackException.message}")
                emit(ApiResult.Error(fallbackException))
            }
        }
    }

    fun setDefaultAddress(userId: String, addressId: String): Flow<ApiResult<Boolean>> = flow {
        emit(ApiResult.Loading)

        try {
            // Real API call to MongoDB
            val response = userApiService.setDefaultAddress(userId, addressId)

            if (response.isSuccessful && response.body() != null) {
                val addressResponse = response.body()!!
                if (addressResponse.success) {
                    // Clear cache to force refresh
                    cacheMutex.withLock {
                        cachedAddresses.remove(userId)
                        cacheTimestamp = 0 // Invalidate cache
                    }

                    emit(ApiResult.Success(true))
                } else {
                    emit(ApiResult.Error(Exception(addressResponse.message)))
                }
            } else {
                emit(ApiResult.Error(Exception("Failed to set default address: ${response.message()}")))
            }
        } catch (e: Exception) {
            println("DEBUG: API call failed, using fallback: ${e.message}")

            // Fallback to local storage
            try {
                val success = simulateSetDefaultAddress(userId, addressId)
                emit(ApiResult.Success(success))
            } catch (fallbackException: Exception) {
                println("DEBUG: Fallback also failed: ${fallbackException.message}")
                emit(ApiResult.Error(fallbackException))
            }
        }
    }

    fun clearCache() {
        cacheMutex.tryLock()
        try {
            cachedAddresses.clear()
            cacheTimestamp = 0
        } finally {
            cacheMutex.unlock()
        }
    }

    // Simulation functions for fallback
    private fun simulateAddAddress(userId: String, request: AddAddressRequest): UserAddress {
        val addressId = "addr_${UUID.randomUUID()}"
        val newAddress = UserAddress(
            id = addressId,
            label = request.label,
            fullAddress = request.fullAddress,
            city = request.city,
            phoneNumber = request.phoneNumber,
            notes = request.notes,
            isDefault = request.isDefault,
            createdAt = System.currentTimeMillis()
        )

        // Save to SharedPreferences
        saveSimulatedAddress(userId, newAddress)

        println("DEBUG: Address added to local storage: ${newAddress.label} - ${newAddress.fullAddress}")

        return newAddress
    }

    private fun getSimulatedAddresses(userId: String): List<UserAddress> {
        val addressesJson = simulatedAddressesPrefs.getString("addresses_$userId", "[]")
        return try {
            val type = object : TypeToken<List<UserAddress>>() {}.type
            gson.fromJson<List<UserAddress>>(addressesJson, type) ?: emptyList()
        } catch (e: Exception) {
            println("DEBUG: Error reading addresses from SharedPreferences: ${e.message}")
            emptyList()
        }
    }

    private fun saveSimulatedAddress(userId: String, address: UserAddress) {
        try {
            val existingAddresses = getSimulatedAddresses(userId).toMutableList()

            // If this is set as default, remove default from others
            if (address.isDefault) {
                existingAddresses.forEach { addr ->
                    if (addr.isDefault) {
                        val updatedAddr = addr.copy(isDefault = false)
                        val index = existingAddresses.indexOf(addr)
                        existingAddresses[index] = updatedAddr
                    }
                }
            }

            existingAddresses.add(address)
            val updatedJson = gson.toJson(existingAddresses)
            simulatedAddressesPrefs.edit().putString("addresses_$userId", updatedJson).apply()

            // Notify callback about address update
            println("DEBUG: Calling address update callback for user: $userId")
            onAddressesUpdated?.invoke(userId, existingAddresses)

            println("DEBUG: Address saved to SharedPreferences for user: $userId")
        } catch (e: Exception) {
            println("DEBUG: Error saving address to SharedPreferences: ${e.message}")
        }
    }

    private fun simulateUpdateAddress(userId: String, addressId: String, request: UpdateAddressRequest): UserAddress {
        val existingAddresses = getSimulatedAddresses(userId).toMutableList()
        val addressIndex = existingAddresses.indexOfFirst { it.id == addressId }

        if (addressIndex == -1) {
            throw Exception("Address not found")
        }

        val updatedAddress = existingAddresses[addressIndex].copy(
            label = request.label,
            fullAddress = request.fullAddress,
            city = request.city,
            phoneNumber = request.phoneNumber,
            notes = request.notes,
            isDefault = request.isDefault
        )

        // If this is set as default, remove default from others
        if (request.isDefault) {
            existingAddresses.forEachIndexed { index, addr ->
                if (index != addressIndex && addr.isDefault) {
                    existingAddresses[index] = addr.copy(isDefault = false)
                }
            }
        }

        existingAddresses[addressIndex] = updatedAddress

        val updatedJson = gson.toJson(existingAddresses)
        simulatedAddressesPrefs.edit().putString("addresses_$userId", updatedJson).apply()

        // Notify callback about address update
        onAddressesUpdated?.invoke(userId, existingAddresses)

        println("DEBUG: Address updated in local storage: ${updatedAddress.label}")

        return updatedAddress
    }

    private fun simulateDeleteAddress(userId: String, addressId: String): Boolean {
        val existingAddresses = getSimulatedAddresses(userId).toMutableList()
        val addressIndex = existingAddresses.indexOfFirst { it.id == addressId }

        if (addressIndex == -1) {
            throw Exception("Address not found")
        }

        existingAddresses.removeAt(addressIndex)

        val updatedJson = gson.toJson(existingAddresses)
        simulatedAddressesPrefs.edit().putString("addresses_$userId", updatedJson).apply()

        // Notify callback about address update
        onAddressesUpdated?.invoke(userId, existingAddresses)

        println("DEBUG: Address deleted from local storage")

        return true
    }

    private fun simulateSetDefaultAddress(userId: String, addressId: String): Boolean {
        val existingAddresses = getSimulatedAddresses(userId).toMutableList()
        val addressIndex = existingAddresses.indexOfFirst { it.id == addressId }

        if (addressIndex == -1) {
            throw Exception("Address not found")
        }

        // Remove default from all addresses
        existingAddresses.forEachIndexed { index, addr ->
            existingAddresses[index] = addr.copy(isDefault = index == addressIndex)
        }

        val updatedJson = gson.toJson(existingAddresses)
        simulatedAddressesPrefs.edit().putString("addresses_$userId", updatedJson).apply()

        // Notify callback about address update
        println("DEBUG: Calling address update callback for set default, user: $userId")
        onAddressesUpdated?.invoke(userId, existingAddresses)

        println("DEBUG: Default address set in local storage")

        return true
    }
}
