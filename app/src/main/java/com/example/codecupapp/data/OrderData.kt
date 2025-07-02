package com.example.codecupapp.data


data class OrderItem(
    val date: String,
    val name: String,
    val address: String,
    val price: Double
)

object OrderData {
    val ongoingOrders = mutableListOf<OrderItem>()
    val historyOrders = mutableListOf<OrderItem>()
}
