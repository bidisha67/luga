package com.example.lugamandu

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.lugamandu.repository.ProductRepoImpl
import com.example.lugamandu.view.admin.AddProductScreen
import com.example.lugamandu.viewmodel.ProductViewModel
import org.junit.Rule
import org.junit.Test

class AddProductInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testAddProductUI() {
        composeRule.setContent {
            val navController = rememberNavController()
            val viewModel = ProductViewModel(ProductRepoImpl())
            AddProductScreen(navController = navController, viewModel = viewModel)
        }
        
        composeRule.onNodeWithTag("productNameInput").assertExists()
        composeRule.onNodeWithTag("productPriceInput").assertExists()
        composeRule.onNodeWithTag("productDescInput").assertExists()
        composeRule.onNodeWithTag("publishProductButton").assertExists()
    }

    @Test
    fun testAddProductInput() {
        composeRule.setContent {
            val navController = rememberNavController()
            val viewModel = ProductViewModel(ProductRepoImpl())
            AddProductScreen(navController = navController, viewModel = viewModel)
        }
        
        composeRule.onNodeWithTag("productNameInput").performTextInput("New T-Shirt")
        composeRule.onNodeWithTag("productPriceInput").performTextInput("1500")
        composeRule.onNodeWithTag("productDescInput").performTextInput("High quality cotton t-shirt")
        
        // Use scrollTo if needed, but for simple tests performClick should work if visible
        composeRule.onNodeWithTag("publishProductButton").performClick()
    }
}
