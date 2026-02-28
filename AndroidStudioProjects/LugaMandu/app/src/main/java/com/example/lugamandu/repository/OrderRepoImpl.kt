package com.example.lugamandu.repository

import com.example.lugamandu.model.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrderRepoImpl : OrderRepo {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("orders")

    override fun placeOrder(userId: String, order: OrderModel, callback: (Boolean, String) -> Unit) {
        val id = database.push().key ?: return callback(false, "Failed to generate order ID")
        order.orderId = id
        order.userId = userId
        order.status = "Pending"
        order.timestamp = System.currentTimeMillis()
        
        database.child(id).setValue(order.toMap())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(true, "Order placed successfully")
                else callback(false, task.exception?.message ?: "Failed to place order")
            }
    }

    override fun getAllOrders(callback: (Boolean, List<OrderModel>, String) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<OrderModel>()
                for (child in snapshot.children) {
                    val order = child.getValue(OrderModel::class.java)
                    if (order != null) orders.add(order)
                }
                // Sort by timestamp descending
                callback(true, orders.sortedByDescending { it.timestamp }, "Orders fetched")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, emptyList(), error.message)
            }
        })
    }

    override fun getUserOrders(userId: String, callback: (Boolean, List<OrderModel>, String) -> Unit) {
        // Simple client-side filtering since query sorting in Firebase needs index configuration
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<OrderModel>()
                for (child in snapshot.children) {
                    val order = child.getValue(OrderModel::class.java)
                    if (order != null && order.userId == userId) {
                        orders.add(order)
                    }
                }
                callback(true, orders.sortedByDescending { it.timestamp }, "User orders fetched")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, emptyList(), error.message)
            }
        })
    }

    override fun updateOrderStatus(orderId: String, newStatus: String, callback: (Boolean, String) -> Unit) {
        database.child(orderId).child("status").setValue(newStatus)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(true, "Order status updated to $newStatus")
                else callback(false, task.exception?.message ?: "Failed to update status")
            }
    }

    override fun hasUserPurchasedProduct(userId: String, productId: String, callback: (Boolean) -> Unit) {
        database.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var hasPurchased = false
                    for (orderSnapshot in snapshot.children) {
                        val itemsSnapshot = orderSnapshot.child("items") // Matches your Firebase screenshot
                        if (itemsSnapshot.exists()) {
                            for (item in itemsSnapshot.children) {
                                val idInOrder = item.child("productId").value?.toString()
                                if (idInOrder == productId) {
                                    hasPurchased = true
                                    break
                                }
                            }
                        }
                        if (hasPurchased) break
                    }
                    callback(hasPurchased)
                }
                override fun onCancelled(error: DatabaseError) { callback(false) }
            })
    }
}
