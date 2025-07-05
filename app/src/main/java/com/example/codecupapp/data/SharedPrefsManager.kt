package com.example.codecupapp.data

import UserData
import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object SharedPrefsManager {

    private const val PREFS_NAME = "user_prefs"

    fun saveUserProfile(context: Context, profile: UserData) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("name", profile.name)
            putString("phone", profile.phone)
            putString("gender", profile.gender)
            putString("address", profile.address)
            apply()
        }
    }

    fun loadUserProfile(context: Context): UserData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return UserData(
            name = prefs.getString("name", "Guest") ?: "Guest",
            email = FirebaseAuth.getInstance().currentUser?.email ?: "",
            phone = prefs.getString("phone", "") ?: "",
            gender = prefs.getString("gender", "") ?: "",
            address = prefs.getString("address", "") ?: ""
        )
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }



}
