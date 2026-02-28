package com.example.lugamandu.model

data class CartModel(
    var cartItemId: String = "",
    var productId: String = "",
    var quantity: Int = 1
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "cartItemId" to cartItemId,
            "productId" to productId,
            "quantity" to quantity
        )
    }
}
