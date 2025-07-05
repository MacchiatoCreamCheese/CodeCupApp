package com.example.codecupapp.data

import OrderItem
import PointTransaction
import UserData
import android.content.Context

object SharedPrefsManager {

    private const val PREFS_NAME = "user_prefs"

    fun saveUserProfile(context: Context, profile: UserData) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putString("name", profile.name)
            putString("email", profile.email)
            putString("phone", profile.phone)
            putString("gender", profile.gender)
            putString("address", profile.address)
        }.apply()
    }

    fun loadUserProfile(context: Context): UserData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return UserData(
            name = prefs.getString("name", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            phone = prefs.getString("phone", "") ?: "",
            gender = prefs.getString("gender", "") ?: "",
            address = prefs.getString("address", "") ?: ""
        )
    }

    fun updateField(context: Context, key: String, value: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(key, value)
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun getAllAsMap(context: Context): Map<String, Any> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.all.filterValues { it is String } as Map<String, Any>
    }
}

sealed class PendingWrite {
    data class OrderItemWrite(val order: OrderItem) : PendingWrite()
    data class PointChange(val delta: Int) : PendingWrite() // points or stamps
    data class StampChange(val delta: Int) : PendingWrite() // optional separate class
    data class RedeemHistoryWrite(val transaction: PointTransaction) : PendingWrite()
}

object PendingWritesManager {

    private val pendingOrderItems = mutableListOf<OrderItem>()
    private val pendingRedeemTransactions = mutableListOf<PointTransaction>()
    private var pendingPointsDelta: Int = 0
    private var pendingStampsDelta: Int = 0

    fun queueOrder(order: OrderItem) {
        pendingOrderItems.add(order)
    }

    fun queueRedeemTransaction(tx: PointTransaction) {
        pendingRedeemTransactions.add(tx)
    }

    fun queuePointChange(delta: Int) {
        pendingPointsDelta += delta
    }

    fun queueStampChange(delta: Int) {
        pendingStampsDelta += delta
    }

    fun hasPendingWrites(): Boolean {
        return pendingOrderItems.isNotEmpty() ||
                pendingRedeemTransactions.isNotEmpty() ||
                pendingPointsDelta != 0 ||
                pendingStampsDelta != 0
    }

    fun applyToUserData(user: UserData): UserData {
        return user.copy(
            ongoingOrders = user.ongoingOrders + pendingOrderItems,
            points = user.points + pendingPointsDelta,
            stamps = user.stamps + pendingStampsDelta,
            redeemHistory = user.redeemHistory + pendingRedeemTransactions
        )
    }

    fun clearOrder() {
        pendingOrderItems.clear()
    }

    fun clearRedeemTransactions() {
        pendingRedeemTransactions.clear()
    }

    fun clearPoints() {
        pendingPointsDelta = 0
    }

    fun clearStamps() {
        pendingStampsDelta = 0
    }

    fun clear() {
        pendingOrderItems.clear()
        pendingRedeemTransactions.clear()
        pendingPointsDelta = 0
        pendingStampsDelta = 0
    }

    fun getPendingMap(): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        if (pendingOrderItems.isNotEmpty()) result["ongoingOrders"] = pendingOrderItems
        if (pendingRedeemTransactions.isNotEmpty()) result["redeemHistory"] =
            pendingRedeemTransactions
        if (pendingPointsDelta != 0) result["points"] = pendingPointsDelta
        if (pendingStampsDelta != 0) result["stamps"] = pendingStampsDelta
        return result
    }


}



