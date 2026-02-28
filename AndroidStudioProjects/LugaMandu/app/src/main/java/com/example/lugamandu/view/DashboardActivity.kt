package com.example.lugamandu.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cloudinary.android.MediaManager
import com.example.lugamandu.model.UserModel
import com.example.lugamandu.repository.*
import com.example.lugamandu.view.admin.AddProductScreen
import com.example.lugamandu.view.admin.AdminDashboardScreen
import com.example.lugamandu.view.customer.CartScreen
import com.example.lugamandu.view.customer.CustomerDashboardScreen
import com.example.lugamandu.view.customer.ProductDetailScreen
import com.example.lugamandu.view.ui.theme.LugaManduTheme
import com.example.lugamandu.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initializeCloudinary()

        // 1. Initialize Repositories
        val productRepo = ProductRepoImpl()
        val cartRepo = CartRepoImpl()
        val orderRepo = OrderRepoImpl()
        val reviewRepo = ReviewRepoImpl()

        // 2. Initialize ViewModels
        val productViewModel = ProductViewModel(productRepo)
        val cartViewModel = CartViewModel(cartRepo)
        val orderViewModel = OrderViewModel(orderRepo)
        val reviewViewModel = ReviewViewModel(reviewRepo)

        setContent {
            LugaManduTheme {
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                var role by remember { mutableStateOf<String?>(null) }
                var loading by remember { mutableStateOf(true) }

                LaunchedEffect(currentUser) {
                    if (currentUser != null) {
                        if (currentUser.email == "admin@lugamandu.com") {
                            role = "admin"
                            loading = false
                        } else {
                            val ref = FirebaseDatabase.getInstance().reference
                                .child("users")
                                .child(currentUser.uid)

                            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val user = snapshot.getValue(UserModel::class.java)
                                    role = user?.role ?: "customer"
                                    loading = false
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    role = "customer"
                                    loading = false
                                }
                            })
                        }
                    } else { loading = false }
                }

                if (loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (currentUser == null) {
                    LaunchedEffect(Unit) {
                        startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                        finish()
                    }
                } else {
                    AppNavigation(
                        role = role ?: "customer",
                        currentUserId = currentUser.uid,
                        productViewModel = productViewModel,
                        cartViewModel = cartViewModel,
                        orderViewModel = orderViewModel,
                        reviewViewModel = reviewViewModel
                    )
                }
            }
        }
    }

    private fun initializeCloudinary() {
        val config = mapOf(
            "cloud_name" to "dwfc51vqa",
            "api_key" to "433791988788857",
            "api_secret" to "u9Qgd5h0Y-hmyxPFO1hsp01swEI"
        )
        try { MediaManager.init(this, config) } catch (e: Exception) {}
    }
}

@Composable
fun AppNavigation(
    role: String,
    currentUserId: String,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    reviewViewModel: ReviewViewModel
) {
    val navController = rememberNavController()
    val startDestination = if (role == "admin") "admin_dashboard" else "customer_dashboard"

    NavHost(navController = navController, startDestination = startDestination) {

        // --- ADMIN ROUTES ---
        composable("admin_dashboard") {
            AdminDashboardScreen(
                navController = navController,
                productViewModel = productViewModel,
                orderViewModel = orderViewModel,
                reviewViewModel = reviewViewModel // CORRECTED: Now passed to Admin UI
            )
        }

        composable("add_product") {
            AddProductScreen(navController = navController, viewModel = productViewModel)
        }

        // --- CUSTOMER ROUTES ---
        composable("customer_dashboard") {
            CustomerDashboardScreen(navController, productViewModel)
        }

        composable("product_detail") {
            ProductDetailScreen(
                navController = navController,
                productViewModel = productViewModel,
                cartViewModel = cartViewModel,
                reviewViewModel = reviewViewModel,
                currentUserId = currentUserId
            )
        }

        composable("cart") {
            CartScreen(navController, cartViewModel, productViewModel, orderViewModel, currentUserId)
        }

        composable("login") {
            val context = androidx.compose.ui.platform.LocalContext.current
            LaunchedEffect(Unit) {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
        }
    }
}