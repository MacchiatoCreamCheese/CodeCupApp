package com.example.codecupapp

import OrderItem
import UserData
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrdersViewModel : ViewModel() {

    // LiveData to track current and past orders
    private val _ongoingOrders = MutableLiveData<MutableList<OrderItem>>(mutableListOf())
    val ongoingOrders: LiveData<MutableList<OrderItem>> get() = _ongoingOrders

    private val _historyOrders = MutableLiveData<MutableList<OrderItem>>(mutableListOf())
    val historyOrders: LiveData<MutableList<OrderItem>> get() = _historyOrders

    // Tracks selected delivery type (Deliver or Pickup)
    val deliveryType = MutableLiveData("Deliver")

    // Adds a new order to the ongoing list and syncs to Firestore
    fun addOngoing(order: OrderItem) {
        val updatedList = _ongoingOrders.value?.toMutableList() ?: mutableListOf()
        updatedList.add(order)
        _ongoingOrders.value = updatedList
        syncOrdersToFirebase()
    }

    // Moves an order to the history list and syncs
    fun addToHistory(order: OrderItem) {
        val updatedList = _historyOrders.value?.toMutableList() ?: mutableListOf()
        updatedList.add(order)
        _historyOrders.value = updatedList
        syncOrdersToFirebase()
    }

    // Removes an order from ongoing list and syncs
    fun removeOngoing(item: OrderItem) {
        _ongoingOrders.value = _ongoingOrders.value?.apply { remove(item) }
        syncOrdersToFirebase()
    }

    // Moves an order from ongoing to history by index (used by UI)
    fun completeOrder(position: Int) {
        val order = _ongoingOrders.value?.removeAt(position)
        _ongoingOrders.value = _ongoingOrders.value
        order?.let {
            _historyOrders.value?.add(0, it)
            _historyOrders.value = _historyOrders.value
        }
        syncOrdersToFirebase()
    }

    // Upload current state of ongoing/history orders to Firestore
    private fun syncOrdersToFirebase() {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val updates = mapOf(
                    "ongoingOrders" to _ongoingOrders.value,
                    "historyOrders" to _historyOrders.value
                )
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update(updates)
                    .await()
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "Sync failed: ${e.message}")
            }
        }
    }

    // Loads ongoing and history orders from Firestore (used on login)

    fun setHistoryOrders(orders: MutableList<OrderItem>) {
        _historyOrders.value = orders
    }
    fun setOngoingOrders(orders: MutableList<OrderItem>) {
        _ongoingOrders.value = orders
    }

}
