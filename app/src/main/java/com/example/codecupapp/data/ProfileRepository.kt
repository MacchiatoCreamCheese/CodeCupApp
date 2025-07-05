package com.example.codecupapp

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

        val profile = snapshot.toObject(UserData::class.java)
            ?: throw Exception("Profile not found")

        // Save locally
        SharedPrefsManager.saveUserProfile(context, profile)

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
