package com.example.lugamandu.repository

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.lugamandu.model.ProductModel
import com.google.firebase.database.*
import java.io.InputStream
import java.util.concurrent.Executors

class ProductRepoImpl : ProductRepo {
    // 1. Explicit URL from your new google-services.json
    private val dbUrl = "https://lugamandu-9f7c4-default-rtdb.firebaseio.com"

    // 2. Database reference (Fixed the duplicate/spacing error)
    private val database = FirebaseDatabase.getInstance(dbUrl).reference.child("products")

    private val cloudinary = Cloudinary(mapOf(
        "cloud_name" to "dwfc51vqa",
        "api_key" to "433791988788857",
        "api_secret" to "u9Qgd5h0Y-hmyxPFO1hsp01swEI"
    ))

    override fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                val response = cloudinary.uploader().upload(inputStream, ObjectUtils.asMap(
                    "folder", "products"
                ))
                val url = (response["url"] as String?)?.replace("http://", "https://")
                Handler(Looper.getMainLooper()).post { callback(url) }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { callback(null) }
            }
        }
    }

    override fun addProduct(product: ProductModel, callback: (Boolean, String) -> Unit) {
        val id = database.push().key ?: return callback(false, "ID Error")
        product.id = id
        database.child(id).setValue(product).addOnCompleteListener {
            callback(it.isSuccessful, if (it.isSuccessful) "Added" else "Failed")
        }
    }

    override fun updateProduct(product: ProductModel, callback: (Boolean, String) -> Unit) {
        database.child(product.id).updateChildren(product.toMap()).addOnCompleteListener {
            callback(it.isSuccessful, if (it.isSuccessful) "Updated" else "Failed")
        }
    }

    override fun deleteProduct(productId: String, callback: (Boolean) -> Unit) {
        database.child(productId).removeValue().addOnCompleteListener { callback(it.isSuccessful) }
    }

    override fun getAllProducts(callback: (Boolean, List<ProductModel>, String) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ProductModel>()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        child.getValue(ProductModel::class.java)?.let { list.add(it) }
                    }
                }
                // This callback stops the loading spinner in your UI
                callback(true, list, "Success")
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, emptyList(), error.message)
            }
        })
    }
}