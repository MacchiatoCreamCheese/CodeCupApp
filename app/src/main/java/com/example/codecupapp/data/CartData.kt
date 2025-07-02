package com.example.codecupapp.data

data class CartItem(
    val name: String,
    val shot: String,
    val temperature: String,
    val size: String,
    val ice: String,
    val quantity: Int,
    val unitPrice: Double
)

object CartData {
    val cartItems = mutableListOf<CartItem>()
}
