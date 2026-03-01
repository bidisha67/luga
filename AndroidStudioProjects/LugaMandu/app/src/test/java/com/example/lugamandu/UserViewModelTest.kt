package com.example.lugamandu

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.lugamandu.model.UserModel
import com.example.lugamandu.repository.UserRepo
import com.example.lugamandu.viewmodel.UserViewModel
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
class UserViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun login_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)
        val email = "test@example.com"
        val password = "password"

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Login successful")
            null
        }.`when`(repo).login(eq(email), eq(password), any())

        var successResult = false
        var messageResult = ""

        viewModel.login(email, password) { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Login successful", messageResult)
        verify(repo).login(eq(email), eq(password), any())
    }

    @Test
    fun register_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)
        val email = "test@example.com"
        val password = "password"

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(2)
            callback(true, "User registered", "u1")
            null
        }.`when`(repo).register(eq(email), eq(password), any())

        var successResult = false
        var messageResult = ""

        viewModel.register(email, password) { success, msg, userId ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("User registered", messageResult)
        verify(repo).register(eq(email), eq(password), any())
    }

    @Test
    fun updateProfile_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)
        val userId = "u1"
        // Corrected UserModel constructor: userId, email, firstName, lastName, dob, contact, role
        val userModel = UserModel(userId, "test@example.com", "First", "Last", "2000-01-01", "1234567890", "customer")

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Profile updated")
            null
        }.`when`(repo).updateProfile(eq(userId), eq(userModel), any())

        var successResult = false
        var messageResult = ""

        viewModel.updateProfile(userId, userModel) { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Profile updated", messageResult)
        verify(repo).updateProfile(eq(userId), eq(userModel), any())
    }
}
