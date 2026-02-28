package com.example.lugamandu.view.customer

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lugamandu.model.CartModel
import com.example.lugamandu.model.ReviewModel
import com.example.lugamandu.repository.OrderRepoImpl
import com.example.lugamandu.ui.theme.Blue
import com.example.lugamandu.viewmodel.CartViewModel
import com.example.lugamandu.viewmodel.ProductViewModel
import com.example.lugamandu.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    reviewViewModel: ReviewViewModel,
    currentUserId: String
) {
    val selectedProduct by productViewModel.selectedProduct.collectAsState()
    val reviews by reviewViewModel.reviews.collectAsState()
    val canReview by reviewViewModel.canReview.collectAsState() // Observe eligibility
    val context = LocalContext.current

    var showReviewDialog by remember { mutableStateOf(false) }

    // 1. Fetch data on load
    LaunchedEffect(selectedProduct?.id) {
        selectedProduct?.id?.let { pid ->
            reviewViewModel.fetchReviews(pid)
            // Verify if this specific user has purchased this product
            reviewViewModel.checkIfUserCanReview(currentUserId, pid, OrderRepoImpl())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedProduct?.name ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        selectedProduct?.let { product ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl.takeIf { it.isNotBlank() } ?: "https://via.placeholder.com/300",
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(product.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Rs ${product.price}", style = MaterialTheme.typography.titleLarge, color = Blue)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Description", fontWeight = FontWeight.SemiBold)
                Text(product.description, color = Color.DarkGray)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val cartItem = CartModel(productId = product.id, quantity = 1)
                        cartViewModel.addToCart(currentUserId, cartItem) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue)
                ) {
                    Text("Add to Cart", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // --- REVIEWS HEADER ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Customer Reviews", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                    // 2. Only show "Write a Review" button to buyers
                    if (canReview) {
                        TextButton(onClick = { showReviewDialog = true }) {
                            Text("Write a Review", color = Blue)
                        }
                    }
                }

                if (!canReview) {
                    Text(
                        "Only verified buyers can leave a review.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Reviews list (Visible to everyone)
                if (reviews.isEmpty()) {
                    Text("No reviews yet.", color = Color.Gray)
                } else {
                    reviews.forEach { ReviewItem(it) }
                }
            }
        }
    }

    if (showReviewDialog) {
        AddReviewDialog(
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment ->
                // 4. Fetch the real username/email before posting
                reviewViewModel.getUserName(currentUserId) { fetchedName ->
                    val newReview = ReviewModel(
                        productId = selectedProduct?.id ?: "",
                        userId = currentUserId,
                        userName = fetchedName, // REAL IDENTITY
                        rating = rating,
                        comment = comment
                    )
                    reviewViewModel.postReview(newReview) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) showReviewDialog = false
                    }
                }
            }
        )
    }
}

@Composable
fun ReviewItem(review: ReviewModel) {
    Column(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(review.userName, fontWeight = FontWeight.Bold) // Displays the fetched name
            Spacer(modifier = Modifier.width(8.dp))
            Row {
                repeat(review.rating) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                }
            }
        }
        Text(review.comment, style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
    }
}

@Composable
fun AddReviewDialog(onDismiss: () -> Unit, onSubmit: (Int, String) -> Unit) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Write a Review") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < rating) Color(0xFFFFC107) else Color.LightGray
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("How was your experience?") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(rating, comment) }, colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}