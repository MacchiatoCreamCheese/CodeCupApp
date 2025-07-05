package com.example.codecupapp

import UserData
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableLiveData<UserData>()
    val userProfile: LiveData<UserData> get() = _userProfile

    fun loadFromLocal(context: Context) {
        _userProfile.value = ProfileRepository.loadFromLocal(context)
    }


    fun setProfile(profile: UserData) {
        _userProfile.value = profile
    }
}


