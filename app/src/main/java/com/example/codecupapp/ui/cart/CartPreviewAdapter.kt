package com.example.codecupapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.data.CartItem
import com.example.codecupapp.databinding.ItemCartPreviewBinding
// Adapter for displaying cart item previews in a RecyclerView
class CartPreviewAdapter(
    private val items: List<CartItem> // Immutable list of cart items
) : RecyclerView.Adapter<CartPreviewAdapter.CartViewHolder>() {

    // ViewHolder class that binds UI layout with cart item data
    inner class CartViewHolder(private val binding: ItemCartPreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // UI workflow: Bind a CartItem's data into the UI
        fun bind(item: CartItem) {
            binding.textCartItemName.text = item.name
            binding.textCartItemDetails.text = buildItemDetails(item)
            binding.textCartItemQuantity.text = formatQuantity(item.quantity)
            binding.textCartItemPrice.text = formatPrice(item.unitPrice, item.quantity)
        }

        // UI formatting: Builds item detail string (size, temp, shot, ice)
        private fun buildItemDetails(item: CartItem): String {
            val itemsWithHiddenShot = listOf("Latte", "Milk Tea", "Mocha", "Pumpkin Spice", "Taco Milktea")

            return buildList {
                add(item.size)
                add(item.temperature)
                if (item.name !in itemsWithHiddenShot) {
                    add("${item.shot} shot")
                }
                add(item.ice)
            }.joinToString(", ")
        }

        // UI formatting: Formats the quantity display (e.g., "x2")
        private fun formatQuantity(quantity: Int): String {
            return "x$quantity"
        }

        // UI formatting: Calculates and formats total item price
        private fun formatPrice(unitPrice: Double, quantity: Int): String {
            val totalPrice = unitPrice * quantity
            return "$%.2f".format(totalPrice)
        }
    }

    // View lifecycle: Inflate layout and create a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCartPreviewBinding.inflate(inflater, parent, false)
        return CartViewHolder(binding)
    }

    // View lifecycle: Bind the data at the current position to the ViewHolder
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    // State access: Returns the total number of items
    override fun getItemCount(): Int = items.size
}
