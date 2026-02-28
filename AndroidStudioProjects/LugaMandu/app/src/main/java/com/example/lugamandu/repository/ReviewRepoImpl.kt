package com.example.lugamandu.repository

import com.example.lugamandu.model.ReviewModel
import com.google.firebase.database.*

class ReviewRepoImpl : ReviewRepo {
    private val dbUrl = "https://lugamandu-9f7c4-default-rtdb.firebaseio.com"
    private val reviewsRef = FirebaseDatabase.getInstance(dbUrl).reference.child("reviews")

    override fun addReview(review: ReviewModel, callback: (Boolean, String) -> Unit) {
        val id = reviewsRef.push().key ?: return callback(false, "ID Error")
        review.reviewId = id
        reviewsRef.child(id).setValue(review).addOnCompleteListener {
            callback(it.isSuccessful, if (it.isSuccessful) "Review Added" else "Failed to post review")
        }
    }

    override fun getReviewsByProduct(productId: String, callback: (List<ReviewModel>) -> Unit) {
        reviewsRef.orderByChild("productId").equalTo(productId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(ReviewModel::class.java) }
                    callback(list)
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }

    override fun deleteReview(reviewId: String, callback: (Boolean) -> Unit) {
        reviewsRef.child(reviewId).removeValue().addOnCompleteListener {
            callback(it.isSuccessful)
        }
    }

    override fun getAllReviews(callback: (List<ReviewModel>) -> Unit) {
        reviewsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(ReviewModel::class.java) }
                callback(list)
            }
            override fun onCancelled(error: DatabaseError) { callback(emptyList()) }
        })
    }
}