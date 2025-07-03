package com.example.codecupapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.codecupapp.data.CartItem


sealed class CartAction {
    data class AddItem(val item: CartItem) : CartAction()
    data class RemoveItem(val item: CartItem) : CartAction()
    data class UpdateQuantity(val item: CartItem, val newQuantity: Int) : CartAction()
    object ClearCart : CartAction()
}



class CartViewModel : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems

    // Reducer-style: Immutable state updates
    fun dispatch(action: CartAction) {
        when (action) {
            is CartAction.AddItem -> {
                val currentItems = _cartItems.value.orEmpty().toMutableList()
                val existingItemIndex = currentItems.indexOfFirst {
                    it.name == action.item.name &&
                            it.shot == action.item.shot &&
                            it.temperature == action.item.temperature &&
                            it.size == action.item.size &&
                            it.ice == action.item.ice
                }

                if (existingItemIndex >= 0) {
                    val existing = currentItems[existingItemIndex]
                    currentItems[existingItemIndex] = existing.copy(quantity = existing.quantity + action.item.quantity)
                } else {
                    currentItems.add(action.item)
                }

                _cartItems.value = currentItems
            }
            is CartAction.RemoveItem -> {
                _cartItems.value = _cartItems.value.orEmpty().filter { it.name != action.item.name }
            }
            is CartAction.UpdateQuantity -> {
                val updated = _cartItems.value.orEmpty().map {
                    if (it.name == action.item.name) it.copy(quantity = action.newQuantity) else it
                }
                _cartItems.value = updated
            }
            is CartAction.ClearCart -> {
                _cartItems.value = emptyList()
            }
        }
    }
}
