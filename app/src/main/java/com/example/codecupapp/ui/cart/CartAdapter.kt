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

    // 🧱 ViewHolder using ViewBinding
    inner class CartViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    // 🔧 Inflate and create ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    // 🖼️ Bind data to view
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]

        holder.binding.apply {
            textCartItemName.text = item.name
            textCartItemOptions.text = "${item.ice} | ${item.temperature} | ${item.size} | ${item.shot}"
            textCartItemPrice.text = "$%.2f x${item.quantity}".format(item.unitPrice)
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItemAt(position: Int): CartItem? =
        if (position in items.indices) items[position] else null

    fun removeAt(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateData(newItems: MutableList<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}