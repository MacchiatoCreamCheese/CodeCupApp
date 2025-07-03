package com.example.codecupapp

import UserData
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableLiveData<UserData>()
    val userProfile: LiveData<UserData> get() = _userProfile

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                val profile = ProfileRepository.loadUserProfileSuspend()
                _userProfile.value = profile
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile: ${e.message}")
            }
        }
    }


    fun updateProfile(
        updated: UserData,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onFailure(Exception("Not signed in"))
            return
        }

        FirebaseFirestore.getInstance().collection("users")
            .document(user.uid)
            .set(updated)
            .addOnSuccessListener {
                loadProfile()
                onSuccess()
            }
            .addOnFailureListener { onFailure(it) }
    }

}


