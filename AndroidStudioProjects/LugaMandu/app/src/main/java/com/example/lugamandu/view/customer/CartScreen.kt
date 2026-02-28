package com.example.lugamandu.view.customer

// Imports
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lugamandu.model.CartModel
import com.example.lugamandu.model.OrderModel
import com.example.lugamandu.model.ProductModel
import com.example.lugamandu.ui.theme.Blue
import com.example.lugamandu.viewmodel.CartViewModel
import com.example.lugamandu.viewmodel.OrderViewModel
import com.example.lugamandu.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel,
    currentUserId: String
) {
    // Fetch user's cart on first composition
    LaunchedEffect(Unit) {
        cartViewModel.fetchCart(currentUserId)
    }

    // Collect cart items and loading state
    val cartItems by cartViewModel.cartItems.collectAsState()
    val loading by cartViewModel.loading.collectAsState()
    val products by productViewModel.products.collectAsState()
    val context = LocalContext.current

    // Compute total amount of items in cart
    var totalAmount by remember { mutableStateOf(0.0) }
    LaunchedEffect(cartItems, products) {
        totalAmount = cartItems.sumOf { item ->
            val product = products.find { it.id == item.productId }
            (product?.price ?: 0.0) * item.quantity
        }
    }

    Scaffold(
        topBar = {
            // Top App Bar with back button
            TopAppBar(
                title = { Text("Your Cart") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // Show total and checkout button if cart has items
            if (cartItems.isNotEmpty()) {
                Surface(color = Color.White, shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Total amount
                        Column {
                            Text("Total:", color = Color.Gray)
                            Text("Rs $totalAmount", fontWeight = FontWeight.Bold, color = Blue)
                        }
                        // Checkout button
                        Button(
                            onClick = {
                                val order = OrderModel(items = cartItems, totalAmount = totalAmount)
                                orderViewModel.placeOrder(currentUserId, order) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        // Clear cart after successful checkout
                                        cartViewModel.clearCart(currentUserId) { _, _ -> }
                                        navController.popBackStack()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Blue)
                        ) {
                            Text("Checkout", color = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        // Main content area
        when {
            loading -> {
                // Show loading spinner while fetching cart
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            cartItems.isEmpty() -> {
                // Show empty cart message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Your cart is empty.")
                }
            }
            else -> {
                // Show list of cart items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    items(cartItems) { item ->
                        val product = products.find { it.id == item.productId }
                        if (product != null) {
                            // Display each cart item row
                            CartItemRow(item, product) {
                                cartViewModel.removeFromCart(currentUserId, item.cartItemId) { _, _ -> }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------- Single Cart Item Row -------------------
@Composable
fun CartItemRow(cartItem: CartModel, product: ProductModel, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image with placeholder if missing
            AsyncImage(
                model = product.imageUrl.takeIf { it.isNotBlank() } ?: "https://via.placeholder.com/150",
                contentDescription = "Product Image",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Product name and quantity
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text("Rs ${product.price} x ${cartItem.quantity}", color = Color.Gray)
            }
            // Remove item button
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, "Remove", tint = Color.Red)
            }
        }
    }
}