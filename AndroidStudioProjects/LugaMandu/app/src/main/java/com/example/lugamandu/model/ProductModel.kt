package com.example.lugamandu.model

data class ProductModel(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var imageUrl: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "description" to description,
        "price" to price,
        "imageUrl" to imageUrl
    )
}