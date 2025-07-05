package com.example.codecupapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


/**
 * ViewModel for managing loyalty stamp state and syncing with Firestore.
 */
class LoyaltyViewModel : ViewModel() {

    // Internal mutable stamp count (max 8)
    private val _stamps = MutableLiveData(0)
    val stamps: LiveData<Int> = _stamps

    /** Increments the stamp count by 1 (up to 8), and syncs to Firestore */
    fun addStamp() {
        val updated = ((_stamps.value ?: 0) + 1).coerceAtMost(8)
        _stamps.value = updated
        syncStampsToFirestore(updated)
    }

    /** Resets stamp count to 0 and syncs to Firestore */
    fun resetStamps() {
        _stamps.value = 0
        syncStampsToFirestore(0)
    }

    /** Sets initial stamp count (used on login) */
    fun setInitialStamps(count: Int) {
        _stamps.value = count
        syncStampsToFirestore(count)
    }

    /** Pushes current stamp count to Firestore */
    private fun syncStampsToFirestore(stampCount: Int) {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("stamps", stampCount)
            } catch (e: Exception) {
                Log.e("LoyaltyViewModel", "Failed to sync stamps: ${e.message}")
            }
        }
    }


}

