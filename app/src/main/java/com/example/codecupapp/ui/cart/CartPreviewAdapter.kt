package com.example.codecupapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.codecupapp.data.CartItem
import com.example.codecupapp.databinding.ItemCartPreviewBinding

class CartPreviewAdapter(
    private val items: List<CartItem>
) : RecyclerView.Adapter<CartPreviewAdapter.CartViewHolder>() {

    inner class CartViewHolder(private val binding: ItemCartPreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItem) {
            binding.textCartItemName.text = item.name
            binding.textCartItemDetails.text =
                "${item.size}, ${item.temperature}, ${item.shot} shot, ${item.ice} ice"
            binding.textCartItemQuantity.text = "x${item.quantity}"
            binding.textCartItemPrice.text = "$%.2f".format(item.unitPrice * item.quantity)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartPreviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
