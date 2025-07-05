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


// ViewModel responsible for managing shopping cart state
class CartViewModel : ViewModel() {

    // State management: LiveData holding current list of cart items
    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems

    // State update: Top-level reducer function that applies user actions to the cart
    fun dispatch(action: CartAction) {
        when (action) {
            is CartAction.AddItem -> handleAddItem(action.item)
            is CartAction.RemoveItem -> handleRemoveItem(action.item)
            is CartAction.UpdateQuantity -> handleUpdateQuantity(action.item, action.newQuantity)
            is CartAction.ClearCart -> clearCart()
        }
    }

    // Workflow: Adds an item to the cart, or increases quantity if already present
    private fun handleAddItem(item: CartItem) {
        val currentItems = _cartItems.value.orEmpty().toMutableList()
        val existingIndex = currentItems.indexOfFirst { isSameCartItem(it, item) }

        if (existingIndex >= 0) {
            val existingItem = currentItems[existingIndex]
            currentItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity + item.quantity)
        } else {
            currentItems.add(item)
        }

        _cartItems.value = currentItems
    }

    // Workflow: Removes a matching item from the cart
    private fun handleRemoveItem(item: CartItem) {
        _cartItems.value = _cartItems.value.orEmpty().filterNot {
            isSameCartItem(it, item)
        }
    }

    // Workflow: Updates the quantity of a matching item
    private fun handleUpdateQuantity(item: CartItem, newQuantity: Int) {
        val updatedItems = _cartItems.value.orEmpty().map {
            if (isSameCartItem(it, item)) {
                it.copy(quantity = newQuantity)
            } else {
                it
            }
        }
        _cartItems.value = updatedItems
    }

    // State reset: Clears the entire cart
    private fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Helper function: Determines whether two cart items are considered equivalent
    // Business rule: Some items ignore shot count when comparing
    private fun isSameCartItem(a: CartItem, b: CartItem): Boolean {
        val ignoreShotItems = listOf("Latte", "Milk Tea", "Mocha", "Pumpkin Spice", "Taco Milktea")
        val ignoreShot = a.name in ignoreShotItems || b.name in ignoreShotItems

        return a.name == b.name &&
                a.temperature == b.temperature &&
                a.size == b.size &&
                a.ice == b.ice &&
                (ignoreShot || a.shot == b.shot)
    }
}
