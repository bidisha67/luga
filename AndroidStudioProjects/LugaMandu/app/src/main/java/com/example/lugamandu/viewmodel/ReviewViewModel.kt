package com.example.lugamandu.viewmodel

import androidx.lifecycle.ViewModel
import com.example.lugamandu.model.ReviewModel
import com.example.lugamandu.repository.OrderRepo
import com.example.lugamandu.repository.ReviewRepo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReviewViewModel(private val repo: ReviewRepo) : ViewModel() {
    private val _reviews = MutableStateFlow<List<ReviewModel>>(emptyList())
    val reviews = _reviews.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    // --- FIX: Added these missing variables ---
    private val _canReview = MutableStateFlow(false)
    val canReview = _canReview.asStateFlow()

    fun fetchReviews(productId: String) {
        _loading.value = true
        repo.getReviewsByProduct(productId) { list ->
            _reviews.value = list
            _loading.value = false
        }
    }

    fun fetchAllReviews() {
        _loading.value = true
        repo.getAllReviews { list ->
            _reviews.value = list
            _loading.value = false
        }
    }

    fun postReview(review: ReviewModel, onComplete: (Boolean, String) -> Unit) {
        _loading.value = true
        repo.addReview(review) { success, message ->
            _loading.value = false
            onComplete(success, message)
        }
    }

    fun deleteReview(reviewId: String, onComplete: (Boolean) -> Unit) {
        _loading.value = true
        repo.deleteReview(reviewId) { success ->
            _loading.value = false
            onComplete(success)
        }
    }

    fun checkIfUserCanReview(userId: String, productId: String, orderRepo: OrderRepo) {
        orderRepo.hasUserPurchasedProduct(userId, productId) { purchased ->
            _canReview.value = purchased
        }
    }

    fun getUserName(userId: String, callback: (String) -> Unit) {
        // Note: Use "Users" (Capital U) if your Firebase node is capitalized
        FirebaseDatabase.getInstance().reference.child("Users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").value?.toString()
                        ?: snapshot.child("email").value?.toString()
                        ?: "Anonymous"
                    callback(name)
                }
                override fun onCancelled(error: DatabaseError) {
                    callback("Anonymous")
                }
            })
    }
}