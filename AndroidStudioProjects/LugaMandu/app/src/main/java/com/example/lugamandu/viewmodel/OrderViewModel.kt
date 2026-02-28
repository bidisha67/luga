package com.example.lugamandu.viewmodel

import androidx.lifecycle.ViewModel
import com.example.lugamandu.model.OrderModel
import com.example.lugamandu.repository.OrderRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrderViewModel(private val orderRepo: OrderRepo) : ViewModel() {
    private val _orders = MutableStateFlow<List<OrderModel>>(emptyList())
    val orders: StateFlow<List<OrderModel>> = _orders.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun fetchAllOrders() {
        _loading.value = true
        orderRepo.getAllOrders { success, list, message ->
            _loading.value = false
            if (success) {
                _orders.value = list
            }
        }
    }

    fun fetchUserOrders(userId: String) {
        _loading.value = true
        orderRepo.getUserOrders(userId) { success, list, message ->
            _loading.value = false
            if (success) {
                _orders.value = list
            }
        }
    }

    fun placeOrder(userId: String, order: OrderModel, callback: (Boolean, String) -> Unit) {
        _loading.value = true
        orderRepo.placeOrder(userId, order) { success, message ->
            _loading.value = false
            callback(success, message)
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String, callback: (Boolean, String) -> Unit) {
        _loading.value = true
        orderRepo.updateOrderStatus(orderId, newStatus) { success, message ->
            _loading.value = false
            callback(success, message)
            if (success) {
                fetchAllOrders() // refresh data immediately
            }
        }
    }
}
