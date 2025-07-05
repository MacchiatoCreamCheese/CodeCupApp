package com.example.codecupapp.data

data class InfoItem(
    val title: String,
    val icon: Int, // Use drawable resource
    val subtitle: String,
    val action: () -> Unit
)
