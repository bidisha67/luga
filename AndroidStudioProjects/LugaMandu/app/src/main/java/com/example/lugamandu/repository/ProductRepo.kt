package com.example.lugamandu.repository

import android.content.Context
import android.net.Uri
import com.example.lugamandu.model.ProductModel

interface ProductRepo {
    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit)
    fun addProduct(product: ProductModel, callback: (Boolean, String) -> Unit)
    fun updateProduct(product: ProductModel, callback: (Boolean, String) -> Unit)
    fun deleteProduct(productId: String, callback: (Boolean) -> Unit)
    fun getAllProducts(callback: (Boolean, List<ProductModel>, String) -> Unit)
}