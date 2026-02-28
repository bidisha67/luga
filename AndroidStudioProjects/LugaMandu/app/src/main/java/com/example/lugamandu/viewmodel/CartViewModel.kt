package com.example.lugamandu.viewmodel

import androidx.lifecycle.ViewModel
import com.example.lugamandu.model.CartModel
import com.example.lugamandu.repository.CartRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartViewModel(private val cartRepo: CartRepo) : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartModel>>(emptyList())
    val cartItems: StateFlow<List<CartModel>> = _cartItems.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun fetchCart(userId: String) {
        _loading.value = true
        cartRepo.getCartItems(userId) { success, list, message ->
            _loading.value = false
            if (success) {
                _cartItems.value = list
            }
        }
    }

    fun addToCart(userId: String, cartItem: CartModel, callback: (Boolean, String) -> Unit) {
        cartRepo.addToCart(userId, cartItem, callback)
    }

    fun removeFromCart(userId: String, cartItemId: String, callback: (Boolean, String) -> Unit) {
        cartRepo.removeFromCart(userId, cartItemId, callback)
    }

    fun clearCart(userId: String, callback: (Boolean, String) -> Unit) {
        cartRepo.clearCart(userId, callback)
    }
}
