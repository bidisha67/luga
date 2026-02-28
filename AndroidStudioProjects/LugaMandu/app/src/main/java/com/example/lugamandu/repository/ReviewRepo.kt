package com.example.lugamandu.repository

import com.example.lugamandu.model.ReviewModel

interface ReviewRepo {
    fun addReview(review: ReviewModel, callback: (Boolean, String) -> Unit)
    fun getReviewsByProduct(productId: String, callback: (List<ReviewModel>) -> Unit)
    fun deleteReview(reviewId: String, callback: (Boolean) -> Unit)
    fun getAllReviews(callback: (List<ReviewModel>) -> Unit)
}