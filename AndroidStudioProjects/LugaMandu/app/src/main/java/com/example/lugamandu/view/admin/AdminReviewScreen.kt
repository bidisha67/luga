package com.example.lugamandu.view.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lugamandu.model.ReviewModel
import com.example.lugamandu.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductReviewScreen(
    navController: NavController,
    productId: String,
    viewModel: ReviewViewModel
) {
    val reviews by viewModel.reviews.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // Fetch reviews when the screen opens
    LaunchedEffect(productId) {
        viewModel.fetchReviews(productId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Reviews") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (reviews.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No reviews for this product yet.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                items(reviews) { review ->
                    ReviewItem(
                        review = review,
                        onDelete = {
                            viewModel.deleteReview(review.reviewId) {
                                // Refresh list after delete
                                viewModel.fetchReviews(productId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: ReviewModel, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = review.userName, fontWeight = FontWeight.Bold)
                // Display Stars
                Text(text = "⭐".repeat(review.rating), color = Color(0xFFFFC107))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = review.comment)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}