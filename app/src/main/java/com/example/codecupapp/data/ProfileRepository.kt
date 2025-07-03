
package com.example.codecupapp

import UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object ProfileRepository {

    suspend fun loadUserProfileSuspend(): UserData {
        val user = FirebaseAuth.getInstance().currentUser ?: throw Exception("Not signed in")

        val snapshot = FirebaseFirestore.getInstance().collection("users")
            .document(user.uid)
            .get()
            .await()

        if (!snapshot.exists()) throw Exception("Profile not found")

        return UserData(
            name = snapshot.getString("name") ?: "Guest",
            email = snapshot.getString("email") ?: "",
            phone = snapshot.getString("phone") ?: "",
            gender = snapshot.getString("gender") ?: "",
            address = snapshot.getString("address") ?: ""
        )
    }
}
