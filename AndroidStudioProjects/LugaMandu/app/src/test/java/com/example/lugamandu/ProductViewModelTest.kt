package com.example.lugamandu

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.lugamandu.model.ProductModel
import com.example.lugamandu.repository.ProductRepo
import com.example.lugamandu.viewmodel.ProductViewModel
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
class ProductViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun deleteProduct_success_test() {
        val repo = mock<ProductRepo>()
        val viewModel = ProductViewModel(repo)
        val productId = "p1"

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean) -> Unit>(1)
            callback(true)
            null
        }.`when`(repo).deleteProduct(eq(productId), any())

        var successResult = false
        viewModel.deleteProduct(productId) { success ->
            successResult = success
        }

        assertTrue(successResult)
        verify(repo).deleteProduct(eq(productId), any())
    }

    @Test
    fun fetchAllProducts_success_test() {
        val repo = mock<ProductRepo>()
        val viewModel = ProductViewModel(repo)
        val productList = listOf(ProductModel("1", "P1", "good", 10.0, "U1"))

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, List<ProductModel>, String) -> Unit>(0)
            callback(true, productList, "Success")
            null
        }.`when`(repo).getAllProducts(any())

        viewModel.fetchAllProducts()

        assertEquals(productList, viewModel.products.value)
    }

    @Test
    fun addProduct_success_test() {
        val repo = mock<ProductRepo>()
        val viewModel = ProductViewModel(repo)
        
        doAnswer { invocation ->
            val callback = invocation.getArgument<(String?) -> Unit>(2)
            callback("http://image.url")
            null
        }.`when`(repo).uploadImage(any(), any(), any())

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Success")
            null
        }.`when`(repo).addProduct(any(), any())

        var successResult = false
        viewModel.uploadAndSave(mock<android.content.Context>(), mock<android.net.Uri>(), "Name", 10.0, "Desc") { success ->
            successResult = success
        }
        
        assertTrue(successResult)
        verify(repo).addProduct(any(), any())
    }

    @Test
    fun updateProduct_success_test() {
        val repo = mock<ProductRepo>()
        val viewModel = ProductViewModel(repo)
        val existingProduct = ProductModel("p1", "Old", "Old", 5.0, "Url")
        
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Success")
            null
        }.`when`(repo).updateProduct(any(), any())

        viewModel.selectProduct(existingProduct)
        
        var successResult = false
        viewModel.uploadAndSave(mock<android.content.Context>(), null, "New", 15.0, "New") { success ->
            successResult = success
        }
        
        assertTrue(successResult)
        verify(repo).updateProduct(any(), any())
    }
}
