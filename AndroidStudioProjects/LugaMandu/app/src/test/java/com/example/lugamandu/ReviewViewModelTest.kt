package com.example.lugamandu

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.lugamandu.model.ReviewModel
import com.example.lugamandu.repository.ReviewRepo
import com.example.lugamandu.viewmodel.ReviewViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun postReview_success_test() {
        val repo = mock<ReviewRepo>()
        val viewModel = ReviewViewModel(repo)
        
        // Corrected ReviewModel constructor: reviewId, productId, userId, userName, rating, comment, timestamp
        val testReview = ReviewModel(
            reviewId = "r1",
            productId = "p1",
            userId = "u1",
            userName = "Anonymous",
            rating = 5,
            comment = "Great!",
            timestamp = System.currentTimeMillis()
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Review posted")
            null
        }.`when`(repo).addReview(eq(testReview), any())

        var successResult = false
        var messageResult = ""

        viewModel.postReview(testReview) { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Review posted", messageResult)
        verify(repo).addReview(eq(testReview), any())
    }

    @Test
    fun deleteReview_success_test() {
        val repo = mock<ReviewRepo>()
        val viewModel = ReviewViewModel(repo)
        val reviewId = "r1"

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean) -> Unit>(1)
            callback(true)
            null
        }.`when`(repo).deleteReview(eq(reviewId), any())

        var successResult = false

        viewModel.deleteReview(reviewId) { success ->
            successResult = success
        }

        assertTrue(successResult)
        verify(repo).deleteReview(eq(reviewId), any())
    }

    @Test
    fun fetchReviews_success_test() {
        val repo = mock<ReviewRepo>()
        val viewModel = ReviewViewModel(repo)
        val productId = "p1"
        val reviewList = listOf(ReviewModel("r1", productId, "u1", "Anonymous", 4, "good", 0L))

        doAnswer { invocation ->
            val callback = invocation.getArgument<(List<ReviewModel>) -> Unit>(1)
            callback(reviewList)
            null
        }.`when`(repo).getReviewsByProduct(eq(productId), any())

        viewModel.fetchReviews(productId)

        assertEquals(reviewList, viewModel.reviews.value)
    }
}
