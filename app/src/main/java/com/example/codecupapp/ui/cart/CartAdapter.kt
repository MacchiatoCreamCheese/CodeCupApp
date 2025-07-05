package com.example.codecupapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.data.CartItem
import com.example.codecupapp.databinding.ItemCartBinding

class CartAdapter(
    private var items: MutableList<CartItem>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // Holds reference to item view binding
    inner class CartViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    // Inflate layout and create ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    // Bind data to ViewHolder
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        bindItem(holder, item)
    }

    override fun getItemCount(): Int = items.size

    // Bind item data to UI components
    private fun bindItem(holder: CartViewHolder, item: CartItem) {
        holder.binding.apply {
            textCartItemName.text = item.name
            textCartItemOptions.text = formatOptions(item)
            textCartItemPrice.text = "$%.2f x${item.quantity}".format(item.unitPrice)
        }
    }
    // Determine whether to show shot information
    private fun shouldDisplayShot(item: CartItem): String? {
        val skipShotFor = listOf("Latte", "Milk Tea", "Mocha", "Pumpkin Spice", "Taco Milktea")
        return if (item.name in skipShotFor) null else "${item.shot} shot"
    }

    // Format cart item options based on item properties
    private fun formatOptions(item: CartItem): String {
        return listOfNotNull(
            item.ice.takeIf { it.isNotBlank() },
            item.temperature.takeIf { it.isNotBlank() },
            item.size.takeIf { it.isNotBlank() },
            shouldDisplayShot(item) // This is already String? or null
        ).joinToString(" | ")
    }





    // Return item at given index if valid
    fun getItemAt(position: Int): CartItem? =
        items.getOrNull(position)

    // Remove item and update adapter
    fun removeAt(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // Replace item list and refresh view
    fun updateData(newItems: MutableList<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
