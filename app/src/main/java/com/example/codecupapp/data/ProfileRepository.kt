
package com.example.codecupapp.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ProfileRepository {

    fun loadUserProfile(onComplete: (() -> Unit)? = null, onError: ((String) -> Unit)? = null) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onError?.invoke("User not signed in")
            return
        }

        FirebaseFirestore.getInstance().collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    UserData.name = doc.getString("name") ?: "Guest"
                    UserData.email = doc.getString("email") ?: ""
                    UserData.phone = doc.getString("phone") ?: ""
                    UserData.gender = doc.getString("gender") ?: ""
                    UserData.address = doc.getString("address") ?: ""

                    Log.d("ProfileRepo", "User profile loaded")
                } else {
                    Log.w("ProfileRepo", "No profile found")
                }
                onComplete?.invoke()
            }
            .addOnFailureListener {
                Log.e("ProfileRepo", "Error loading profile: ${it.message}")
                onError?.invoke(it.message ?: "Unknown error")
            }
    }
}
