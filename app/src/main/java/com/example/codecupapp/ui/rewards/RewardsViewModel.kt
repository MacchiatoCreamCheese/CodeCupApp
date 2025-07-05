package com.example.codecupapp

import PointTransaction
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codecupapp.data.RewardItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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


    fun initializeRewards() {
        if (_rewardList.value.isNullOrEmpty()) {
            _rewardList.value = listOf(
                RewardItem("Free cup", 40),
                RewardItem("10% Discount", 5),
                RewardItem("Tote Bag", 50),
                RewardItem("Capuchino", 20),
                RewardItem("Freeship", 15),
                RewardItem("Pudding", 10),
                RewardItem("School Bag", 100)
            )
        }
    }

    fun setPoints(value: Int) {
        _points.value = value
        syncPointsToFirestore(value)
    }

    fun addPoints(source: String, amount: Int) {
        val updated = (_points.value ?: 0) + amount
        _points.value = updated
        recordTransaction(source, amount)
        syncPointsToFirestore(updated)
    }

    fun subtractPoints(source: String, amount: Int) {
        val updated = (_points.value ?: 0) - amount
        _points.value = updated
        recordTransaction(source, -amount)
        syncPointsToFirestore(updated)
    }


    private fun recordTransaction(source: String, amount: Int) {
        val formatter =
            java.text.SimpleDateFormat("dd MMM | hh:mm a", java.util.Locale.getDefault())
        val newTransaction = PointTransaction(source, amount, formatter.format(java.util.Date()))

        val updated = _transactionHistory.value.orEmpty().toMutableList()
        updated.add(0, newTransaction)
        _transactionHistory.value = updated

        syncRedeemHistoryToFirebase(updated)
    }

    private fun syncRedeemHistoryToFirebase(history: List<PointTransaction>) {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val data = mapOf("redeemHistory" to history)
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update(data)
            } catch (e: Exception) {
                Log.e("RewardsViewModel", "Failed to sync redeemHistory: ${e.message}")
            }
        }
    }

    fun loadRedeemHistoryFromFirebase(context: Context) {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .await()

                val historyList =
                    snapshot["redeemHistory"] as? List<Map<String, Any>> ?: emptyList()
                val parsed = historyList.mapNotNull { map ->
                    try {
                        PointTransaction(
                            source = map["source"] as? String ?: "",
                            amount = (map["amount"] as? Long)?.toInt() ?: 0,
                            date = map["date"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                _transactionHistory.value = parsed
            } catch (e: Exception) {
                Log.e("RewardsViewModel", "Failed to load redeemHistory: ${e.message}")
            }
        }
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

    private fun syncPointsToFirestore(points: Int) {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("points", points)
            } catch (e: Exception) {
                Log.e("RewardsViewModel", "Failed to sync points: ${e.message}")
            }
        }
    }



}
