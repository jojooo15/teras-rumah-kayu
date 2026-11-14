package com.jojo.terasrumah.data

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

data class CartItem(
    val foodItem: FoodItem,
    val quantity: Int,
    val specialNotes: String = ""
)

object CartRepository {
    private val _cartItems = mutableStateOf<List<CartItem>>(emptyList())
    val cartItems: State<List<CartItem>> = _cartItems
    
    private val _cartCount = mutableStateOf(0)
    val cartCount: State<Int> = _cartCount
    
    fun addToCart(foodItem: FoodItem, specialNotes: String = "") {
        val currentItems = _cartItems.value.toMutableList()
        val existingItemIndex = currentItems.indexOfFirst {
            it.foodItem.id == foodItem.id && it.specialNotes == specialNotes
        }

        if (existingItemIndex >= 0) {
            // Item with same notes already exists, increase quantity
            val existingItem = currentItems[existingItemIndex]
            currentItems[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            // New item or item with different notes, add to cart
            currentItems.add(CartItem(foodItem, 1, specialNotes))
        }

        _cartItems.value = currentItems
        updateCartCount()
    }
    
    fun removeFromCart(foodItemId: Int) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItemIndex = currentItems.indexOfFirst { it.foodItem.id == foodItemId }
        
        if (existingItemIndex >= 0) {
            val existingItem = currentItems[existingItemIndex]
            if (existingItem.quantity > 1) {
                // Decrease quantity
                currentItems[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity - 1)
            } else {
                // Remove item completely
                currentItems.removeAt(existingItemIndex)
            }
        }
        
        _cartItems.value = currentItems
        updateCartCount()
    }
    
    fun removeItemCompletely(foodItemId: Int) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.removeAll { it.foodItem.id == foodItemId }
        _cartItems.value = currentItems
        updateCartCount()
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _cartCount.value = 0
    }
    
    private fun updateCartCount() {
        _cartCount.value = _cartItems.value.sumOf { it.quantity }
    }
    
    fun getTotalPrice(): Int {
        return _cartItems.value.sumOf { it.foodItem.price * it.quantity }
    }
}
