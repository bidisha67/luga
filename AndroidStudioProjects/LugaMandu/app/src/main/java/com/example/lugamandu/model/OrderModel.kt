package com.example.lugamandu.model

data class OrderModel(
    var orderId: String = "",
    var userId: String = "",
    var items: List<CartModel> = emptyList(),
    var status: String = "Pending",
    var totalAmount: Double = 0.0,
    var timestamp: Long = System.currentTimeMillis()
) {
    // Helper function to convert to map for Firebase
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "orderId" to orderId,
            "userId" to userId,
            "items" to items,
            "status" to status,
            "totalAmount" to totalAmount,
            "timestamp" to timestamp
        )
    }
}