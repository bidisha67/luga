package com.example.lugamandu.repository

import com.example.lugamandu.model.ProductModel
import com.example.lugamandu.model.CartModel
import com.example.lugamandu.model.OrderModel

interface CartRepo {
    fun addToCart(userId: String, cartItem: CartModel, callback: (Boolean, String) -> Unit)
    fun removeFromCart(userId: String, cartItemId: String, callback: (Boolean, String) -> Unit)
    fun getCartItems(userId: String, callback: (Boolean, List<CartModel>, String) -> Unit)
    fun clearCart(userId: String, callback: (Boolean, String) -> Unit)
}
interface OrderRepo {
    fun placeOrder(userId: String, order: OrderModel, callback: (Boolean, String) -> Unit)
    fun getAllOrders(callback: (Boolean, List<OrderModel>, String) -> Unit)
    fun getUserOrders(userId: String, callback: (Boolean, List<OrderModel>, String) -> Unit)
    fun updateOrderStatus(orderId: String, newStatus: String, callback: (Boolean, String) -> Unit)
    fun hasUserPurchasedProduct(userId: String, productId: String, callback: (Boolean) -> Unit)}
