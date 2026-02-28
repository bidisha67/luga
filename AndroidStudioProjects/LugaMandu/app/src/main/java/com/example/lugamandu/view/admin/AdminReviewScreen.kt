package com.example.lugamandu.view.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lugamandu.model.ReviewModel
import com.example.lugamandu.viewmodel.ReviewViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReviewScreen(
    navController: NavController,
    reviewViewModel: ReviewViewModel
) {
    val allReviews by reviewViewModel.reviews.collectAsState()
    val loading by reviewViewModel.loading.collectAsState()
    val context = LocalContext.current

    // Dialog State
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<ReviewModel?>(null) }

    LaunchedEffect(Unit) {
        reviewViewModel.fetchAllReviews()
    }

    // --- DIALOG SECTION ---
    // This must be outside the Scaffold or LazyColumn to render properly
    if (showDeleteDialog && reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                reviewToDelete = null
            },
            title = { Text("Delete Review") },
            text = {
                Text("Are you sure you want to delete the review by ${reviewToDelete?.userName}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        reviewToDelete?.let { review ->
                            reviewViewModel.deleteReview(review.reviewId) { success ->
                                if (success) {
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                    reviewViewModel.fetchAllReviews()
                                }
                                showDeleteDialog = false
                                reviewToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    reviewToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Reviews") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue)
            }
        } else if (allReviews.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No reviews found.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                items(allReviews) { review ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(review.userName, fontWeight = FontWeight.Bold)

                                // FIX: This button ONLY triggers the dialog state
                                IconButton(onClick = {
                                    reviewToDelete = review
                                    showDeleteDialog = true
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }

                            Row {
                                repeat(5) { index ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (index < review.rating) Color(0xFFFFC107) else Color.LightGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(review.comment)
                            Text("Product ID: ${review.productId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}