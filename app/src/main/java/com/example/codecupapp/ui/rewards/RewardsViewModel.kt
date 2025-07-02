package com.example.codecupapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.codecupapp.data.RewardItem



class RewardsViewModel : ViewModel() {

    private val _points = MutableLiveData(0)
    val points: LiveData<Int> get() = _points

    private val _rewardList = MutableLiveData<List<RewardItem>>(emptyList())
    val rewardList: LiveData<List<RewardItem>> get() = _rewardList

    private val _transactionHistory = MutableLiveData<List<PointTransaction>>(emptyList())
    val transactionHistory: LiveData<List<PointTransaction>> = _transactionHistory


    init {
        initializeRewards()
    }

    private val _redeemList = MutableLiveData<List<RedeemItem>>(emptyList())
    val redeemList: LiveData<List<RedeemItem>> = _redeemList

    fun addToRedeem(item: RedeemItem) {
        val updated = _redeemList.value.orEmpty().toMutableList()
        updated.add(0, item)  // Newest on top
        _redeemList.value = updated
    }


    fun initializeRewards() {
        if (_rewardList.value.isNullOrEmpty()) {
            _rewardList.value = listOf(
                RewardItem("Free Coffee", 100),
                RewardItem("10% Discount", 1),
                RewardItem("Tote Bag", 150)
            )
        }
    }

    fun setPoints(value: Int) {
        _points.value = value
    }

    fun addPoints(source: String, amount: Int) {
        _points.value = (_points.value ?: 0) + amount
        recordTransaction(source, amount)
    }

    fun subtractPoints(source: String, amount: Int) {
        _points.value = (_points.value ?: 0) - amount
        recordTransaction(source, -amount)
    }

    private fun recordTransaction(source: String, amount: Int) {
        val formatter = java.text.SimpleDateFormat("dd MMM | hh:mm a", java.util.Locale.getDefault())
        val updated = _transactionHistory.value.orEmpty().toMutableList()
        updated.add(0, PointTransaction(source, amount, formatter.format(java.util.Date())))
        _transactionHistory.value = updated
    }
    fun redeem(reward: RewardItem): Boolean {
        val current = _points.value ?: 0
        return if (current >= reward.pointsRequired) {
            subtractPoints("Redeemed: ${reward.title}", reward.pointsRequired)
            true
        } else {
            false
        }
    }


}
