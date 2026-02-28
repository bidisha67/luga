package com.example.lugamandu.view.admin

// Imports
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lugamandu.model.OrderModel
import com.example.lugamandu.model.ProductModel
import com.example.lugamandu.model.ReviewModel
import com.example.lugamandu.viewmodel.OrderViewModel
import com.example.lugamandu.viewmodel.ProductViewModel
import com.example.lugamandu.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat
import java.util.*

// Custom colors
val Blue = Color(0xFF2196F3)
val LightBlue = Color(0xFFE3F2FD)
val SoftGray = Color(0xFFF5F7FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel,
    reviewViewModel: ReviewViewModel
) {
    // Track selected tab index
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Products", "Orders", "Reviews")

    // Scaffold layout with top bar and floating action button
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "LUGA MANDU ADMIN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Blue),
                actions = {
                    // Logout button
                    IconButton(onClick = {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") { popUpTo(0) }
                    }) {
                        Icon(Icons.Filled.ExitToApp, "Logout", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            // Only show FAB on Products tab
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        productViewModel.selectProduct(null)
                        navController.navigate("add_product")
                    },
                    containerColor = Blue,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Filled.Add, "Add") },
                    text = { Text("New Product") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(SoftGray)
        ) {
            // Tab row for Products, Orders, Reviews
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Blue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Blue,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Display content based on selected tab
            when (selectedTab) {
                0 -> AdminProductsScreen(navController, productViewModel)
                1 -> AdminOrdersScreen(orderViewModel)
                2 -> AdminReviewScreen(reviewViewModel)
            }
        }
    }
}

// ------------------- Reviews Screen -------------------
@Composable
fun AdminReviewScreen(viewModel: ReviewViewModel) {
    val reviews by viewModel.reviews.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val context = LocalContext.current

    // Fetch reviews on first composition
    LaunchedEffect(Unit) { viewModel.fetchAllReviews() }

    // Variables for delete dialog
    var showDeleteReviewDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<ReviewModel?>(null) }

    // Delete review dialog
    if (showDeleteReviewDialog && reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteReviewDialog = false },
            title = { Text("Purge Feedback") },
            text = { Text("Remove the review from ${reviewToDelete?.userName} permanently?") },
            confirmButton = {
                Button(
                    onClick = {
                        reviewToDelete?.let { review ->
                            viewModel.deleteReview(review.reviewId) { success ->
                                if (success) Toast.makeText(context, "Review Removed", Toast.LENGTH_SHORT).show()
                                viewModel.fetchAllReviews()
                            }
                        }
                        showDeleteReviewDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteReviewDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Show loading indicator
    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Blue)
        }
    } else {
        // Display list of reviews
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            items(reviews) { review ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // User avatar circle with first letter
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(LightBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(review.userName.take(1).uppercase(), color = Blue, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(review.userName, fontWeight = FontWeight.Bold)
                                // Display stars for rating
                                Row {
                                    repeat(5) { index ->
                                        Icon(
                                            Icons.Filled.Star, null,
                                            tint = if (index < review.rating) Color(0xFFFFC107) else Color.LightGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            // Delete button
                            IconButton(
                                onClick = { reviewToDelete = review; showDeleteReviewDialog = true },
                                modifier = Modifier.background(Color(0xFFFFEBEE), CircleShape).size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // Comment box
                        Surface(color = SoftGray, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "\"${review.comment}\"",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Product ID
                        Text("PID: ${review.productId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// ------------------- Products Screen -------------------
@Composable
fun AdminProductsScreen(navController: NavController, viewModel: ProductViewModel) {
    val products by viewModel.products.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val context = LocalContext.current

    var showDeleteProductDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductModel?>(null) }

    // Delete product dialog
    if (showDeleteProductDialog && productToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteProductDialog = false },
            title = { Text("Delete Product") },
            text = { Text("Confirm removal of '${productToDelete?.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        productToDelete?.let { product ->
                            viewModel.deleteProduct(product.id) { success ->
                                if(success) Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showDeleteProductDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { showDeleteProductDialog = false }) { Text("Cancel") } }
        )
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Blue)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            items(products) { product ->
                ProductAdminItem(
                    product = product,
                    onEdit = {
                        viewModel.selectProduct(product)
                        navController.navigate("add_product")
                    },
                    onDelete = {
                        productToDelete = product
                        showDeleteProductDialog = true
                    }
                )
            }
        }
    }
}

// ------------------- Product Item -------------------
@Composable
fun ProductAdminItem(product: ProductModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Product image
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text("Rs ${product.price}", color = Blue, fontWeight = FontWeight.Bold)
                Text("ID: ${product.id.take(8)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            // Edit button
            IconButton(onClick = onEdit, modifier = Modifier.background(LightBlue, CircleShape).size(36.dp)) {
                Icon(Icons.Filled.Edit, null, tint = Blue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Delete button
            IconButton(onClick = onDelete, modifier = Modifier.background(Color(0xFFFFEBEE), CircleShape).size(36.dp)) {
                Icon(Icons.Filled.Delete, null, tint = Color.Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ------------------- Orders Screen -------------------
@Composable
fun AdminOrdersScreen(viewModel: OrderViewModel) {
    LaunchedEffect(Unit) { viewModel.fetchAllOrders() }
    val orders by viewModel.orders.collectAsState()
    val loading by viewModel.loading.collectAsState()

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Blue)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Pending tasks count
            Surface(color = LightBlue, shape = RoundedCornerShape(12.dp)) {
                Text(
                    "PENDING TASKS: ${orders.filter { it.status.lowercase() == "pending" }.size}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Blue
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Display all orders
            LazyColumn {
                items(orders) { order -> OrderAdminItem(order, viewModel) }
            }
        }
    }
}

// ------------------- Single Order Item -------------------
@Composable
fun OrderAdminItem(order: OrderModel, viewModel: OrderViewModel) {
    val context = LocalContext.current
    val isPending = order.status.lowercase() == "pending"

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        border = if(isPending) androidx.compose.foundation.BorderStroke(1.dp, Blue.copy(0.3f)) else null,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order #${order.orderId.takeLast(6)}", fontWeight = FontWeight.Bold)
                Surface(
                    color = if(isPending) Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        order.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if(isPending) Color(0xFFE65100) else Color(0xFF2E7D32)
                    )
                }
            }
            Text("Total: Rs ${order.totalAmount}", color = Blue, fontWeight = FontWeight.Bold, fontSize = 18.sp)

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = Color.Gray.copy(alpha = 0.1f)
            )

            // Items in order
            order.items.forEach { item ->
                Text("• ${item.productId} (x${item.quantity})", style = MaterialTheme.typography.bodySmall)
            }

            // Order timestamp
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            Text(sdf.format(Date(order.timestamp)), color = Color.Gray, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp))

            // Button to mark order as complete
            if (isPending) {
                Button(
                    onClick = {
                        viewModel.updateOrderStatus(order.orderId, "Complete") { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue)
                ) {
                    Text("Verify & Complete")
                }
            }
        }
    }
}