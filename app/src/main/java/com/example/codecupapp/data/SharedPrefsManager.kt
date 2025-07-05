package com.example.codecupapp.data

import UserData
import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object SharedPrefsManager {

    private const val PREFS_NAME = "user_prefs"

    fun saveUserProfile(context: Context, profile: UserData) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putString("name", profile.name)
            putString("email", profile.email)
            putString("phone", profile.phone)
            putString("gender", profile.gender)
            putString("address", profile.address)
        }.apply()
    }

    fun loadUserProfile(context: Context): UserData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return UserData(
            name = prefs.getString("name", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            phone = prefs.getString("phone", "") ?: "",
            gender = prefs.getString("gender", "") ?: "",
            address = prefs.getString("address", "") ?: ""
        )
    }

    fun updateField(context: Context, key: String, value: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(key, value)
            .apply()
    }
    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun getAllAsMap(context: Context): Map<String, Any> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.all.filterValues { it is String } as Map<String, Any>
    }
}
