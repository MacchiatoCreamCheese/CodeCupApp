package com.example.codecupapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoyaltyViewModel : ViewModel() {

    private val _stamps = MutableLiveData(0)
    val stamps: LiveData<Int> = _stamps

    fun addStamp() {
        _stamps.value = ((_stamps.value ?: 0) + 1).coerceAtMost(8)
    }

    fun resetStamps() {
        _stamps.value = 0
    }
    fun setInitialStamps(count: Int) {
        _stamps.value = count
    }

}
