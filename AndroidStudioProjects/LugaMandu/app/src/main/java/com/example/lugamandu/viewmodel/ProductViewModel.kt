package com.example.lugamandu.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lugamandu.model.ProductModel
import com.example.lugamandu.repository.ProductRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(private val repo: ProductRepo) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _products = MutableStateFlow<List<ProductModel>>(emptyList())
    val products = _products.asStateFlow()

    private val _selectedProduct = MutableStateFlow<ProductModel?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    init { fetchAllProducts() }

    fun selectProduct(product: ProductModel?) { _selectedProduct.value = product }

    fun fetchAllProducts() {
        _loading.value = true

        // Safety Timeout: If Firebase doesn't respond in 5 seconds, stop loading
        viewModelScope.launch {
            delay(5000)
            if (_loading.value) {
                _loading.value = false
                // This tells you if the connection is the problem
                android.util.Log.e("FirebaseDebug", "Database took too long to respond!")
            }
        }

        repo.getAllProducts { success, list, _ ->
            _loading.value = false
            if (success) _products.value = list
        }
    }

    fun uploadAndSave(context: Context, uri: Uri?, name: String, price: Double, desc: String, onComplete: (Boolean) -> Unit) {
        _loading.value = true
        if (uri != null) {
            repo.uploadImage(context, uri) { url ->
                if (url != null) {
                    val product = ProductModel(
                        id = _selectedProduct.value?.id ?: "",
                        name = name, price = price, description = desc, imageUrl = url
                    )
                    saveToFirebase(product, onComplete)
                } else {
                    _loading.value = false
                    onComplete(false)
                }
            }
        } else if (_selectedProduct.value != null) {
            val product = _selectedProduct.value!!.copy(name = name, price = price, description = desc)
            saveToFirebase(product, onComplete)
        } else {
            _loading.value = false
            onComplete(false)
        }
    }

    private fun saveToFirebase(product: ProductModel, onComplete: (Boolean) -> Unit) {
        if (product.id.isEmpty()) {
            repo.addProduct(product) { success, _ ->
                _loading.value = false
                onComplete(success)
            }
        } else {
            repo.updateProduct(product) { success, _ ->
                _loading.value = false
                onComplete(success)
            }
        }
    }

    fun deleteProduct(id: String, onComplete: (Boolean) -> Unit) {
        _loading.value = true
        repo.deleteProduct(id) { success ->
            _loading.value = false
            onComplete(success)
        }
    }
}