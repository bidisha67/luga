package com.example.lugamandu.model

data class ReviewModel(
    var reviewId: String = "",
    var productId: String = "",
    var userId: String = "",
    var userName: String = "Anonymous",
    var rating: Int = 5,
    var comment: String = "",
    var timestamp: Long = System.currentTimeMillis()
)