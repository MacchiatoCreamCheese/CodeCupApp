package com.example.codecupapp

import UserData
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codecupapp.data.SharedPrefsManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableLiveData<UserData>()
    val userProfile: LiveData<UserData> get() = _userProfile

    fun loadFromLocal(context: Context) {
        _userProfile.value = ProfileRepository.loadFromLocal(context)
    }

    fun saveToLocal(context: Context, userData: UserData) {
        SharedPrefsManager.saveUserProfile(context, userData)
    }


    fun setProfile(profile: UserData) {
        _userProfile.value = profile
    }
}


