package com.example.codecupapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.codecupapp.data.OrderItem

class OrdersViewModel : ViewModel() {

    private val _ongoingOrders = MutableLiveData<MutableList<OrderItem>>(mutableListOf())
    val ongoingOrders: LiveData<MutableList<OrderItem>> get() = _ongoingOrders
    val deliveryType = MutableLiveData("Deliver")

    private val _historyOrders = MutableLiveData<MutableList<OrderItem>>(mutableListOf())
    val historyOrders: LiveData<MutableList<OrderItem>> get() = _historyOrders

    fun addOngoing(order: OrderItem) {
        _ongoingOrders.value?.add(order)
        _ongoingOrders.value = _ongoingOrders.value
    }
    fun removeOngoing(item: OrderItem) {
        _ongoingOrders.value = ongoingOrders.value?.apply { remove(item) }
    }

    fun addToHistory(item: OrderItem) {
        _historyOrders.value = historyOrders.value?.apply { add(0, item) }
    }

    fun completeOrder(position: Int) {
        val order = _ongoingOrders.value?.removeAt(position)
        _ongoingOrders.value = _ongoingOrders.value
        order?.let {
            _historyOrders.value?.add(0, it)
            _historyOrders.value = _historyOrders.value
        }
    }
}
