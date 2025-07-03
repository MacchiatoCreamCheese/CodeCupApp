
package com.example.codecupapp

import UserData
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ProfileRepository {

    fun loadUserProfile(
        onComplete: (UserData) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onError("User not signed in")
            return
        }

        FirebaseFirestore.getInstance().collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val profile = UserData(
                        name = doc.getString("name") ?: "Guest",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        gender = doc.getString("gender") ?: "",
                        address = doc.getString("address") ?: ""
                    )

                    Log.d("ProfileRepo", "User profile loaded: $profile")
                    onComplete(profile)
                } else {
                    Log.w("ProfileRepo", "No profile found")
                    onError("No profile found")
                }
            }
            .addOnFailureListener {
                Log.e("ProfileRepo", "Error loading profile: ${it.message}")
                onError(it.message ?: "Unknown error")
            }
    }
}
