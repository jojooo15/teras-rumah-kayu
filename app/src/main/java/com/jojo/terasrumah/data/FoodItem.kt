package com.jojo.terasrumah.data

data class FoodItem(
    val id: Int,
    val name: String,
    val price: Int,
    val rating: Double,
    val deliveryTime: String,
    val imageUrl: String,
    val fallbackImageUrl: String? = null,
    val category: FoodCategory,
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val notes: String = ""
)

enum class FoodCategory(val displayName: String) {
    ALL("Semua Menu"),
    PAKET_UTAMA("Paket Makanan Utama"),
    SAMBAL("Macam Sambal"),
    MAKANAN_RINGAN("Makanan Ringan & Dadakan"),
    MINUMAN("Minuman")
}
