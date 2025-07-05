package com.example.codecupapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import OrderItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrdersViewModel : ViewModel() {

    private val _ongoingOrders = MutableLiveData<MutableList<OrderItem>>(mutableListOf())
    val ongoingOrders: LiveData<MutableList<OrderItem>> get() = _ongoingOrders
    val deliveryType = MutableLiveData("Deliver")

    private val _historyOrders = MutableLiveData<MutableList<OrderItem>>(mutableListOf())
    val historyOrders: LiveData<MutableList<OrderItem>> get() = _historyOrders

    fun addOngoing(order: OrderItem) {
        val list = ongoingOrders.value?.toMutableList() ?: mutableListOf()
        list.add(order)
        _ongoingOrders.value = list
        syncOrdersToFirebase()
    }

    fun addToHistory(order: OrderItem) {
        val list = historyOrders.value?.toMutableList() ?: mutableListOf()
        list.add(order)
        _historyOrders.value = list
        syncOrdersToFirebase()
    }

    fun removeOngoing(item: OrderItem) {
        _ongoingOrders.value = ongoingOrders.value?.apply { remove(item) }
        syncOrdersToFirebase()
    }

    fun syncOrdersToFirebase() {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val updatedData = mapOf(
                    "ongoingOrders" to _ongoingOrders.value,
                    "historyOrders" to _historyOrders.value
                )
                FirebaseFirestore.getInstance().collection("users")
                    .document(uid).update(updatedData).await()
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "Sync failed: ${e.message}")
            }
        }
    }


    fun completeOrder(position: Int) {
        val order = _ongoingOrders.value?.removeAt(position)
        _ongoingOrders.value = _ongoingOrders.value
        order?.let {
            _historyOrders.value?.add(0, it)
            _historyOrders.value = _historyOrders.value
        }
    }

    fun loadOrdersFromFirebase() {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val doc = FirebaseFirestore.getInstance()
                    .collection("users").document(uid).get().await()

                val userData = doc.toObject(UserData::class.java)
                _ongoingOrders.value = userData?.ongoingOrders?.toMutableList() ?: mutableListOf()
                _historyOrders.value = userData?.historyOrders?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "Failed to load orders: ${e.message}")
            }
        }
    }

    // Optional: helper to refresh
    fun refresh() = loadOrdersFromFirebase()



}
