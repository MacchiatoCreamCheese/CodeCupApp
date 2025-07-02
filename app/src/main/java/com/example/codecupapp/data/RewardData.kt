package com.example.codecupapp.data

data class RewardItem(
    val title: String,
    val pointsRequired: Int
)

object RewardData {
    val rewardList = mutableListOf<RewardItem>()
}
