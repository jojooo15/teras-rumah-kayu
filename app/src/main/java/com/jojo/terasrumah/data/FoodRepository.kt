package com.jojo.terasrumah.data

import android.content.Context
import com.jojo.terasrumah.data.api.ApiClient
import com.jojo.terasrumah.data.api.ApiResult
import com.jojo.terasrumah.data.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FoodRepository(private val context: Context) {

    private val apiService = ApiClient.foodApiService
    private val cacheMutex = Mutex()

    // Cache variables
    private var cachedFoodItems: List<FoodItem>? = null
    private var cacheTimestamp: Long = 0
    private val cacheValidityDuration = 0L // No cache - always fetch fresh data

    // Function to clear cache and force refresh
    suspend fun clearCache() {
        cacheMutex.withLock {
            cachedFoodItems = null
            cacheTimestamp = 0
        }
    }

    // Force refresh function to immediately get fresh data
    suspend fun forceRefreshAllFoodItems(): Flow<ApiResult<List<FoodItem>>> = flow {
        emit(ApiResult.Loading)

        // Clear cache first
        clearCache()

        try {
            // Force fresh API call
            val response = apiService.getAllFoodItemsLegacy()

            if (response.isSuccessful && response.body() != null) {
                val freshFoodItems = response.body()!!
                val updatedFoodItems = updateFoodRatingsWithReviews(freshFoodItems)

                // Update cache with fresh data
                cacheMutex.withLock {
                    cachedFoodItems = updatedFoodItems
                    cacheTimestamp = System.currentTimeMillis()
                }

                emit(ApiResult.Success(updatedFoodItems))
            } else {
                throw Exception("API Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            // Return fallback data on error
            val updatedFallbackItems = updateFoodRatingsWithReviews(fallbackFoodItems)
            emit(ApiResult.Success(updatedFallbackItems))
        }
    }

    // Fallback data for offline mode
    private val fallbackFoodItems = listOf(
        // PAKET MAKANAN UTAMA
        FoodItem(
            id = 1,
            name = "Paket Sop Iga",
            price = 40000,
            rating = 4.8,
            deliveryTime = "25m",
            imageUrl = "file:///android_asset/foto-menu/sop iga.png",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_wooden_house_logo",
            category = FoodCategory.PAKET_UTAMA,
            description = "Paket lengkap sop iga dengan nasi dan minuman",
            ingredients = listOf("Nasi Putih", "Iga Sapi", "Wortel", "Kentang", "Daun Bawang", "Seledri", "Bawang Merah", "Bawang Putih", "Jahe", "Garam", "Merica", "Es Teh Manis"),
            notes = "Paket hemat dan mengenyangkan"
        ),
        FoodItem(
            id = 2,
            name = "Paket Pecak Nila",
            price = 28000,
            rating = 4.6,
            deliveryTime = "20m",
            imageUrl = "file:///android_asset/foto-menu/pecak ikan.png",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.PAKET_UTAMA,
            description = "Paket pecak ikan nila dengan nasi dan minuman",
            ingredients = listOf("Nasi Putih", "Ikan Nila", "Cabai Merah", "Cabai Rawit", "Bawang Merah", "Bawang Putih", "Kemiri", "Kunyit", "Jahe", "Garam", "Gula", "Lalapan", "Es Teh Manis"),
            notes = "Ikan segar dengan bumbu pecak khas"
        ),
        FoodItem(
            id = 3,
            name = "Paket Pecak Mas",
            price = 28000,
            rating = 4.6,
            deliveryTime = "20m",
            imageUrl = "file:///android_asset/foto-menu/pecak ikan.png",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.PAKET_UTAMA,
            description = "Paket pecak ikan mas dengan nasi dan minuman",
            ingredients = listOf("Nasi Putih", "Ikan Mas", "Cabai Merah", "Cabai Rawit", "Bawang Merah", "Bawang Putih", "Kemiri", "Kunyit", "Jahe", "Garam", "Gula", "Lalapan", "Es Teh Manis"),
            notes = "Ikan mas segar dengan bumbu pecak"
        ),
        FoodItem(
            id = 4,
            name = "Paket Pecak Lele",
            price = 28000,
            rating = 4.5,
            deliveryTime = "20m",
            imageUrl = "file:///android_asset/foto-menu/pecak ikan.png",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.PAKET_UTAMA,
            description = "Paket pecak ikan lele dengan nasi dan minuman",
            ingredients = listOf("Nasi Putih", "Ikan Lele", "Cabai Merah", "Cabai Rawit", "Bawang Merah", "Bawang Putih", "Kemiri", "Kunyit", "Jahe", "Garam", "Gula", "Lalapan", "Es Teh Manis"),
            notes = "Lele crispy dengan bumbu pecak pedas"
        ),
        FoodItem(
            id = 5,
            name = "Paket Pecak Ayam",
            price = 28000,
            rating = 4.7,
            deliveryTime = "20m",
            imageUrl = "file:///android_asset/foto-menu/pecak ayam.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.PAKET_UTAMA,
            description = "Paket pecak ayam dengan nasi dan minuman",
            ingredients = listOf("Nasi Putih", "Ayam", "Cabai Merah", "Cabai Rawit", "Bawang Merah", "Bawang Putih", "Kemiri", "Kunyit", "Jahe", "Garam", "Gula", "Lalapan", "Es Teh Manis"),
            notes = "Ayam dengan bumbu pecak yang gurih"
        ),
        FoodItem(
            id = 6,
            name = "Paket Pecak Gurame (+Rp 10.000)",
            price = 38000,
            rating = 4.8,
            deliveryTime = "25m",
            imageUrl = "file:///android_asset/foto-menu/pecak ikan.png",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.PAKET_UTAMA,
            description = "Paket pecak ikan gurame premium dengan nasi dan minuman",
            ingredients = listOf("Nasi Putih", "Ikan Gurame", "Cabai Merah", "Cabai Rawit", "Bawang Merah", "Bawang Putih", "Kemiri", "Kunyit", "Jahe", "Garam", "Gula", "Lalapan", "Es Teh Manis"),
            notes = "Gurame segar dengan bumbu pecak istimewa"
        ),
        FoodItem(
            id = 7,
            name = "Paket Geprek",
            price = 23000,
            rating = 4.5,
            deliveryTime = "15m",
            imageUrl = "file:///android_asset/foto-menu/ayam geprek.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.PAKET_UTAMA,
            description = "Paket ayam geprek dengan nasi dan lalapan",
            ingredients = listOf("Nasi Putih", "Ayam Fillet", "Tepung Terigu", "Cabai Rawit", "Cabai Merah", "Bawang Putih", "Garam", "Gula", "Lalapan", "Timun", "Tomat"),
            notes = "Pilihan level kepedasan tersedia"
        ),

        FoodItem(
            id = 8,
            name = "Paket Bawal Dabu",
            price = 22000,
            rating = 4.6,
            deliveryTime = "20m",
            imageUrl = "file:///android_asset/foto-menu/bawal dabu.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.PAKET_UTAMA,
            description = "Paket ikan bawal dengan sambal dabu-dabu khas Manado",
            ingredients = listOf("Nasi Putih", "Ikan Bawal", "Cabai Rawit", "Tomat", "Bawang Merah", "Jeruk Nipis", "Garam", "Gula", "Lalapan", "Es Teh Manis"),
            notes = "Cita rasa khas Manado yang segar"
        ),
        FoodItem(
            id = 9,
            name = "Paket Empal",
            price = 30000,
            rating = 4.7,
            deliveryTime = "20m",
            imageUrl = "file:///android_asset/foto-menu/paket empal.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.PAKET_UTAMA,
            description = "Paket empal daging dengan nasi dan pelengkap",
            ingredients = listOf("Nasi Putih", "Daging Sapi", "Kelapa Parut", "Ketumbar", "Jinten", "Bawang Merah", "Bawang Putih", "Garam", "Gula Merah", "Tahu", "Tempe", "Lalapan", "Es Teh Manis"),
            notes = "Empal daging empuk dengan bumbu tradisional"
        ),
        FoodItem(
            id = 10,
            name = "Paket Ayam Bakar",
            price = 24000,
            rating = 4.8,
            deliveryTime = "25m",
            imageUrl = "file:///android_asset/foto-menu/ayam bakar.jpeg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.PAKET_UTAMA,
            description = "Paket ayam bakar dengan bumbu khas",
            ingredients = listOf("Nasi Putih", "Ayam", "Bawang Merah", "Bawang Putih", "Kemiri", "Kunyit", "Jahe", "Lengkuas", "Ketumbar", "Garam", "Gula Merah", "Lalapan", "Sambal", "Es Teh Manis"),
            notes = "Ayam bakar dengan bumbu meresap sempurna"
        ),

        // MACAM SAMBAL - Using local assets
        FoodItem(
            id = 11,
            name = "Sambal Dadak",
            price = 5000,
            rating = 4.3,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/sambal dadak.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_sambal_placeholder",
            category = FoodCategory.SAMBAL,
            description = "Sambal dadak pedas segar",
            ingredients = listOf("Cabai Merah", "Cabai Rawit", "Tomat", "Bawang Merah", "Bawang Putih", "Garam", "Gula", "Terasi"),
            notes = "Sambal segar dengan rasa pedas yang pas"
        ),
        FoodItem(
            id = 12,
            name = "Sambal Geprek",
            price = 5000,
            rating = 4.4,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/sambal geprek.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_sambal_placeholder",
            category = FoodCategory.SAMBAL,
            description = "Sambal geprek dengan cabai yang digeprek kasar",
            ingredients = listOf("Cabai Rawit", "Cabai Merah", "Bawang Putih", "Garam", "Gula", "Terasi"),
            notes = "Sambal geprek dengan tekstur kasar"
        ),
        FoodItem(
            id = 13,
            name = "Sambal Pecel",
            price = 5000,
            rating = 4.2,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/sambal pecel.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_sambal_placeholder",
            category = FoodCategory.SAMBAL,
            description = "Sambal pecel khas Jawa Timur",
            ingredients = listOf("Kacang Tanah", "Cabai Rawit", "Cabai Merah", "Gula Merah", "Asam Jawa", "Garam", "Kencur"),
            notes = "Sambal pecel manis pedas"
        ),
        FoodItem(
            id = 14,
            name = "Sambal Ijo",
            price = 5000,
            rating = 4.5,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/sambal ijo.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_sambal_placeholder",
            category = FoodCategory.SAMBAL,
            description = "Sambal hijau khas Padang",
            ingredients = listOf("Cabai Hijau Besar", "Cabai Rawit Hijau", "Bawang Merah", "Bawang Putih", "Kemiri", "Garam", "Gula", "Minyak"),
            notes = "Sambal hijau dengan rasa segar"
        ),
        FoodItem(
            id = 15,
            name = "Sambal Dabu",
            price = 5000,
            rating = 4.6,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/sambal dabu dabu.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_sambal_placeholder",
            category = FoodCategory.SAMBAL,
            description = "Sambal dabu-dabu khas Manado",
            ingredients = listOf("Cabai", "Tomat", "Bawang merah", "Jeruk nipis"),
            notes = "Sambal segar khas Manado"
        ),

        // MAKANAN RINGAN & DADAKAN
        FoodItem(
            id = 16,
            name = "Sarapan Sehat",
            price = 20000,
            rating = 4.4,
            deliveryTime = "15m",
            imageUrl = "file:///android_asset/foto-menu/sarapan sehat.png",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MAKANAN_RINGAN,
            description = "Menu sarapan sehat lengkap dengan berbagai sayuran dan protein",
            ingredients = listOf("Kentang Rebus", "Brokoli Rebus", "Dada Ayam Panggang", "Wortel Rebus", "Telur Rebus", "Saus Tomat", "Lada", "Garam"),
            notes = "Menu bergizi lengkap untuk memulai hari dengan energi"
        ),
        FoodItem(
            id = 17,
            name = "Cah Kangkung",
            price = 10000,
            rating = 4.2,
            deliveryTime = "10m",
            imageUrl = "file:///android_asset/foto-menu/cah kangkung.png",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MAKANAN_RINGAN,
            description = "Kangkung segar ditumis dengan bumbu sederhana",
            ingredients = listOf("Kangkung", "Bawang Putih", "Bawang Merah", "Cabai Merah", "Kecap Manis", "Garam", "Gula", "Minyak Goreng"),
            notes = "Sayuran segar dan sehat"
        ),
        FoodItem(
            id = 18,
            name = "Tempe Penyet",
            price = 10000,
            rating = 4.3,
            deliveryTime = "10m",
            imageUrl = "file:///android_asset/foto-menu/tempe penyet.png",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MAKANAN_RINGAN,
            description = "Tempe goreng yang dipenyet dengan sambal",
            ingredients = listOf("Tempe", "Cabai Rawit", "Cabai Merah", "Bawang Putih", "Tomat", "Garam", "Gula", "Terasi", "Lalapan"),
            notes = "Protein nabati yang lezat"
        ),
        FoodItem(
            id = 19,
            name = "Telor Barendo/Dadar/Ceplok",
            price = 5000,
            rating = 4.0,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/telur barendo.png",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MAKANAN_RINGAN,
            description = "Telur dengan berbagai pilihan masakan",
            ingredients = listOf("Telur", "Minyak goreng", "Garam"),
            notes = "Pilihan: barendo, dadar, atau ceplok"
        ),
        FoodItem(
            id = 20,
            name = "Tempe/Tahu 1 Porsi",
            price = 5000,
            rating = 4.1,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/tempe tahu.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MAKANAN_RINGAN,
            description = "Tempe atau tahu goreng 1 porsi",
            ingredients = listOf("Tempe/Tahu", "Minyak goreng"),
            notes = "Pilihan tempe atau tahu"
        ),
        FoodItem(
            id = 21,
            name = "Peda Selimut Jagung",
            price = 15000,
            rating = 4.5,
            deliveryTime = "15m",
            imageUrl = "file:///android_asset/foto-menu/peda selimut jagung.png",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MAKANAN_RINGAN,
            description = "Ikan peda dengan selimut jagung yang unik",
            ingredients = listOf("Ikan Peda", "Jagung Manis", "Tepung Terigu", "Telur", "Bawang Putih", "Merica", "Garam", "Minyak Goreng"),
            notes = "Menu khas dengan cita rasa istimewa"
        ),

        // MINUMAN
        FoodItem(
            id = 22,
            name = "Es Teh Tawar",
            price = 3000,
            rating = 4.0,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/es teh tawar.jpeg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MINUMAN,
            description = "Es teh tawar segar tanpa gula",
            ingredients = listOf("Teh", "Es batu"),
            notes = "Minuman sehat tanpa gula"
        ),
        FoodItem(
            id = 23,
            name = "Es Teh Manis",
            price = 5000,
            rating = 4.2,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/es teh manis.jpeg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MINUMAN,
            description = "Es teh manis yang menyegarkan",
            ingredients = listOf("Teh", "Gula", "Es batu"),
            notes = "Minuman favorit yang menyegarkan"
        ),
        FoodItem(
            id = 24,
            name = "Es Kopi (All Variant)",
            price = 7000,
            rating = 4.3,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/es kopi.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MINUMAN,
            description = "Es kopi dengan berbagai varian rasa",
            ingredients = listOf("Kopi", "Gula", "Es batu"),
            notes = "Tersedia berbagai varian kopi"
        ),
        FoodItem(
            id = 25,
            name = "Es Susu",
            price = 7000,
            rating = 4.1,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/es susu.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MINUMAN,
            description = "Es susu segar dan creamy",
            ingredients = listOf("Susu", "Gula", "Es batu"),
            notes = "Minuman susu yang creamy"
        ),
        FoodItem(
            id = 26,
            name = "Es Nutri Sari",
            price = 7000,
            rating = 4.0,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/es nutri sari.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MINUMAN,
            description = "Minuman sari buah yang menyegarkan",
            ingredients = listOf("Nutri Sari", "Air", "Es batu"),
            notes = "Minuman vitamin yang menyegarkan"
        ),
        FoodItem(
            id = 27,
            name = "Es Jeruk Peras",
            price = 10000,
            rating = 4.4,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/es jeruk peras.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MINUMAN,
            description = "Es jeruk peras segar langsung dari buah",
            ingredients = listOf("Jeruk segar", "Gula", "Es batu"),
            notes = "Jeruk segar diperas langsung"
        ),
        FoodItem(
            id = 28,
            name = "Teh Manis Panas",
            price = 3000,
            rating = 4.1,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/teh manis panas.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MINUMAN,
            description = "Teh manis hangat yang menghangatkan",
            ingredients = listOf("Teh", "Gula", "Air panas"),
            notes = "Minuman hangat untuk cuaca dingin"
        ),
        FoodItem(
            id = 29,
            name = "Susu Panas",
            price = 5000,
            rating = 4.0,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/susu panas.jpeg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MINUMAN,
            description = "Susu hangat yang menenangkan",
            ingredients = listOf("Susu", "Gula"),
            notes = "Minuman hangat yang menenangkan"
        ),
        FoodItem(
            id = 30,
            name = "Kopi",
            price = 5000,
            rating = 4.2,
            deliveryTime = "5m",
            imageUrl = "file:///android_asset/foto-menu/kopi panas.jpg",
            fallbackImageUrl = "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
            category = FoodCategory.MINUMAN,
            description = "Kopi panas klasik",
            ingredients = listOf("Kopi", "Gula", "Air panas"),
            notes = "Kopi panas untuk memulai hari"
        )
    )

    private fun isCacheValid(): Boolean {
        return cachedFoodItems != null &&
               (System.currentTimeMillis() - cacheTimestamp) < cacheValidityDuration
    }

    private fun updateFoodRatingsWithReviews(foodItems: List<FoodItem>): List<FoodItem> {
        val reviewRepository = ReviewRepository.getInstance(context)

        return foodItems.map { foodItem ->
            // Generate random reviews if none exist (first time)
            reviewRepository.generateRandomReviewsForFood(foodItem.id, foodItem.rating)

            // Get actual reviews and calculate real rating
            val reviews = reviewRepository.getSimulatedReviews(foodItem.id)
            val actualRating = if (reviews.isNotEmpty()) {
                val totalRating = reviews.sumOf { review -> review.rating }
                val calculatedRating = totalRating.toDouble() / reviews.size
                // Round to 1 decimal place
                kotlin.math.round(calculatedRating * 10.0) / 10.0
            } else {
                foodItem.rating // Keep original if no reviews
            }

            println("DEBUG: Food ${foodItem.name} - Original rating: ${foodItem.rating}, Calculated rating: $actualRating (${reviews.size} reviews)")

            // Update food item with calculated rating
            foodItem.copy(rating = actualRating)
        }
    }

    fun getAllFoodItems(): Flow<ApiResult<List<FoodItem>>> = flow {
        emit(ApiResult.Loading)

        try {
            cacheMutex.withLock {
                // Return cached data if valid, but always update ratings based on latest reviews
                if (isCacheValid()) {
                    val updatedCachedItems = updateFoodRatingsWithReviews(cachedFoodItems!!)
                    cachedFoodItems = updatedCachedItems
                    emit(ApiResult.Success(updatedCachedItems))
                    return@flow
                }
            }

            // Use legacy endpoints (compatible with MongoDB server)
            val response = apiService.getAllFoodItemsLegacy()

            if (response.isSuccessful && response.body() != null) {
                val foodItems = response.body()!!
                println("DEBUG: Successfully fetched ${foodItems.size} items from API")
                println("DEBUG: First item imageUrl: ${foodItems.firstOrNull()?.imageUrl}")

                // Update ratings based on reviews
                val updatedFoodItems = updateFoodRatingsWithReviews(foodItems)

                cacheMutex.withLock {
                    cachedFoodItems = updatedFoodItems
                    cacheTimestamp = System.currentTimeMillis()
                }

                emit(ApiResult.Success(updatedFoodItems))
            } else {
                throw Exception("API Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            println("DEBUG: API call failed, using fallback data: ${e.message}")
            // Return fallback data on error with updated ratings
            val updatedFallbackItems = updateFoodRatingsWithReviews(fallbackFoodItems)
            println("DEBUG: Using fallback data with ${updatedFallbackItems.size} items")
            println("DEBUG: First fallback item imageUrl: ${updatedFallbackItems.firstOrNull()?.imageUrl}")

            cacheMutex.withLock {
                cachedFoodItems = updatedFallbackItems
                cacheTimestamp = System.currentTimeMillis()
            }

            emit(ApiResult.Success(updatedFallbackItems))
        }
    }

    fun getFoodItemsByCategory(category: FoodCategory): Flow<ApiResult<List<FoodItem>>> = flow {
        emit(ApiResult.Loading)

        try {
            val categoryString = if (category == FoodCategory.ALL) "" else category.name
            val response = if (categoryString.isEmpty()) {
                apiService.getAllFoodItemsLegacy()
            } else {
                apiService.getFoodItemsByCategoryLegacy(categoryString)
            }

            if (response.isSuccessful && response.body() != null) {
                val foodItems = response.body()!!
                val updatedFoodItems = updateFoodRatingsWithReviews(foodItems)
                emit(ApiResult.Success(updatedFoodItems))
            } else {
                throw Exception("API Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            // Return filtered fallback data on error with updated ratings
            val filteredItems = if (category == FoodCategory.ALL) {
                fallbackFoodItems
            } else {
                fallbackFoodItems.filter { it.category == category }
            }
            val updatedFilteredItems = updateFoodRatingsWithReviews(filteredItems)
            emit(ApiResult.Success(updatedFilteredItems))
        }
    }

    fun getFoodItemById(id: Int): Flow<ApiResult<FoodItem?>> = flow {
        emit(ApiResult.Loading)

        try {
            val response = apiService.getFoodItemByIdLegacy(id)

            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()))
            } else {
                throw Exception("API Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            // Return fallback data on error
            val fallbackItem = fallbackFoodItems.find { it.id == id }
            emit(ApiResult.Success(fallbackItem))
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FoodRepository? = null

        fun getInstance(context: Context): FoodRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FoodRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        // For backward compatibility
        fun getInstance(): FoodRepository {
            return INSTANCE ?: throw IllegalStateException("FoodRepository must be initialized with context first")
        }
    }
}
