package com.example.lugamandu.repository

import com.example.lugamandu.model.CartModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartRepoImpl : CartRepo {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("carts")

    override fun addToCart(userId: String, cartItem: CartModel, callback: (Boolean, String) -> Unit) {
        val ref = database.child(userId)
        val id = ref.push().key ?: return callback(false, "Failed to generate cart item ID")
        cartItem.cartItemId = id
        ref.child(id).setValue(cartItem.toMap())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(true, "Added to cart")
                else callback(false, task.exception?.message ?: "Failed to add to cart")
            }
    }

    override fun removeFromCart(userId: String, cartItemId: String, callback: (Boolean, String) -> Unit) {
        database.child(userId).child(cartItemId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(true, "Removed from cart")
                else callback(false, task.exception?.message ?: "Failed to remove from cart")
            }
    }

    override fun getCartItems(userId: String, callback: (Boolean, List<CartModel>, String) -> Unit) {
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<CartModel>()
                for (child in snapshot.children) {
                    val item = child.getValue(CartModel::class.java)
                    if (item != null) items.add(item)
                }
                callback(true, items, "Cart fetched")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, emptyList(), error.message)
            }
        })
    }

    override fun clearCart(userId: String, callback: (Boolean, String) -> Unit) {
        database.child(userId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(true, "Cart cleared")
                else callback(false, task.exception?.message ?: "Failed to clear cart")
            }
    }
}
