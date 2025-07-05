package com.example.codecupapp

import UserData
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.codecupapp.data.SharedPrefsManager

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableLiveData<UserData>()
    val userProfile: LiveData<UserData> get() = _userProfile

    fun loadFromLocal(context: Context) {
        _userProfile.value = SharedPrefsManager.loadUserProfile(context)
    }

    fun setProfile(user: UserData) {
        _userProfile.value = user
    }

    fun saveToLocal(context: Context, profile: UserData) {
        val prefs = context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("name", profile.name)
            putString("phone", profile.phone)
            putString("gender", profile.gender)
            putString("address", profile.address)
            putString("email", profile.email)
        }.apply()
    }
}



