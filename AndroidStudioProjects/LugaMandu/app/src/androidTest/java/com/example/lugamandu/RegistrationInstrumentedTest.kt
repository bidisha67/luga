package com.example.lugamandu

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.lugamandu.view.RegisterBody
import org.junit.Rule
import org.junit.Test

class RegistrationInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testRegistrationUI() {
        composeRule.setContent {
            RegisterBody()
        }
        
        composeRule.onNodeWithTag("emailInput").assertExists()
        composeRule.onNodeWithTag("usernameInput").assertExists()
        composeRule.onNodeWithTag("passwordInput").assertExists()
        composeRule.onNodeWithTag("confirmPasswordInput").assertExists()
        composeRule.onNodeWithTag("termsCheckbox").assertExists()
        composeRule.onNodeWithTag("signUpButton").assertExists()
    }

    @Test
    fun testRegistrationInput() {
        composeRule.setContent {
            RegisterBody()
        }
        
        composeRule.onNodeWithTag("emailInput").performTextInput("testuser@example.com")
        composeRule.onNodeWithTag("usernameInput").performTextInput("testuser")
        composeRule.onNodeWithTag("passwordInput").performTextInput("password123")
        composeRule.onNodeWithTag("confirmPasswordInput").performTextInput("password123")
        
        composeRule.onNodeWithTag("termsCheckbox").performClick()
        composeRule.onNodeWithTag("signUpButton").performClick()
    }
}
