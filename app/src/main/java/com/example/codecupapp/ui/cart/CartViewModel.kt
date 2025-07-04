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


    private fun isSameCartItem(a: CartItem, b: CartItem): Boolean {
        val skipShotFor = listOf("Latte", "Milk Tea", "Mocha", "Pumpkin Spice", "Taco Milktea")
        val skipShot = a.name in skipShotFor || b.name in skipShotFor

        return a.name == b.name &&
                a.temperature == b.temperature &&
                a.size == b.size &&
                a.ice == b.ice &&
                (skipShot || a.shot == b.shot)
    }

    // Reducer-style: Immutable state updates
    fun dispatch(action: CartAction) {
        when (action) {
            is CartAction.AddItem -> {
                val currentItems = _cartItems.value.orEmpty().toMutableList()
                val existingItemIndex = currentItems.indexOfFirst {
                    isSameCartItem(it, action.item)
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
                _cartItems.value = _cartItems.value.orEmpty().filterNot {
                    isSameCartItem(it, action.item)
                }
            }

            is CartAction.UpdateQuantity -> {
                val updated = _cartItems.value.orEmpty().map {
                    if (isSameCartItem(it, action.item)) it.copy(quantity = action.newQuantity) else it
                }
                _cartItems.value = updated
            }

            is CartAction.ClearCart -> {
                _cartItems.value = emptyList()
            }
        }
    }

}
