package com.example.codecupapp

import UserData
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableLiveData<UserData>()
    val userProfile: LiveData<UserData> get() = _userProfile

    init {
        loadProfile()
    }

    fun loadProfile() {
        ProfileRepository.loadUserProfile(
            onComplete = { profile ->
                _userProfile.value = profile
            },
            onError = { error ->
                Log.e("ProfileViewModel", "Error: $error")
            }
        )
    }


    fun updateProfile(
        updated: UserData,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val updateMap = mapOf(
            "name" to updated.name,
            "email" to updated.email,
            "phone" to updated.phone,
            "gender" to updated.gender,
            "address" to updated.address
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(updateMap)
            .addOnSuccessListener {
                _userProfile.value = updated
                onSuccess()
            }
            .addOnFailureListener {
                onFailure()
            }
    }
}


