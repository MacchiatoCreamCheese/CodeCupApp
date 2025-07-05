
package com.example.codecupapp

import PointTransaction
import UserData
import android.content.Context
import com.example.codecupapp.data.SharedPrefsManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


object ProfileRepository {

    suspend fun loadUserProfileSuspend(context: Context): UserData {
        val user = FirebaseAuth.getInstance().currentUser ?: throw Exception("Not signed in")
        val snapshot = FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .get()
            .await()

        val profile = UserData(
            name = snapshot.getString("name") ?: "Guest",
            email = snapshot.getString("email") ?: "",
            phone = snapshot.getString("phone") ?: "",
            gender = snapshot.getString("gender") ?: "",
            address = snapshot.getString("address") ?: "",
            points = (snapshot.getLong("points") ?: 0L).toInt(), // 👈 load points
            redeemHistory = (snapshot["redeemHistory"] as? List<Map<String, Any>>)
                ?.mapNotNull { map ->
                    try {
                        PointTransaction(
                            source = map["source"] as? String ?: "",
                            amount = (map["amount"] as? Long)?.toInt() ?: 0,
                            date = map["date"] as? String ?: ""
                        )
                    } catch (_: Exception) { null }
                } ?: emptyList()
        )
        // Load stamps separately
        val stampCount = (snapshot.getLong("stamps") ?: 0L).toInt()
        SharedPrefsManager.saveUserProfile(context, profile) // 🟢 Store locally too


        // Push to ViewModels if in login fragment
        RewardsViewModel().setPoints(profile.points)
        LoyaltyViewModel().setInitialStamps(stampCount)

        return profile
    }

    fun updateUserProfile(
        context: Context,
        updated: UserData,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onFailure(Exception("Not signed in"))
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .set(updated)
            .addOnSuccessListener {
                SharedPrefsManager.saveUserProfile(context, updated) // ✅ Save to prefs
                onSuccess()
            }
            .addOnFailureListener { onFailure(it) }
    }


    fun loadFromLocal(context: Context): UserData {
        return SharedPrefsManager.loadUserProfile(context)
    }
}
