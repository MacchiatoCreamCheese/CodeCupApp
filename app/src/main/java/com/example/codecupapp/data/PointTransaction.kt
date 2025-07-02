package com.example.codecupapp

data class PointTransaction(
    val source: String,  // "Order: Latte" or "Redeemed Free Coffee"
    val amount: Int,     // Positive = Earned, Negative = Spent
    val date: String
)
